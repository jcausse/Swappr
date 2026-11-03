<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<div class="sticky top-0 pt-3 z-50">
    <nav class="mx-auto max-w-6xl rounded-xl relative z-10">
        <div class="rounded-xl w-full p-2 flex flex-row items-center gap-2 bg-white/80 backdrop-blur-sm border border-black/10">
            <c:url value='/' var="homeUrl" />
            <c:url value='/listing' var="buyUrl" />
            <c:url value='/listing/by-product' var="productsUrl" />
            <c:url value='/listing/new/choose-product' var="sellUrl" />

            <paw:linkButton href="${homeUrl}" variant="ghost" size="sm">
                <img src="<c:url value='/static-image/logo.svg'/>" class="min-w-24" alt="Swappr Logo" />
            </paw:linkButton>

            <spring:message code="navbar.discover" var="linkDiscover"/>
            <spring:message code="navbar.sell" var="linkSell"/>
            <spring:message code="navbar.products" var="linkProducts"/>
            <spring:message code="navbar.categories" var="linkCategories"/>
            <paw:linkButton href="${buyUrl}" variant="ghost" text="${linkDiscover}" />
            <paw:linkButton href="${productsUrl}" variant="ghost" text="${linkProducts}" />
            <paw:menu id="categoriesMenu" variant="ghost" text="${linkCategories}" panelWidth="w-64" panelAlign="left">
                <c:forEach var="category" items="${categories}">
                    <c:url value="/listing" var="categoryUrl">
                        <c:param name="categoryId" value="${category.id}"/>
                    </c:url>
                    <spring:message code="category.${category.name}" var="categoryLabel"/>
                    <paw:linkButton href="${categoryUrl}" text="${categoryLabel}" variant="ghost" role="secondary" classname="w-full justify-start text-sm" />
                </c:forEach>
            </paw:menu>
            <paw:linkButton href="${sellUrl}" variant="ghost" text="${linkSell}" />

            <div class="ml-auto flex flex-row items-center gap-3">
                <%-- Language toggle. /language stores the locale (via LocaleChangeInterceptor) and redirects back. --%>
                <c:set var="currentLang" value="${pageContext.response.locale.language}"/>
                <c:url value="/language" var="langUrlEs"><c:param name="lang" value="es"/></c:url>
                <c:url value="/language" var="langUrlEn"><c:param name="lang" value="en"/></c:url>
                <div class="flex flex-row items-center text-xs font-medium">
                    <a href="${langUrlEs}"
                        class="w-7 text-center py-0.5 rounded ${currentLang eq 'es' ? 'text-lime-600 font-bold' : 'text-black/40 hover:text-black/70'}">
                        <spring:message code="navbar.lang.es"/>
                    </a>
                    <span class="text-black/20">|</span>
                    <a href="${langUrlEn}"
                        class="w-7 text-center py-0.5 rounded ${currentLang eq 'en' ? 'text-lime-600 font-bold' : 'text-black/40 hover:text-black/70'}">
                        <spring:message code="navbar.lang.en"/>
                    </a>
                </div>

                <c:choose>
                    <c:when test="${currentUser.isPresent()}">
                        <div class="relative" id="userMenu">
                            <paw:button id="userButton" variant="ghost" classname="justify-start text-start gap-3 h-14 min-w-40!" type="button">
                                <div class="flex flex-row gap-2 items-center text-sm">
                                    <paw:userAvatar user="${currentUser.get()}" size="8" />
                                    <div class="flex flex-col">
                                        <p class="text-sm text-black font-normal"><c:out value="${currentUser.get().displayName}"/></p>
                                        <p class="text-xs text-black/60 font-normal"><c:out value="${currentUser.get().username}"/></p>
                                    </div>
                                </div>
                            </paw:button>

                            <div id="userPanel" class="hidden absolute right-0 top-full w-48 bg-white rounded-2xl border border-black/10 z-50 flex flex-col p-2">
                                <c:url value="/account" var="accountUrl"/>
                                <c:url value="/profile" var="profileUrl"/>
                                <spring:message code="navbar.account" var="accountLabel" />
                                <paw:linkButton href="${accountUrl}" text="${accountLabel}" variant="ghost" role="secondary" />
                                <spring:message code="navbar.profile" var="profileLabel" />
                                <paw:linkButton href="${profileUrl}" text="${profileLabel}" variant="ghost" role="secondary" />

                                <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
                                    <c:url value="/admin" var="adminUrl"/>
                                    <spring:message code="navbar.admin" var="adminLabel" />
                                    <paw:linkButton href="${adminUrl}" text="${adminLabel}" variant="ghost" role="secondary" />
                                </c:if>

                                <hr class="border-t border-black/10 my-1">

                                <c:url value="/logout" var="logoutUrl"/>
                                <form action="${logoutUrl}" method="post" class="flex flex-col">
                                    <spring:message code="navbar.logout" var="logoutLabel"/>
                                    <paw:button text="${logoutLabel}" type="submit" variant="ghost" role="danger" />
                                </form>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a href="<c:url value='/login'/>">
                            <spring:message code="navbar.signin" var="signinLabel"/>
                            <paw:button text="${signinLabel}" variant="outline" size="sm"/>
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </nav>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const userButton = document.getElementById('userButton');
        const userPanel = document.getElementById('userPanel');
        const userMenu = document.getElementById('userMenu');

        if (userButton && userPanel) {
            userButton.addEventListener('click', function(e) {
                e.stopPropagation();
                userPanel.classList.toggle('hidden');
            });

            document.addEventListener('click', function(e) {
                if (!userMenu.contains(e.target)) {
                    userPanel.classList.add('hidden');
                }
            });
        }
    });
</script>
