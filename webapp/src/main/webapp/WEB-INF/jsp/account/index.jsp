<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>


<spring:message code="account.welcome" arguments="${currentUser.get().displayName}" var="welcomeMsg"/>
<spring:message code="account.subtitle" var="subtitleMsg"/>

<c:url value="/account/listings" var="listingsUrl"/>
<c:url value="/account/incoming-offers" var="offersUrl"/>
<c:url value="/account/my-offers" var="myOffersUrl"/>

<spring:message code="account.myListings" var="myListingsTitle"/>
<spring:message code="account.incomingOffers" var="incomingOffersTitle"/>
<spring:message code="account.myOffers" var="myOffersTitle"/>

<account:layout titleKey="account.title" title="${welcomeMsg}" subtitle="${subtitleMsg}">
    <spring:message code="account.sidebar.buyer" var="buyerSubtitle"/>
    <paw:card title="${buyerSubtitle}">
        <div class="flex flex-col">
            <paw:linkButton variant="ghost" size="lg" href="${myOffersUrl}">
                <div class="grow flex flex-col gap-2">
                    <h3 class="text-black font-semibold"><c:out value="${myOffersTitle}"/></h3>
                    <spring:message code="account.myOffers.desc" var="myOffersDesc"/>
                    <p class="text-black/60 font-normal text-sm"><c:out value="${myOffersDesc}"/></p>
                </div>
                <paw:icon name="chevron-right" classname="text-xl" />
            </paw:linkButton>
        </div>
    </paw:card>

    <spring:message code="account.sidebar.seller" var="sellerSubtitle"/>
    <paw:card title="${sellerSubtitle}" classname="mt-6">
        <div class="flex flex-col">
            <paw:linkButton variant="ghost" size="lg" href="${listingsUrl}">
                <div class="grow flex flex-col gap-2">
                    <h3 class="text-black font-semibold"><c:out value="${myListingsTitle}"/></h3>
                    <spring:message code="account.myListings.desc" var="listingsDesc"/>
                    <p class="text-black/60 font-normal text-sm"><c:out value="${listingsDesc}"/></p>
                </div>
                <paw:icon name="chevron-right" classname="text-xl" />
            </paw:linkButton>

            <paw:linkButton variant="ghost" size="lg" href="${offersUrl}">
                <div class="grow flex flex-col gap-2">
                    <div class="flex items-center gap-2">
                        <h3 class="text-black font-semibold">
                            <c:out value="${incomingOffersTitle}"/>
                        </h3>
                        <c:if test="${pendingOffersCount > 0}">
                            <paw:badge text="${pendingOffersCount}" classname="text-red-600 min-w-5! p-0.25!" size="sm" />
                        </c:if>
                    </div>
                    <spring:message code="account.incomingOffers.desc" var="offersDesc"/>
                    <p class="text-black/60 font-normal text-sm"><c:out value="${offersDesc}"/></p>
                </div>
                <paw:icon name="chevron-right" classname="text-xl" />
            </paw:linkButton>
        </div>
    </paw:card>
</account:layout>
