package ar.edu.itba.paw.webapp.controller;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.function.Predicate;

public class RedirectHelperTest {

    private static final String FALLBACK = "/fallback";
    private static final String PAW_HOST = "pawserver.it.itba.edu.ar";
    private static final String PAW_CONTEXT = "/paw-2026b-08";

    private static MockHttpServletRequest buildDevRequest() {
        final var request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        return request;
    }

    private static MockHttpServletRequest buildProdRequest() {
        final var request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName(PAW_HOST);
        request.setServerPort(443);
        request.setContextPath(PAW_CONTEXT);
        return request;
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resolveReferer (2-arg overload) - Valid cases                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResolveRefererValidSameOriginCustomPortPreservesQuery() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "http://localhost:8080/listing?categoryId=2&page=3");

        final String result = RedirectHelper.resolveReferer(request, FALLBACK);

        Assert.assertEquals("/listing?categoryId=2&page=3", result);
    }

    @Test
    public void testResolveRefererValidProdWithContextPathStripsContextPath() {
        final var request = buildProdRequest();
        request.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-08/listing/42?sort=price");

        final String result = RedirectHelper.resolveReferer(request, FALLBACK);

        Assert.assertEquals("/listing/42?sort=price", result);
    }

    @Test
    public void testResolveRefererRootWithoutTrailingSlashNormalizesToSlash() {
        final var devRequest = buildDevRequest();
        devRequest.addHeader("Referer", "http://localhost:8080");
        Assert.assertEquals("/", RedirectHelper.resolveReferer(devRequest, FALLBACK));

        final var prodRequest = buildProdRequest();
        prodRequest.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-08?welcome=1");
        Assert.assertEquals("/?welcome=1", RedirectHelper.resolveReferer(prodRequest, FALLBACK));
    }

    @Test
    public void testResolveRefererCaseInsensitiveSchemeAndHost() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "HTTP://LOCALHOST:8080/profile");

        Assert.assertEquals("/profile", RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererBehindTlsTerminatingReverseProxy() {
        final var request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName(PAW_HOST);
        request.setServerPort(80);
        request.setContextPath(PAW_CONTEXT);
        request.addHeader("X-Forwarded-Proto", "https, http");
        request.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-08/account/my-offers");

        Assert.assertEquals("/account/my-offers", RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererBehindReverseProxyWithForwardedPort() {
        final var request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("example.org");
        request.setServerPort(8080);
        request.setContextPath("");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Port", "8443");
        request.addHeader("Referer", "https://example.org:8443/listing");

        Assert.assertEquals("/listing", RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererStripsUrlFragment() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "http://localhost:8080/listing/10?tab=details#offers");

        Assert.assertEquals("/listing/10?tab=details", RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererAllowsEncodedBackslashInQueryParam() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "http://localhost:8080/listing?query=C%3A%5CUsers");

        Assert.assertEquals("/listing?query=C%3A%5CUsers", RedirectHelper.resolveReferer(request, FALLBACK));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resolveReferer - Security & Open Redirect rejection cases                                       */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResolveRefererMissingOrBlankReturnsFallback() {
        final var missingRequest = buildDevRequest();
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(missingRequest, FALLBACK));

        final var blankRequest = buildDevRequest();
        blankRequest.addHeader("Referer", "   ");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(blankRequest, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsExternalDomain() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "https://evil.com/phishing");

        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsLookalikeAndSubdomainHosts() {
        final var subdomainReq = buildDevRequest();
        subdomainReq.addHeader("Referer", "http://localhost.evil.com:8080/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(subdomainReq, FALLBACK));

        final var prefixHostReq = buildDevRequest();
        prefixHostReq.addHeader("Referer", "http://evillocalhost:8080/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(prefixHostReq, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsDifferentPortOnSameHost() {
        final var otherCustomPort = buildDevRequest();
        otherCustomPort.addHeader("Referer", "http://localhost:3000/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(otherCustomPort, FALLBACK));

        final var defaultPortAgainst8080 = buildDevRequest();
        defaultPortAgainst8080.addHeader("Referer", "http://localhost/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(defaultPortAgainst8080, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsSchemeMismatch() {
        final var httpsOnHttpDev = buildDevRequest();
        httpsOnHttpDev.addHeader("Referer", "https://localhost:8080/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(httpsOnHttpDev, FALLBACK));

        final var httpOnHttpsProd = buildProdRequest();
        httpOnHttpsProd.addHeader("Referer", "http://pawserver.it.itba.edu.ar/paw-2026b-08/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(httpOnHttpsProd, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsNonHttpSchemes() {
        final var jsAuthorityReq = buildDevRequest();
        jsAuthorityReq.addHeader("Referer", "javascript://localhost:8080/%0aalert(1)");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(jsAuthorityReq, FALLBACK));

        final var jsOpaqueReq = buildDevRequest();
        jsOpaqueReq.addHeader("Referer", "javascript:alert(1)");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(jsOpaqueReq, FALLBACK));

        final var ftpReq = buildDevRequest();
        ftpReq.addHeader("Referer", "ftp://localhost:8080/pub/file");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(ftpReq, FALLBACK));

        final var dataReq = buildDevRequest();
        dataReq.addHeader("Referer", "data:text/html,<script>alert(1)</script>");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(dataReq, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsProtocolRelativeUri() {
        final var sameHostProtoRel = buildDevRequest();
        sameHostProtoRel.addHeader("Referer", "//localhost:8080/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(sameHostProtoRel, FALLBACK));

        final var externalProtoRel = buildDevRequest();
        externalProtoRel.addHeader("Referer", "//evil.com/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(externalProtoRel, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsUserInfoCredentials() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "http://evil.com@localhost:8080/listing");

        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(request, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsCrossContextPathOnSharedHost() {
        final var otherGroupReq = buildProdRequest();
        otherGroupReq.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-01/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(otherGroupReq, FALLBACK));

        final var prefixCollisionReq = buildProdRequest();
        prefixCollisionReq.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-080/listing");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(prefixCollisionReq, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsDoubleSlashAndEncodedSlashAfterContextPath() {
        final var devDoubleSlash = buildDevRequest();
        devDoubleSlash.addHeader("Referer", "http://localhost:8080//evil.com");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(devDoubleSlash, FALLBACK));

        final var prodDoubleSlash = buildProdRequest();
        prodDoubleSlash.addHeader("Referer", "https://pawserver.it.itba.edu.ar/paw-2026b-08//evil.com");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(prodDoubleSlash, FALLBACK));

        final var encodedSlash = buildDevRequest();
        encodedSlash.addHeader("Referer", "http://localhost:8080/%2Fevil.com");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(encodedSlash, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsBackslashesAndControlCharsInPath() {
        final var rawBackslash = buildDevRequest();
        rawBackslash.addHeader("Referer", "http://localhost:8080/\\evil.com");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(rawBackslash, FALLBACK));

        final var encodedBackslash = buildDevRequest();
        encodedBackslash.addHeader("Referer", "http://localhost:8080/%5Cevil.com");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(encodedBackslash, FALLBACK));

        final var crlfHeader = buildDevRequest();
        crlfHeader.addHeader("Referer", "http://localhost:8080/listing\r\nSet-Cookie:evil=1");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(crlfHeader, FALLBACK));

        final var encodedCrlf = buildDevRequest();
        encodedCrlf.addHeader("Referer", "http://localhost:8080/listing%0d%0a");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(encodedCrlf, FALLBACK));
    }

    @Test
    public void testResolveRefererRejectsMalformedUri() {
        final var request = buildDevRequest();
        request.addHeader("Referer", "http://[::1");

        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(request, FALLBACK));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resolveReferer (3-arg overload) & hasPathPrefix                                                 */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResolveRefererWithPredicateAllowsMatchingPathPrefixes() {
        final Predicate<String> offerFilter = RedirectHelper.hasPathPrefix(
                "/account/my-offers",
                "/account/incoming-offers"
        );

        final var myOffersReq = buildProdRequest();
        myOffersReq.addHeader(
                "Referer",
                "https://pawserver.it.itba.edu.ar/paw-2026b-08/account/my-offers?statusGroup=pending"
        );
        Assert.assertEquals(
                "/account/my-offers?statusGroup=pending",
                RedirectHelper.resolveReferer(myOffersReq, FALLBACK, offerFilter)
        );

        final var incomingDetailReq = buildDevRequest();
        incomingDetailReq.addHeader("Referer", "http://localhost:8080/account/incoming-offers/15");
        Assert.assertEquals(
                "/account/incoming-offers/15",
                RedirectHelper.resolveReferer(incomingDetailReq, FALLBACK, offerFilter)
        );
    }

    @Test
    public void testResolveRefererWithPredicateRejectsQueryFragmentAndTraversalSpoofs() {
        final Predicate<String> offerFilter = RedirectHelper.hasPathPrefix(
                "/account/my-offers",
                "/account/incoming-offers"
        );

        final var querySpoof = buildDevRequest();
        querySpoof.addHeader("Referer", "http://localhost:8080/other?next=/account/my-offers");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(querySpoof, FALLBACK, offerFilter));

        final var fragmentSpoof = buildDevRequest();
        fragmentSpoof.addHeader("Referer", "http://localhost:8080/other#/account/my-offers");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(fragmentSpoof, FALLBACK, offerFilter));

        final var traversalSpoof = buildDevRequest();
        traversalSpoof.addHeader("Referer", "http://localhost:8080/account/my-offers/../../logout");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(traversalSpoof, FALLBACK, offerFilter));

        final var encodedTraversalSpoof = buildDevRequest();
        encodedTraversalSpoof.addHeader("Referer", "http://localhost:8080/account/my-offers/%2e%2e/other");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(encodedTraversalSpoof, FALLBACK, offerFilter));

        final var boundaryCollision = buildDevRequest();
        boundaryCollision.addHeader("Referer", "http://localhost:8080/account/my-offers-evil");
        Assert.assertEquals(FALLBACK, RedirectHelper.resolveReferer(boundaryCollision, FALLBACK, offerFilter));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resolvePath (2-arg & 3-arg overloads)                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResolvePathAcceptsValidRelativePaths() {
        Assert.assertEquals(
                "/admin?success=listing_takedown",
                RedirectHelper.resolvePath("/admin?success=listing_takedown", FALLBACK)
        );
        Assert.assertEquals(
                "/admin/offers?page=2",
                RedirectHelper.resolvePath("/admin/offers?page=2#top", FALLBACK, RedirectHelper.hasPathPrefix("/admin"))
        );
    }

    @Test
    public void testResolvePathRejectsOpenRedirectPayloads() {
        final Predicate<String> adminFilter = RedirectHelper.hasPathPrefix("/admin");

        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath(null, FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("   ", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("https://evil.com/admin", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("http://localhost:8080/admin", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("//evil.com/admin", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("///evil.com", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/%2fevil.com", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/%5cevil.com", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("javascript:alert(1)", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("admin/offers", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("evil.com/admin", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/admin/../logout", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/admin/%2e%2e/logout", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/admin\r\nX-Injected: 1", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/admin-panel", FALLBACK, adminFilter));
        Assert.assertEquals(FALLBACK, RedirectHelper.resolvePath("/listing/10", FALLBACK, adminFilter));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* appendQueryParam                                                                                 */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testAppendQueryParamAddsToPathWithoutQuery() {
        Assert.assertEquals(
                "/admin/catalog?success=brand_created",
                RedirectHelper.appendQueryParam("/admin/catalog", "success", "brand_created")
        );
    }

    @Test
    public void testAppendQueryParamPreservesExistingParamsAndEncodesValue() {
        Assert.assertEquals(
                "/admin/catalog?page=2&sort=BRAND_ASC&error=name%3A+taken",
                RedirectHelper.appendQueryParam(
                        "/admin/catalog?page=2&sort=BRAND_ASC",
                        "error",
                        "name: taken"
                )
        );
    }

    @Test
    public void testAppendQueryParamReplacesExistingParamWithoutDuplicating() {
        Assert.assertEquals(
                "/admin/catalog?page=2&success=model_created",
                RedirectHelper.appendQueryParam(
                        "/admin/catalog?success=brand_created&page=2",
                        "success",
                        "model_created"
                )
        );
    }

    @Test
    public void testAppendQueryParamReplacesEncodedParamName() {
        Assert.assertEquals(
                "/admin/catalog?page=2&sort=NAME_ASC",
                RedirectHelper.appendQueryParam(
                        "/admin/catalog?page=2&sort=BRAND_ASC%20",
                        "sort",
                        "NAME_ASC"
                )
        );
    }

    @Test
    public void testAppendQueryParamPreservesEncodedQueryValues() {
        Assert.assertEquals(
                "/admin/catalog?query=a%26b&page=2",
                RedirectHelper.appendQueryParam(
                        "/admin/catalog?query=a%26b",
                        "page",
                        "2"
                )
        );
    }

    @Test(expected = NullPointerException.class)
    public void testAppendQueryParamNullPathThrowsNpe() {
        RedirectHelper.appendQueryParam(null, "page", "2");
    }

    @Test(expected = NullPointerException.class)
    public void testAppendQueryParamNullNameThrowsNpe() {
        RedirectHelper.appendQueryParam("/admin", null, "2");
    }

    @Test(expected = NullPointerException.class)
    public void testAppendQueryParamNullValueThrowsNpe() {
        RedirectHelper.appendQueryParam("/admin", "page", null);
    }

    @Test(expected = NullPointerException.class)
    public void testResolveRefererNullRequestThrowsNpe() {
        RedirectHelper.resolveReferer(null, FALLBACK);
    }

    @Test(expected = NullPointerException.class)
    public void testResolveRefererNullFallbackThrowsNpe() {
        RedirectHelper.resolveReferer(buildDevRequest(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testResolvePathNullPredicateThrowsNpe() {
        RedirectHelper.resolvePath("/admin", FALLBACK, null);
    }
}
