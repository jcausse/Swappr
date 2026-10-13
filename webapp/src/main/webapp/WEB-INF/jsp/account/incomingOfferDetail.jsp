<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>


<c:url value="/account/incoming-offers" var="incomingOffersUrl"/>
<spring:message code="account.incomingOffers.title" var="titleMsg"/>
<spring:message code="account.incomingOffers.subtitle" var="subtitleMsg"/>

<account:layout titleKey="offer.detail.title" title="${titleMsg}" subtitle="${subtitleMsg}">
    <!-- Back link -->
    <div class="mb-4">
        <spring:message code="offer.detail.back" var="backLabel"/>
        <paw:linkButton href="${incomingOffersUrl}" text="${backLabel}" variant="ghost" icon="chevron-left" classname="justify-start" />
    </div>

    <paw:card classname="flex flex-col gap-4">
        <div class="text-center">
            <c:set var="offerAmount" value="${offer.amount}"/>
            <c:set var="tradeAndAmount" value=""/>
            <c:if test="${offerAmount != null and offerAmount > 0.0}">
                <spring:message var="tradeAndAmount" code="offer.detail.tradeAndAmount" arguments="${offerAmount}"/>
            </c:if>

            <c:choose>
                <c:when test="${offer.offeredListing != null}">
                    <spring:message code="offer.detail.title.trade"
                                    arguments="${offer.buyer.displayName},${offer.offeredListing.product.brand},${offer.offeredListing.product.model},${offer.offeredListing.product.year},${tradeAndAmount},${offer.listing.product.brand},${offer.listing.product.model},${offer.listing.product.year}"
                                    var="title"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="offer.detail.title"
                                    arguments="${offer.buyer.displayName},${offer.amount},${offer.listing.product.brand},${offer.listing.product.model},${offer.listing.product.year}"
                                    var="title"/>
                </c:otherwise>
            </c:choose>
            <h1 class="text-xl font-medium text-balance"><c:out value="${title}"/></h1>

            <c:choose>
                <c:when test="${offer.status.name() == 'PENDING'}">
                    <spring:message code="offer.detail.description.pending" var="description"/>
                </c:when>
                <c:when test="${offer.status.name() == 'PENDING_PAYMENT'}">
                    <spring:message code="offer.detail.description.pendingPayment" var="description"/>
                </c:when>
                <c:when test="${offer.status.name() == 'ACCEPTED'}">
                    <spring:message code="offer.detail.description.accepted" var="description"/>
                </c:when>
                <c:when test="${offer.status.name() == 'REJECTED'}">
                    <spring:message code="offer.detail.description.rejected" var="description"/>
                </c:when>
                <c:when test="${offer.status.name() == 'WITHDRAWN'}">
                    <spring:message code="offer.detail.description.withdrawn" var="description"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="offer.detail.description" var="description"/>
                </c:otherwise>
            </c:choose>
            <p class="text-black/60 mt-2 text-balance"><c:out value="${description}"/></p>
        </div>

        <c:if test="${offer.hasOtherOffers and offer.status.name() == 'PENDING'}">
            <div class="p-3 rounded-lg bg-amber-50 border border-amber-200 mt-2 flex flex-row gap-2 text-amber-800 items-center">
                <paw:icon name="triangle-alert" />
                <spring:message code="${offer.hasBetterOffers ? 'offer.warning.betterOffers' : 'offer.warning.otherOffers'}" var="warningMsg"/>
                <p class="text-sm"><c:out value="${warningMsg}"/></p>
            </div>
        </c:if>

        <paw:divider />

        <!-- Listing info section -->
        <div class="flex flex-row gap-4">
            <paw:listingImage listing="${offer.listing}" size="xl" />

            <div class="flex-1 min-w-0 flex flex-col justify-center">
                <h2 class="text-xl font-semibold"><c:out value="${offer.listing.title}"/></h2>
                <p class="text-sm text-black/60 mt-1">
                    <c:out value="${offer.listing.product.brand}"/>
                    <c:out value="${offer.listing.product.model}"/>
                    (<c:out value="${offer.listing.product.year}"/>)
                </p>
            </div>
        </div>

        <div class="flex flex-col gap-4">
            <paw:user user="${offer.buyer}" />
        </div>

        <div class="flex flex-col">
            <c:if test="${offer.offeredListing != null}">
                <c:url value="/listing/${offer.offeredListing.id}" var="offeredListingUrl"/>
                <spring:message code="offer.detail.offeredItem" var="offeredItemLabel"/>
                <p class="text-black/60 text-sm font-semibold mb-1"><c:out value="${offeredItemLabel}"/></p>
                <div class="flex flex-row gap-4">
                    <paw:listingImage listing="${offer.offeredListing}" size="xl" />
                    <div class="flex-1 min-w-0 flex flex-col justify-center">
                        <a href="${offeredListingUrl}" class="text-xl font-semibold hover:text-lime-600 transition">
                            <c:out value="${offer.offeredListing.title}"/>
                        </a>
                        <p class="text-sm text-black/60 mt-1">
                            <c:out value="${offer.offeredListing.product.brand}"/>
                            <c:out value="${offer.offeredListing.product.model}"/>
                            (<c:out value="${offer.offeredListing.product.year}"/>)
                        </p>
                        <p class="text-sm text-black/60 mt-1">
                            $<c:out value="${offer.offeredListing.price.amount}"/>
                        </p>
                    </div>
                </div>
                <c:if test="${offer.amount != null and offer.amount > 0.0}">
                    <spring:message code="offer.tradePlusAmount" arguments="${offer.amount}" var="plusAmountLabel"/>
                    <span class="text-2xl text-end font-bold mt-2"><c:out value="${plusAmountLabel}"/></span>
                </c:if>
            </c:if>
            <c:if test="${offer.offeredListing == null}">
                <c:if test="${not offer.isFullPrice}">
                    <p class="text-lg font-medium line-through text-black/60">
                        $<c:out value="${offer.listing.price.getAmount()}"/>
                    </p>
                </c:if>
                <p class="text-3xl font-bold">
                    $<c:out value="${offer.amount}"/>
                    <c:if test="${not offer.isFullPrice}">
                        <c:set var="listingPrice" value="${offer.listing.price.getAmount()}"/>
                        <c:set var="offerAmount" value="${offer.amount}"/>
                        <c:set var="discountPercent" value="${((listingPrice - offerAmount) / listingPrice) * 100}"/>
                        <span class="text-red-600 text-xl"> -<fmt:formatNumber value="${discountPercent}" maxFractionDigits="0"/>%</span>
                    </c:if>
                </p>
            </c:if>
        </div>

        <c:if test="${not empty offer.message}">
            <paw:divider />
            <spring:message code="offer.detail.message" var="messageLabel"/>
            <div class="flex flex-col">
                <span class="text-sm text-black/60"><c:out value="${messageLabel}"/></span>
                <p class="text-black/90 whitespace-pre-wrap"><c:out value="${offer.message}"/></p>
            </div>
        </c:if>

        <!-- Proof of payment/shipping for pending payment state -->
        <c:if test="${offer.status.name() == 'PENDING_PAYMENT' and (offer.proofOfPaymentId != null or offer.proofOfShippingId != null or not empty offer.trackingNumber)}">
            <paw:divider />
            <spring:message code="offer.detail.proofOfPayment" var="proofLabel"/>
            <div class="flex flex-col gap-2">
                <span class="text-sm text-black/60"><c:out value="${proofLabel}"/></span>
                <c:if test="${offer.proofOfPaymentId != null}">
                    <paw:offerProofOfPayment offer="${offer}" />
                </c:if>
                <c:if test="${offer.proofOfShippingId != null or not empty offer.trackingNumber}">
                    <paw:offerProofOfShipping offer="${offer}" />
                </c:if>
            </div>
        </c:if>

        <paw:divider />

        <c:choose>
            <c:when test="${offer.status.name() == 'PENDING'}">
                <div class="flex flex-row gap-4">
                    <spring:message code="offer.detail.accept" var="acceptLabel"/>
                    <form action="<c:url value='/offer/${offer.id}/accept'/>" method="POST" class="flex-1">
                        <paw:button type="submit" variant="default" size="lg" classname="w-full" text="${acceptLabel}"/>
                    </form>

                    <spring:message code="offer.detail.reject" var="rejectLabel"/>
                    <form action="<c:url value='/offer/${offer.id}/reject'/>" method="POST" class="flex-1">
                        <paw:button type="submit" variant="default" role="danger" size="lg" classname="w-full" text="${rejectLabel}"/>
                    </form>
                </div>
            </c:when>
            <c:when test="${offer.status.name() == 'PENDING_PAYMENT'}">
                <spring:message code="offer.detail.confirmPayment" var="confirmLabel"/>
                <form action="<c:url value='/offer/${offer.id}/confirm-payment'/>" method="POST" class="w-full mb-2">
                    <paw:button type="submit" variant="default" size="lg" classname="w-full" text="${confirmLabel}"/>
                </form>
                <spring:message code="offer.detail.reject" var="rejectLabel"/>
                <form action="<c:url value='/offer/${offer.id}/reject'/>" method="POST" class="w-full">
                    <paw:button type="submit" variant="ghost" role="danger" size="md" classname="w-full" text="${rejectLabel}"/>
                </form>
            </c:when>
            <c:when test="${offer.status.name() == 'ACCEPTED'}">
                <spring:message code="offer.detail.accepted" var="acceptedMsg"/>
                <p class="text-center text-lg font-medium text-lime-700"><c:out value="${acceptedMsg}"/></p>
            </c:when>
            <c:when test="${offer.status.name() == 'REJECTED'}">
                <spring:message code="offer.detail.rejected" var="rejectedMsg"/>
                <p class="text-center text-lg font-medium text-red-700"><c:out value="${rejectedMsg}"/></p>
            </c:when>
            <c:when test="${offer.status.name() == 'WITHDRAWN'}">
                <spring:message code="offer.detail.withdrawn" var="withdrawnMsg"/>
                <p class="text-center text-lg font-medium text-stone-700"><c:out value="${withdrawnMsg}"/></p>
            </c:when>
            <c:otherwise>
                <div class="flex flex-row gap-4">
                    <spring:message code="offer.detail.accept" var="acceptLabel"/>
                    <form action="<c:url value='/offer/${offer.id}/accept'/>" method="POST" class="flex-1">
                        <paw:button type="submit" variant="default" size="lg" classname="w-full" text="${acceptLabel}"/>
                    </form>

                    <spring:message code="offer.detail.reject" var="rejectLabel"/>
                    <form action="<c:url value='/offer/${offer.id}/reject'/>" method="POST" class="flex-1">
                        <paw:button type="submit" variant="default" role="danger" size="lg" classname="w-full" text="${rejectLabel}"/>
                    </form>
                </div>
            </c:otherwise>
        </c:choose>
    </paw:card>
</account:layout>
