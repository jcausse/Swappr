<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<html lang="${pageContext.response.locale.language}">
<paw:head titleKey="landing.title">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value="/css/input.css"/>"/>
</paw:head>

<body class="pb-24 bg-neutral-50">
    <paw:navbar/>

    <!-- Hero Banner -->
    <section class="relative w-full max-h-[80vh] h-144 -mt-21.5 pt-24 pb-8 flex flex-col items-center justify-center overflow-hidden bg-gradient-to-b from-lime-500/20 via-neutral-50 to-neutral-50" id="heroBanner">
        <div class="absolute inset-0 bg-gradient-to-r from-lime-500/10 via-transparent to-lime-500/10"></div>
        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-from)_0%,_transparent_70%)] from-lime-500/20"></div>

        <div class="relative z-10 max-w-6xl w-full mx-auto px-6 text-center flex flex-col md:flex-row gap-8 md:gap-16 md:justify-center lg:justify-between items-center">
            <div>
                <div class="mb-8 md:text-start">
                    <p class="text-3xl sm:text-4xl md:text-5xl font-extrabold text-black leading-tight">
                        <spring:message code="landing.hero.title.line1"/>
                    </p>
                    <p class="text-3xl sm:text-4xl md:text-5xl font-extrabold text-lime-600 leading-tight">
                        <spring:message code="landing.hero.title.line2"/>
                    </p>
                    <p class="text-3xl sm:text-4xl md:text-5xl font-extrabold text-lime-600 leading-tight">
                        <spring:message code="landing.hero.title.line3"/>
                    </p>
                </div>

                <div class="flex flex-col sm:flex-row items-center justify-center gap-8 text-sm text-black/60">
                    <div class="flex items-center gap-2">
                        <paw:icon name="zap" classname="w-5 h-5 text-lime-600" />
                        <span class="font-medium">
                            <spring:message code="landing.hero.feature1.line1"/><br/>
                            <spring:message code="landing.hero.feature1.line2"/>
                        </span>
                    </div>
                    <div class="w-px h-6 bg-black/10 hidden sm:block"></div>
                    <div class="flex items-center gap-2">
                        <paw:icon name="map-pin" classname="w-5 h-5 text-lime-600" />
                        <span class="font-medium">
                            <spring:message code="landing.hero.feature2.line1"/><br/>
                            <spring:message code="landing.hero.feature2.line2"/>
                        </span>
                    </div>
                </div>
            </div>

            <div>
                <div class="flex flex-row md:flex-col items-center md:items-stretch md:w-80 justify-center gap-4">
                    <c:url value="/listing/new/choose-product" var="sellUrl"/>
                    <spring:message code="landing.hero.cta.explore" var="exploreCta"/>
                    <spring:message code="landing.hero.cta.sell" var="sellCta"/>
                    <paw:linkButton href="javascript:void(0);" variant="default" size="lg" text="${exploreCta}"
                        onclick="window.scrollTo({behavior: 'smooth', top: document.getElementById('heroBanner').offsetHeight - 100});" />
                    <paw:linkButton href="${sellUrl}" variant="outline" size="lg" text="${sellCta}" />
                </div>
            </div>
        </div>
    </section>

    <main class="max-w-6xl mx-auto px-6 py-8 flex flex-col gap-12">
        <!-- Hot Items -->
        <c:if test="${not empty hotItems}">
            <c:url value="/listing?sort=MOST_OFFERS" var="hotItemsSeeMoreUrl"/>
            <spring:message code="landing.hotItems.title" var="hotItemsTitle"/>
            <spring:message code="landing.hotItems.subtitle" var="hotItemsSubtitle"/>
            <spring:message code="landing.seeMore" var="seeMoreLabel"/>
            <paw:listingCarousel
                id="hotItemsCarousel"
                title="${hotItemsTitle}"
                subtitle="${hotItemsSubtitle}"
                listings="${hotItems}"
                seeMoreUrl="${hotItemsSeeMoreUrl}"
                seeMoreLabel="${seeMoreLabel}"
            />
        </c:if>

        <!-- Trending Items -->
        <c:if test="${not empty trendingItems}">
            <c:url value="/listing?sort=RECENT_OFFERS" var="trendingItemsSeeMoreUrl"/>
            <spring:message code="landing.trendingItems.title" var="trendingItemsTitle"/>
            <spring:message code="landing.trendingItems.subtitle" var="trendingItemsSubtitle"/>
            <spring:message code="landing.seeMore" var="seeMoreLabel"/>
            <paw:listingCarousel
                id="trendingItemsCarousel"
                title="${trendingItemsTitle}"
                subtitle="${trendingItemsSubtitle}"
                listings="${trendingItems}"
                seeMoreUrl="${trendingItemsSeeMoreUrl}"
                seeMoreLabel="${seeMoreLabel}"
            />
        </c:if>

        <!-- New Items -->
        <c:if test="${not empty newItems}">
            <c:url value="/listing?sort=RECENT" var="newItemsSeeMoreUrl"/>
            <spring:message code="landing.newItems.title" var="newItemsTitle"/>
            <spring:message code="landing.newItems.subtitle" var="newItemsSubtitle"/>
            <spring:message code="landing.seeMore" var="seeMoreLabel"/>
            <paw:listingCarousel
                id="newItemsCarousel"
                title="${newItemsTitle}"
                subtitle="${newItemsSubtitle}"
                listings="${newItems}"
                seeMoreUrl="${newItemsSeeMoreUrl}"
                seeMoreLabel="${seeMoreLabel}"
            />
        </c:if>

        <!-- Categories Grid -->
        <c:if test="${not empty categories}">
            <spring:message code="landing.categories.title" var="categoriesTitle"/>
            <div>
                <h2 class="text-xl font-bold mb-4"><c:out value="${categoriesTitle}"/></h2>
                <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                    <c:forEach var="category" items="${categories}">
                        <c:url value="/listing" var="categoryUrl">
                            <c:param name="categoryId" value="${category.id}"/>
                        </c:url>
                        <spring:message code="category.${category.name}" var="categoryLabel"/>
                        <paw:linkButton
                            href="${categoryUrl}"
                            text="${categoryLabel}"
                            variant="outline"
                            size="lg"
                            classname="w-full justify-center"
                        />
                    </c:forEach>
                </div>
            </div>
        </c:if>
    </main>
</body>
</html>
