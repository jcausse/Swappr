<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<spring:message code="admin.dashboard.title" var="dashboardTitle"/>
<spring:message code="admin.dashboard.subtitle" var="dashboardSubtitle"/>

<c:url value="/admin/listings" var="listingsUrl"/>
<c:url value="/admin/offers" var="offersUrl"/>
<c:url value="/admin/users" var="usersUrl"/>
<c:url value="/admin/listings/takedown" var="takedownListingUrl"/>
<c:url value="/admin/offers/takedown" var="takedownOfferUrl"/>
<c:url value="/admin/users/suspend" var="suspendUserUrl"/>
<c:url value="/admin/users/grant-admin" var="grantAdminUrl"/>

<admin:layout titleKey="admin.dashboard.title" title="${dashboardTitle}" subtitle="${dashboardSubtitle}">
    <div class="flex flex-col gap-6">
        <%-- Quick Navigation Cards --%>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <paw:card classname="p-5 flex flex-col justify-between hover:border-black/20 transition">
                <div class="flex items-center gap-3 mb-2">
                    <span class="p-2 rounded-lg bg-lime-100 text-lime-700">
                        <paw:icon name="store"/>
                    </span>
                    <spring:message code="admin.sidebar.listings" var="listingsCardTitle"/>
                    <h2 class="font-semibold text-base"><c:out value="${listingsCardTitle}"/></h2>
                </div>
                <spring:message code="admin.dashboard.listingsDesc" var="listingsCardDesc"/>
                <p class="text-xs text-black/60 mb-4"><c:out value="${listingsCardDesc}"/></p>
                <spring:message code="admin.dashboard.browseListings" var="browseListingsText"/>
                <paw:linkButton href="${listingsUrl}" text="${browseListingsText}" variant="outline" size="sm" icon="arrow-right"/>
            </paw:card>

            <paw:card classname="p-5 flex flex-col justify-between hover:border-black/20 transition">
                <div class="flex items-center gap-3 mb-2">
                    <span class="p-2 rounded-lg bg-blue-100 text-blue-700">
                        <paw:icon name="handshake"/>
                    </span>
                    <spring:message code="admin.sidebar.offers" var="offersCardTitle"/>
                    <h2 class="font-semibold text-base"><c:out value="${offersCardTitle}"/></h2>
                </div>
                <spring:message code="admin.dashboard.offersDesc" var="offersCardDesc"/>
                <p class="text-xs text-black/60 mb-4"><c:out value="${offersCardDesc}"/></p>
                <spring:message code="admin.dashboard.browseOffers" var="browseOffersText"/>
                <paw:linkButton href="${offersUrl}" text="${browseOffersText}" variant="outline" size="sm" icon="arrow-right"/>
            </paw:card>

            <paw:card classname="p-5 flex flex-col justify-between hover:border-black/20 transition">
                <div class="flex items-center gap-3 mb-2">
                    <span class="p-2 rounded-lg bg-purple-100 text-purple-700">
                        <paw:icon name="users"/>
                    </span>
                    <spring:message code="admin.sidebar.users" var="usersCardTitle"/>
                    <h2 class="font-semibold text-base"><c:out value="${usersCardTitle}"/></h2>
                </div>
                <spring:message code="admin.dashboard.usersDesc" var="usersCardDesc"/>
                <p class="text-xs text-black/60 mb-4"><c:out value="${usersCardDesc}"/></p>
                <spring:message code="admin.dashboard.manageUsers" var="manageUsersText"/>
                <paw:linkButton href="${usersUrl}" text="${manageUsersText}" variant="outline" size="sm" icon="arrow-right"/>
            </paw:card>
        </div>

        <%-- Quick Actions Section --%>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <%-- Listing Take Down --%>
            <spring:message code="admin.listings.takedown.title" var="takeDownListingTitle"/>
            <spring:message code="admin.listings.takedown.desc" var="takeDownListingDesc"/>
            <paw:card title="${takeDownListingTitle}" subtitle="${takeDownListingDesc}">
                <form:form method="post" action="${takedownListingUrl}" modelAttribute="takeDownListingForm" class="flex flex-col gap-4 mt-2">
                    <input type="hidden" name="redirect" value="/admin?success=listing_takedown"/>
                    <spring:message code="admin.listings.takedown.idLabel" var="listingIdLabel"/>
                    <spring:message code="admin.listings.takedown.idPlaceholder" var="listingIdPlaceholder"/>
                    <paw:formInput path="listingId" type="number" min="1" label="${listingIdLabel}" placeholder="${listingIdPlaceholder}" variant="outline"/>
                    <spring:message code="admin.listings.takedown.submit" var="submitListingTakedown"/>
                    <paw:button text="${submitListingTakedown}" type="submit" variant="default" role="danger" icon="trash-2" classname="self-end"/>
                </form:form>
            </paw:card>

            <%-- Offer Take Down --%>
            <spring:message code="admin.offers.takedown.title" var="takeDownOfferTitle"/>
            <spring:message code="admin.offers.takedown.desc" var="takeDownOfferDesc"/>
            <paw:card title="${takeDownOfferTitle}" subtitle="${takeDownOfferDesc}">
                <form:form method="post" action="${takedownOfferUrl}" modelAttribute="takeDownOfferForm" class="flex flex-col gap-4 mt-2">
                    <input type="hidden" name="redirect" value="/admin?success=offer_takedown"/>
                    <spring:message code="admin.offers.takedown.idLabel" var="offerIdLabel"/>
                    <spring:message code="admin.offers.takedown.idPlaceholder" var="offerIdPlaceholder"/>
                    <paw:formInput path="offerId" type="number" min="1" label="${offerIdLabel}" placeholder="${offerIdPlaceholder}" variant="outline"/>
                    <spring:message code="admin.offers.takedown.submit" var="submitOfferTakedown"/>
                    <paw:button text="${submitOfferTakedown}" type="submit" variant="default" role="danger" icon="trash-2" classname="self-end"/>
                </form:form>
            </paw:card>

            <%-- User Suspend --%>
            <spring:message code="admin.users.suspend.title" var="suspendUserTitle"/>
            <spring:message code="admin.users.suspend.desc" var="suspendUserDesc"/>
            <paw:card title="${suspendUserTitle}" subtitle="${suspendUserDesc}">
                <form:form method="post" action="${suspendUserUrl}" modelAttribute="suspendUserForm" class="flex flex-col gap-4 mt-2">
                    <spring:message code="admin.users.usernameLabel" var="usernameLabel"/>
                    <spring:message code="admin.users.usernamePlaceholder" var="usernamePlaceholder"/>
                    <paw:formInput path="username" label="${usernameLabel}" placeholder="${usernamePlaceholder}" variant="outline"/>
                    <spring:message code="admin.users.suspend.submit" var="submitUserSuspend"/>
                    <paw:button text="${submitUserSuspend}" type="submit" variant="default" role="danger" icon="user-x" classname="self-end"/>
                </form:form>
            </paw:card>

            <%-- User Grant Admin Role --%>
            <spring:message code="admin.users.grantAdmin.title" var="grantAdminTitle"/>
            <spring:message code="admin.users.grantAdmin.desc" var="grantAdminDesc"/>
            <paw:card title="${grantAdminTitle}" subtitle="${grantAdminDesc}">
                <form:form method="post" action="${grantAdminUrl}" modelAttribute="grantRoleForm" class="flex flex-col gap-4 mt-2">
                    <spring:message code="admin.users.grantAdmin.usernameLabel" var="grantUsernameLabel"/>
                    <spring:message code="admin.users.usernamePlaceholder" var="grantUsernamePlaceholder"/>
                    <paw:formInput path="username" label="${grantUsernameLabel}" placeholder="${grantUsernamePlaceholder}" variant="outline"/>
                    <spring:message code="admin.users.grantAdmin.submit" var="submitGrantAdmin"/>
                    <paw:button text="${submitGrantAdmin}" type="submit" variant="default" role="default" icon="shield" classname="self-end"/>
                </form:form>
            </paw:card>
        </div>
    </div>
</admin:layout>
