package ar.edu.itba.paw.webapp.controller;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Stateless utility for resolving safe, context-relative redirect targets and preventing Open Redirect vulnerabilities.
 * <p>
 * Returned strings are guaranteed to be context-relative paths (e.g. {@code "/listing?page=2"}) or the caller-provided
 * {@code fallbackPath}, without the {@code "redirect:"} view prefix.
 */
public final class RedirectHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectHelper.class);

    private static final String REFERER_HEADER = "Referer";
    private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    private static final String FORWARDED_PORT_HEADER = "X-Forwarded-Port";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private RedirectHelper() {
        throw new AssertionError("Utility class");
    }

    /**
     * Resolves the {@code Referer} header of {@code request} into a normalized context-relative path (preserving query
     * parameters), or returns {@code fallbackPath} if the header is missing, malformed, or cross-origin/cross-context.
     */
    public static String resolveReferer(final HttpServletRequest request, final String fallbackPath) {
        return resolveReferer(request, fallbackPath, path -> true);
    }

    /**
     * Resolves the {@code Referer} header of {@code request} into a normalized context-relative path (preserving query
     * parameters) when it belongs to the same origin and context path and its decoded path satisfies
     * {@code allowedPathPredicate}; otherwise returns {@code fallbackPath}.
     */
    public static String resolveReferer(
            final @NonNull HttpServletRequest request,
            final @NonNull String fallbackPath,
            final @NonNull Predicate<String> allowedPathPredicate
    ) {
        final String referer = request.getHeader(REFERER_HEADER);
        if (referer == null || referer.isBlank() || containsControlChars(referer)) {
            return fallbackPath;
        }

        final URI refererUri;
        try {
            refererUri = new URI(referer.trim());
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid Referer URI syntax: {}", referer);
            return fallbackPath;
        }

        if (!isSameOrigin(refererUri, request)) {
            LOGGER.warn("Rejected cross-origin Referer: {}", referer);
            return fallbackPath;
        }

        final String rawFullPath = Optional.ofNullable(refererUri.getRawPath()).orElse("");
        final String decodedFullPath = Optional.ofNullable(refererUri.getPath()).orElse("");
        if (hasDotSegments(decodedFullPath) || decodedFullPath.contains("\\") || containsControlChars(decodedFullPath)) {
            LOGGER.warn("Rejected Referer with unsafe path segments: {}", referer);
            return fallbackPath;
        }

        final String contextPath = normalizeContextPath(request.getContextPath());
        if (!contextPath.isEmpty() && !matchesPrefix(rawFullPath, contextPath)) {
            LOGGER.warn("Rejected Referer outside application context path '{}': {}", contextPath, referer);
            return fallbackPath;
        }

        final String rawAppPath = rawFullPath.substring(contextPath.length());
        final String normalizedRawAppPath = rawAppPath.isEmpty() ? "/" : rawAppPath;
        final String rawQuery = refererUri.getRawQuery();
        final String candidateRelativeUri = rawQuery != null
                ? normalizedRawAppPath + "?" + rawQuery
                : normalizedRawAppPath;

        return resolvePath(candidateRelativeUri, fallbackPath, allowedPathPredicate);
    }

    /**
     * Validates a candidate context-relative redirect target (e.g. from a {@code redirect} query/form parameter),
     * returning the normalized {@code path[?query]} if safe or {@code fallbackPath} otherwise.
     */
    public static String resolvePath(final String candidatePath, final String fallbackPath) {
        return resolvePath(candidatePath, fallbackPath, path -> true);
    }

    /**
     * Validates a candidate context-relative redirect target and checks its decoded path against
     * {@code allowedPathPredicate}, returning the normalized {@code path[?query]} if safe or {@code fallbackPath}
     * otherwise.
     */
    public static String resolvePath(
            final String candidatePath,
            @NonNull final String fallbackPath,
            @NonNull final Predicate<String> allowedPathPredicate
    ) {
        if (candidatePath == null || candidatePath.isBlank() || containsControlChars(candidatePath)) {
            return fallbackPath;
        }

        final URI uri;
        try {
            uri = new URI(candidatePath.trim());
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid redirect path syntax: {}", candidatePath);
            return fallbackPath;
        }

        if (uri.getScheme() != null || uri.getRawAuthority() != null || uri.getHost() != null || uri.getRawUserInfo() != null) {
            LOGGER.warn("Rejected non-relative redirect target: {}", candidatePath);
            return fallbackPath;
        }

        final String rawPath = uri.getRawPath();
        final String decodedPath = uri.getPath();
        if (rawPath == null || decodedPath == null
                || !rawPath.startsWith("/") || rawPath.startsWith("//")
                || !decodedPath.startsWith("/") || decodedPath.startsWith("//")
                || decodedPath.contains("\\")
                || containsControlChars(decodedPath)
                || hasDotSegments(decodedPath)) {
            LOGGER.warn("Rejected unsafe relative redirect path: {}", candidatePath);
            return fallbackPath;
        }

        if (!allowedPathPredicate.test(decodedPath)) {
            LOGGER.debug("Redirect path '{}' did not match allowed path predicate; using fallback '{}'", decodedPath, fallbackPath);
            return fallbackPath;
        }

        final String rawQuery = uri.getRawQuery();
        return rawQuery != null ? rawPath + "?" + rawQuery : rawPath;
    }

    /**
     * Builds a path predicate that matches any path equal to one of {@code prefixes} or nested beneath it on a {@code '/'}
     * boundary (e.g. prefix {@code "/admin"} matches {@code "/admin"} and {@code "/admin/offers"}, but not
     * {@code "/admin-evil"}).
     */
    public static Predicate<String> hasPathPrefix(final String... prefixes) {
        Objects.requireNonNull(prefixes, "prefixes must not be null");
        return path -> path != null && Arrays.stream(prefixes)
                .filter(Objects::nonNull)
                .anyMatch(prefix -> matchesPrefix(path, prefix));
    }

    private static boolean isSameOrigin(final URI refererUri, final HttpServletRequest request) {
        final String refererScheme = refererUri.getScheme();
        if (!isHttpScheme(refererScheme) || refererUri.getRawUserInfo() != null) {
            return false;
        }

        final String requestScheme = resolveRequestScheme(request);
        if (!refererScheme.equalsIgnoreCase(requestScheme)) {
            return false;
        }

        final String refererHost = refererUri.getHost();
        final String requestHost = request.getServerName();
        if (refererHost == null || !refererHost.equalsIgnoreCase(requestHost)) {
            return false;
        }

        final int refererPort = refererUri.getPort() != -1
                ? refererUri.getPort()
                : defaultPortForScheme(refererScheme);
        final int requestPort = resolveRequestPort(request, requestScheme);

        return refererPort == requestPort;
    }

    private static boolean isHttpScheme(final String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static int defaultPortForScheme(final String scheme) {
        return "https".equalsIgnoreCase(scheme) ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT;
    }

    private static String resolveRequestScheme(final HttpServletRequest request) {
        final String forwardedProto = firstHeaderToken(request.getHeader(FORWARDED_PROTO_HEADER));
        if (isHttpScheme(forwardedProto)) {
            return forwardedProto;
        }
        return request.getScheme();
    }

    private static int resolveRequestPort(final HttpServletRequest request, final String effectiveScheme) {
        final String forwardedPort = firstHeaderToken(request.getHeader(FORWARDED_PORT_HEADER));
        if (forwardedPort != null) {
            try {
                final int parsed = Integer.parseInt(forwardedPort);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Fall back to container port resolution
            }
        }

        final int serverPort = request.getServerPort();
        if (serverPort == HTTP_DEFAULT_PORT
                && "http".equalsIgnoreCase(request.getScheme())
                && "https".equalsIgnoreCase(effectiveScheme)) {
            return HTTPS_DEFAULT_PORT;
        }
        return serverPort > 0 ? serverPort : defaultPortForScheme(effectiveScheme);
    }

    private static String firstHeaderToken(final String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        final int commaIdx = headerValue.indexOf(',');
        final String first = (commaIdx >= 0 ? headerValue.substring(0, commaIdx) : headerValue).trim();
        return first.isEmpty() ? null : first;
    }

    private static String normalizeContextPath(final String contextPath) {
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            return "";
        }
        return contextPath.endsWith("/")
                ? contextPath.substring(0, contextPath.length() - 1)
                : contextPath;
    }

    private static boolean matchesPrefix(final String path, final String prefix) {
        if ("/".equals(prefix)) {
            return path.startsWith("/");
        }
        final String normalizedPrefix = prefix.endsWith("/")
                ? prefix.substring(0, prefix.length() - 1)
                : prefix;
        return path.equals(normalizedPrefix) || path.startsWith(normalizedPrefix + "/");
    }

    private static boolean hasDotSegments(final String decodedPath) {
        return Arrays.stream(decodedPath.split("/", -1))
                .anyMatch(segment -> ".".equals(segment) || "..".equals(segment));
    }

    private static boolean containsControlChars(final String value) {
        return value.chars().anyMatch(ch -> ch <= 0x1F || ch == 0x7F);
    }
}
