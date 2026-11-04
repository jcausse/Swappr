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
                <spring:message code="a11y.logo" var="logoAlt"/>
                <img src="<c:url value='/static-image/logo.svg'/>" class="min-w-24" alt="<c:out value='${logoAlt}'/>" />
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
                        <c:url value="/notifications" var="allNotificationsUrl"/>
                        <c:url value="/notifications/read-all" var="markAllReadUrl"/>
                        <spring:message code="notifications.bell.ariaLabel" var="notificationsBellAria"/>
                        <spring:message code="notifications.dropdown.title" var="notificationsDropdownTitle"/>
                        <spring:message code="notifications.dropdown.empty" var="notificationsDropdownEmpty"/>
                        <spring:message code="notifications.dropdown.showAll" var="notificationsShowAllLabel"/>
                        <spring:message code="notifications.action.markAllRead" var="markAllReadLabel"/>

                        <paw:button id="notificationsButton" popovertarget="notificationsPopover" variant="ghost" role="secondary" classname="h-10 px-2.5 gap-1.5" type="button" ariaLabel="${notificationsBellAria}">
                            <paw:icon name="bell" classname="text-lg text-black/80"/>
                            <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
                                <paw:badge text="${recentUnreadNotificationsPage.totalCount}" classname="text-red-600 min-w-5! p-0.25!" size="sm"/>
                            </c:if>
                        </paw:button>

                        <div id="notificationsPopover" popover="auto" class="fixed inset-auto m-0 p-0 bg-transparent border-0 z-50">
                            <div class="w-96 sm:w-[26rem] max-w-[calc(100vw-1.5rem)] bg-white rounded-2xl border border-black/10 shadow-xl p-3 flex flex-col gap-2.5">
                                <div class="flex flex-row items-center justify-between gap-2 px-1">
                                    <div class="flex flex-row items-center gap-2">
                                        <h3 class="text-sm font-semibold text-black"><c:out value="${notificationsDropdownTitle}"/></h3>
                                        <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
                                            <paw:badge text="${recentUnreadNotificationsPage.totalCount}" classname="text-red-600 min-w-5! p-0.25!" size="sm"/>
                                        </c:if>
                                    </div>
                                    <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
                                        <form action="${markAllReadUrl}" method="post" class="inline-flex">
                                            <paw:button type="submit" variant="ghost" size="sm" icon="check-check" text="${markAllReadLabel}"/>
                                        </form>
                                    </c:if>
                                </div>

                                <paw:divider/>

                                <div class="max-h-96 overflow-y-auto flex flex-col gap-2">
                                    <c:choose>
                                        <c:when test="${empty recentUnreadNotificationsPage.content}">
                                            <div class="py-8 px-4 flex flex-col items-center gap-2 text-center">
                                                <paw:icon name="bell-off" classname="text-2xl text-black/30"/>
                                                <p class="text-sm text-black/50"><c:out value="${notificationsDropdownEmpty}"/></p>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="notification" items="${recentUnreadNotificationsPage.content}">
                                                <paw:notificationCard notification="${notification}" compact="${true}"/>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <c:if test="${recentUnreadNotificationsPage.totalCount > 10}">
                                    <paw:linkButton href="${allNotificationsUrl}" text="${notificationsShowAllLabel}" variant="outline" classname="w-full"/>
                                </c:if>
                            </div>
                        </div>

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
                                <spring:message code="navbar.notifications" var="navbarNotificationsLabel" />
                                <paw:linkButton href="${allNotificationsUrl}" text="${navbarNotificationsLabel}" variant="ghost" role="secondary" />
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
        const notificationsButton = document.getElementById('notificationsButton');
        const notificationsPopover = document.getElementById('notificationsPopover');

        function positionNotificationsPopover() {
            if (!notificationsButton || !notificationsPopover) {
                return;
            }
            const rect = notificationsButton.getBoundingClientRect();
            const margin = 12;
            const popoverWidth = notificationsPopover.offsetWidth || Math.min(416, window.innerWidth - margin * 2);
            let left = rect.right - popoverWidth;
            if (left + popoverWidth > window.innerWidth - margin) {
                left = window.innerWidth - popoverWidth - margin;
            }
            if (left < margin) {
                left = margin;
            }
            notificationsPopover.style.top = (rect.bottom + 8) + 'px';
            notificationsPopover.style.left = left + 'px';
        }

        if (notificationsButton && notificationsPopover) {
            notificationsPopover.addEventListener('beforetoggle', function(e) {
                if (e.newState === 'open') {
                    if (userPanel) {
                        userPanel.classList.add('hidden');
                    }
                    positionNotificationsPopover();
                }
            });

            notificationsPopover.addEventListener('toggle', function(e) {
                if (e.newState === 'open') {
                    positionNotificationsPopover();
                }
            });

            window.addEventListener('resize', function() {
                if (notificationsPopover.matches(':popover-open')) {
                    positionNotificationsPopover();
                }
            });

            window.addEventListener('scroll', function() {
                if (notificationsPopover.matches(':popover-open')) {
                    positionNotificationsPopover();
                }
            }, { passive: true });
        }

        if (userButton && userPanel) {
            userButton.addEventListener('click', function(e) {
                e.stopPropagation();
                if (notificationsPopover && notificationsPopover.matches(':popover-open')) {
                    notificationsPopover.hidePopover();
                }
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
