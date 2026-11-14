package ar.edu.itba.paw.webapp.controller;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
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
    private static final String FORWARDED_HEADER = "Forwarded";
    private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    private static final String FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    private static final String FORWARDED_PORT_HEADER = "X-Forwarded-Port";
    private static final String FORWARDED_SSL_HEADER = "X-Forwarded-Ssl";
    private static final String APP_PROPERTIES_RESOURCE = "/app.properties";
    private static final String BASE_URL_PROPERTY = "app.baseUrl";
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private static final ConfiguredBaseUrl CONFIGURED_BASE_URL = loadConfiguredBaseUrl();

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
        return resolveReferer(request, fallbackPath, allowedPathPredicate, CONFIGURED_BASE_URL);
    }

    static String resolveReferer(
            final @NonNull HttpServletRequest request,
            final @NonNull String fallbackPath,
            final @NonNull Predicate<String> allowedPathPredicate,
            final URI configuredBaseUrlUri
    ) {
        return resolveReferer(request, fallbackPath, allowedPathPredicate, toConfiguredBaseUrl(configuredBaseUrlUri));
    }

    private static String resolveReferer(
            final HttpServletRequest request,
            final String fallbackPath,
            final Predicate<String> allowedPathPredicate,
            final ConfiguredBaseUrl configuredBaseUrl
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

        final String contextPath = normalizeContextPath(request.getContextPath());
        final Origin refererOrigin = extractOrigin(refererUri);
        final Origin requestOrigin = resolveRequestOrigin(request);
        if (refererOrigin == null || (!refererOrigin.matches(requestOrigin)
                && !matchesConfiguredBaseUrl(refererOrigin, contextPath, configuredBaseUrl))) {
            LOGGER.warn("Rejected cross-origin Referer '{}' (expected origin {})", referer, requestOrigin);
            return fallbackPath;
        }

        final String rawFullPath = Optional.ofNullable(refererUri.getRawPath()).orElse("");
        final String decodedFullPath = Optional.ofNullable(refererUri.getPath()).orElse("");
        if (hasDotSegments(decodedFullPath) || decodedFullPath.contains("\\") || containsControlChars(decodedFullPath)) {
            LOGGER.warn("Rejected Referer with unsafe path segments: {}", referer);
            return fallbackPath;
        }

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
     * Adds or replaces a single query parameter on a validated context-relative {@code path[?query]} target,
     * encoding both the parameter name and value. An existing parameter with the same name is removed first, so the
     * returned target never contains duplicate values for {@code name}.
     */
    public static String appendQueryParam(final String path, final String name, final String value) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");

        final URI uri;
        try {
            uri = new URI(path.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid path syntax: " + path, e);
        }

        final String rawPath = Optional.ofNullable(uri.getRawPath()).orElse("");
        final String rawQuery = uri.getRawQuery();
        final List<String> params = new ArrayList<>();
        if (rawQuery != null && !rawQuery.isEmpty()) {
            for (final String pair : rawQuery.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                final int separatorIdx = pair.indexOf('=');
                final String rawName = separatorIdx >= 0 ? pair.substring(0, separatorIdx) : pair;
                if (!decode(rawName).equals(name)) {
                    params.add(pair);
                }
            }
        }
        params.add(encode(name) + "=" + encode(value));
        return rawPath + "?" + String.join("&", params);
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

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(final String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private static ConfiguredBaseUrl loadConfiguredBaseUrl() {
        try (InputStream stream = RedirectHelper.class.getResourceAsStream(APP_PROPERTIES_RESOURCE)) {
            if (stream == null) {
                return null;
            }
            final Properties properties = new Properties();
            properties.load(stream);
            final String baseUrl = properties.getProperty(BASE_URL_PROPERTY);
            if (baseUrl == null || baseUrl.isBlank()) {
                return null;
            }
            return toConfiguredBaseUrl(URI.create(baseUrl.trim()));
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Unable to parse {} from {}: {}", BASE_URL_PROPERTY, APP_PROPERTIES_RESOURCE, e.getMessage());
            return null;
        }
    }

    private static ConfiguredBaseUrl toConfiguredBaseUrl(final URI uri) {
        if (uri == null) {
            return null;
        }
        final Origin origin = extractOrigin(uri);
        if (origin == null) {
            return null;
        }
        return new ConfiguredBaseUrl(origin, normalizeContextPath(uri.getRawPath()));
    }

    private static boolean matchesConfiguredBaseUrl(
            final Origin refererOrigin,
            final String requestContextPath,
            final ConfiguredBaseUrl configuredBaseUrl
    ) {
        return configuredBaseUrl != null
                && refererOrigin.matches(configuredBaseUrl.origin())
                && configuredBaseUrl.contextPath().equals(requestContextPath);
    }

    private static Origin extractOrigin(final URI uri) {
        final String scheme = uri.getScheme();
        if (!isHttpScheme(scheme) || uri.getRawUserInfo() != null) {
            return null;
        }
        final String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return null;
        }
        final int port = uri.getPort() != -1 ? uri.getPort() : defaultPortForScheme(scheme);
        return new Origin(scheme, host, port);
    }

    private static Origin resolveRequestOrigin(final HttpServletRequest request) {
        final ForwardedValues forwarded = parseForwardedHeader(request.getHeader(FORWARDED_HEADER));
        final String effectiveScheme = resolveRequestScheme(request, forwarded);

        final HostAndPort forwardedHost = resolveForwardedHost(request, forwarded);
        final String effectiveHost = forwardedHost != null
                ? forwardedHost.host()
                : request.getServerName();
        final int effectivePort = resolveRequestPort(request, effectiveScheme, forwardedHost);
        return new Origin(effectiveScheme, effectiveHost, effectivePort);
    }

    private static boolean isHttpScheme(final String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static int defaultPortForScheme(final String scheme) {
        return "https".equalsIgnoreCase(scheme) ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT;
    }

    private static String resolveRequestScheme(final HttpServletRequest request, final ForwardedValues forwarded) {
        if (isHttpScheme(forwarded.proto())) {
            return forwarded.proto();
        }
        final String forwardedProto = firstHeaderToken(request.getHeader(FORWARDED_PROTO_HEADER));
        if (isHttpScheme(forwardedProto)) {
            return forwardedProto;
        }
        final String forwardedSsl = firstHeaderToken(request.getHeader(FORWARDED_SSL_HEADER));
        if ("on".equalsIgnoreCase(forwardedSsl)) {
            return "https";
        }
        return request.getScheme();
    }

    private static HostAndPort resolveForwardedHost(
            final HttpServletRequest request,
            final ForwardedValues forwarded
    ) {
        if (forwarded.host() != null) {
            final HostAndPort parsed = parseHostAndPort(forwarded.host());
            if (parsed != null) {
                return parsed;
            }
        }
        final String forwardedHostHeader = firstHeaderToken(request.getHeader(FORWARDED_HOST_HEADER));
        if (forwardedHostHeader != null) {
            return parseHostAndPort(forwardedHostHeader);
        }
        return null;
    }

    private static int resolveRequestPort(
            final HttpServletRequest request,
            final String effectiveScheme,
            final HostAndPort forwardedHost
    ) {
        final Integer forwardedPort = parsePositivePort(firstHeaderToken(request.getHeader(FORWARDED_PORT_HEADER)));
        if (forwardedPort != null) {
            return forwardedPort;
        }
        if (forwardedHost != null) {
            return forwardedHost.port() != null
                    ? forwardedHost.port()
                    : defaultPortForScheme(effectiveScheme);
        }
        // TLS terminated at a reverse proxy (with internal Tomcat running on 80, 8080, etc.) exposes standard 443 externally
        if ("http".equalsIgnoreCase(request.getScheme()) && "https".equalsIgnoreCase(effectiveScheme)) {
            return HTTPS_DEFAULT_PORT;
        }
        final int serverPort = request.getServerPort();
        return serverPort > 0 ? serverPort : defaultPortForScheme(effectiveScheme);
    }

    private static ForwardedValues parseForwardedHeader(final String headerValue) {
        final String firstElement = firstHeaderToken(headerValue);
        if (firstElement == null) {
            return new ForwardedValues(null, null);
        }
        String proto = null;
        String host = null;
        for (final String param : firstElement.split(";")) {
            final int eqIdx = param.indexOf('=');
            if (eqIdx <= 0) {
                continue;
            }
            final String key = param.substring(0, eqIdx).trim().toLowerCase(Locale.ROOT);
            final String value = stripOptionalQuotes(param.substring(eqIdx + 1).trim());
            if ("proto".equals(key) && proto == null) {
                proto = value;
            } else if ("host".equals(key) && host == null) {
                host = value;
            }
        }
        return new ForwardedValues(proto, host);
    }

    private static String stripOptionalQuotes(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static HostAndPort parseHostAndPort(final String rawHost) {
        if (rawHost == null || rawHost.isBlank() || rawHost.contains("@") || containsControlChars(rawHost)) {
            return null;
        }
        final String trimmed = rawHost.trim();
        if (trimmed.startsWith("[")) {
            final int closeBracket = trimmed.indexOf(']');
            if (closeBracket <= 1) {
                return null;
            }
            final String ipv6Host = trimmed.substring(0, closeBracket + 1);
            if (closeBracket == trimmed.length() - 1) {
                return new HostAndPort(ipv6Host, null);
            }
            if (trimmed.charAt(closeBracket + 1) != ':') {
                return null;
            }
            final Integer port = parsePositivePort(trimmed.substring(closeBracket + 2));
            return port != null ? new HostAndPort(ipv6Host, port) : null;
        }
        final int colonIdx = trimmed.indexOf(':');
        if (colonIdx < 0) {
            return new HostAndPort(trimmed, null);
        }
        if (colonIdx == 0 || trimmed.indexOf(':', colonIdx + 1) >= 0) {
            return null;
        }
        final Integer port = parsePositivePort(trimmed.substring(colonIdx + 1));
        return port != null ? new HostAndPort(trimmed.substring(0, colonIdx), port) : null;
    }

    private static Integer parsePositivePort(final String rawPort) {
        if (rawPort == null || rawPort.isBlank()) {
            return null;
        }
        try {
            final int parsed = Integer.parseInt(rawPort.trim());
            return parsed > 0 && parsed <= 65535 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record Origin(String scheme, String host, int port) {
        private boolean matches(final Origin other) {
            return other != null
                    && scheme.equalsIgnoreCase(other.scheme)
                    && host.equalsIgnoreCase(other.host)
                    && port == other.port;
        }
    }

    private record ConfiguredBaseUrl(Origin origin, String contextPath) {
    }

    private record ForwardedValues(String proto, String host) {
    }

    private record HostAndPort(String host, Integer port) {
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
