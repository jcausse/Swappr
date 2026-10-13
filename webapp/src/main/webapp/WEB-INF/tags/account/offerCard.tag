<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="offer" required="true" type="ar.edu.itba.paw.model.Offer" %>
<%@ attribute name="user" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<paw:card classname="flex flex-col h-full gap-2">
    <div class="flex items-start justify-between gap-2">
        <h3 class="text-base font-semibold flex-1 min-w-0 truncate">
            <c:url value="/listing/${offer.listing.id}" var="listingUrl"/>
            <a href="${listingUrl}" class="hover:text-lime-600 transition block truncate"><c:out value="${offer.listing.title}"/></a>
        </h3>
        <c:choose>
            <c:when test="${offer.status.name() == 'PENDING'}">
                <c:set var="statusClass" value="text-amber-600"/>
                <spring:message code="offer.status.PENDING" var="statusLabel"/>
            </c:when>
            <c:when test="${offer.status.name() == 'PENDING_PAYMENT'}">
                <c:set var="statusClass" value="text-blue-600"/>
                <spring:message code="offer.status.PENDING_PAYMENT" var="statusLabel"/>
            </c:when>
        </c:choose>
        <paw:badge text="${statusLabel}" classname="ml-auto ${statusClass}" />
        <c:if test="${offer.offeredListing != null}">
            <spring:message code="offer.tradeBadge" var="tradeBadgeLabel"/>
            <paw:badge text="${tradeBadgeLabel}" classname="text-purple-600" />
        </c:if>
    </div>

    <div class="flex gap-3 mt-2">
        <paw:listingImage listing="${offer.listing}" />

        <div class="flex-1 min-w-0 flex flex-col justify-between">
            <paw:product product="${offer.listing.product}" size="sm" />

            <div class="mt-2 flex flex-row gap-4 items-end">
                <div class="flex-1">
                    <c:if test="${offer.offeredListing != null}">
                        <c:url value="/listing/${offer.offeredListing.id}" var="offeredListingUrl"/>
                        <spring:message code="offer.detail.offeredItem" var="offeredItemLabel"/>
                        <p class="text-black/60 text-xs font-semibold"><c:out value="${offeredItemLabel}"/></p>
                        <div class="flex flex-row items-end gap-2">
                            <paw:linkButton variant="ghost" href="${offeredListingUrl}" size="sm" classname="p-1! flex-1">
                                <paw:listingImage listing="${offer.offeredListing}" size="sm" />
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium text-black truncate"><c:out value="${offer.offeredListing.title}"/></p>
                                    <p class="text-xs text-black/60 font-normal">
                                        <c:out value="${offer.offeredListing.product.brand}"/>
                                        <c:out value="${offer.offeredListing.product.model}"/>
                                        (<c:out value="${offer.offeredListing.product.year}"/>)
                                    </p>
                                    <p class="text-xs text-black/60 font-normal">$<c:out value="${offer.offeredListing.price.amount}"/></p>
                                </div>
                            </paw:linkButton>
                            <c:if test="${offer.amount != null and offer.amount > 0.0}">
                                <spring:message code="offer.tradePlusAmount" arguments="${offer.amount}" var="plusAmountLabel"/>
                                <span class="text-2xl font-bold"><c:out value="${plusAmountLabel}"/></span>
                            </c:if>
                        </div>
                    </c:if>
                    <c:if test="${offer.offeredListing == null}">
                        <c:if test="${not offer.isFullPrice}">
                            <p class="text-base font-medium line-through text-black/60"> $<c:out value="${offer.listing.price.amount}"/> </p>
                        </c:if>
                        <p class="text-2xl font-bold">
                            $<c:out value="${offer.amount}"/>
                            <c:if test="${not offer.isFullPrice}">
                                 <c:set var="listingPrice" value="${offer.listing.price.amount}"/>
                                 <c:set var="offerAmount" value="${offer.amount}"/>
                                 <c:set var="discountPercent" value="${((listingPrice - offerAmount) / listingPrice) * 100}"/>
                                 <span class="text-red-600 text-lg"> -<fmt:formatNumber value="${discountPercent}" maxFractionDigits="0"/>%</span>
                             </c:if>
                        </p>
                    </c:if>
                </div>

                <div class="max-w-48 ml-auto">
                    <c:choose>
                        <c:when test="${user == 'seller'}">
                            <paw:user user="${offer.listing.creator}" variant="compact" />
                        </c:when>
                        <c:when test="${user == 'buyer'}">
                            <paw:user user="${offer.buyer}" variant="compact" />
                        </c:when>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="flex flex-row gap-1 self-end">
            <jsp:doBody />
        </div>
    </div>

    <c:if test="${offer.hasOtherOffers && user == 'buyer'}">
        <spring:message code="${offer.hasBetterOffers ? 'offer.warning.betterOffers' : 'offer.warning.otherOffers'}" var="warningMsg"/>
        <paw:banner text="${warningMsg}" icon="triangle-alert" role="warning" />
    </c:if>

    <c:if test="${offer.status.name() == 'PENDING_PAYMENT'}">
        <c:choose>
            <c:when test="${user == 'buyer'}">
                <spring:message code="offer.contactInfo.buyerEmail" var="buyerEmailLabel"/>
                <paw:banner role="secondary" icon="contact-round">
                    <div class="text-sm">
                        <c:out value="${buyerEmailLabel}:" />
                        <c:url value="mailto:${offer.buyer.email}" var="buyerEmailUrl" />
                        <a class="hover:text-lime-600 underline cursor-pointer" href="${buyerEmailUrl}"><c:out value="${offer.buyer.email}" /></a>
                    </div>
                </paw:banner>
            </c:when>
            <c:when test="${user == 'seller'}">
                <spring:message code="offer.contactInfo.sellerEmail" var="sellerEmailLabel"/>
                <paw:banner role="secondary" icon="contact-round">
                    <div class="text-sm">
                        <c:out value="${sellerEmailLabel}:" />
                        <c:url value="mailto:${offer.listing.creator.email}" var="sellerEmailUrl" />
                        <a class="hover:text-lime-600 underline cursor-pointer" href="${sellerEmailUrl}"><c:out value="${offer.listing.creator.email}" /></a>
                    </div>
                </paw:banner>
            </c:when>
        </c:choose>
    </c:if>

    <c:if test="${offer.proofOfPaymentId != null && user == 'buyer'}">
        <spring:message code="offer.proofOfPayment.uploadedNotification" var="proofOfPaymentMsg"/>
        <paw:banner text="${proofOfPaymentMsg}" icon="badge-check" />
    </c:if>
    <c:if test="${(offer.proofOfShippingId != null || not empty offer.trackingNumber) && user == 'seller'}">
        <spring:message code="offer.proofOfShipping.uploadedNotification" var="proofOfShippingMsg"/>
        <paw:banner text="${proofOfShippingMsg}" icon="badge-check" />
    </c:if>

    <c:if test="${offer.proofOfPaymentId != null || offer.proofOfShippingId != null || not empty offer.trackingNumber}">
        <spring:message code="offer.attachments" var="attachmentsLabel"/>
        <paw:collapsible title="${attachmentsLabel}" classname="flex-1">
            <c:if test="${offer.proofOfPaymentId != null}">
                <paw:offerProofOfPayment offer="${offer}" />
            </c:if>
            <c:if test="${offer.proofOfShippingId != null || not empty offer.trackingNumber}">
                <paw:offerProofOfShipping offer="${offer}" />
            </c:if>
        </paw:collapsible>
    </c:if>

    <c:if test="${not empty offer.message}">
        <paw:divider />
        <spring:message code="offer.detail.message" var="messageLabel"/>
        <paw:collapsible title="${messageLabel}">
            <p class="text-sm whitespace-pre-wrap"><c:out value="${offer.message}"/></p>
        </paw:collapsible>
    </c:if>
</paw:card>
