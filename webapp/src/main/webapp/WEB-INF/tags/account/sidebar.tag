<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="user" required="true" type="ar.edu.itba.paw.model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>

<c:url value="/account/listings" var="listingsUrl"/>
<c:url value="/account/incoming-offers" var="offersUrl"/>
<c:url value="/account/my-offers" var="myOffersUrl"/>
<c:url value="/notifications" var="notificationsUrl"/>

<spring:message code="account.myListings" var="myListingsTitle"/>
<spring:message code="account.incomingOffers" var="incomingOffersTitle"/>
<spring:message code="account.myOffers" var="myOffersTitle"/>
<spring:message code="account.notifications" var="notificationsTitle"/>

<spring:message code="account.title" var="accountTitle"/>
<paw:card title="${accountTitle}" classname="sticky top-24">
    <div class="flex flex-col gap-2 mt-4">
        <paw:user user="${user}" variant="detailed" href="/account" classname="-mx-2 w-[calc(100%+1rem)]!" />

        <div class="flex flex-col gap-1">
            <account:link href="${notificationsUrl}" text="${notificationsTitle}" icon="bell">
                <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
                    <paw:badge text="${recentUnreadNotificationsPage.totalCount}" classname="text-red-600 min-w-5! p-0.25!" size="sm" />
                </c:if>
            </account:link>
        </div>

        <paw:divider />

        <spring:message code="account.sidebar.buyer" var="buyerSubtitle"/>
        <h4 class="font-semibold text-sm mt-2"><c:out value="${buyerSubtitle}" /></h4>
        <div class="flex flex-col gap-1">
            <account:link href="${myOffersUrl}" text="${myOffersTitle}" icon="handshake" />
        </div>

        <paw:divider />

        <spring:message code="account.sidebar.seller" var="sellerSubtitle"/>
        <h4 class="font-semibold text-sm mt-2"><c:out value="${sellerSubtitle}" /></h4>
        <div class="flex flex-col gap-1">
            <account:link href="${listingsUrl}" text="${myListingsTitle}" icon="store" />
            <account:link href="${offersUrl}" text="${incomingOffersTitle}" icon="handshake">
                <c:if test="${pendingOffersCount > 0}">
                    <paw:badge text="${pendingOffersCount}" classname="text-red-600 min-w-5! p-0.25!" size="sm" />
                </c:if>
            </account:link>
        </div>
    </div>
</paw:card>
