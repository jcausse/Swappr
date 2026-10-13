<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<spring:message code="listing.detail.canceled" var="canceledMsg"/>
<spring:message code="listing.detail.sold" var="soldMsg"/>

<paw:layout titleKey="listing.detail.title">
    <c:if test="${isCanceled}">
        <paw:banner text="${canceledMsg}" icon="circle-alert" role="danger" classname="mb-4" />
    </c:if>
    <c:if test="${isSold}">
        <paw:banner text="${soldMsg}" icon="info" role="secondary" classname="mb-4" />
    </c:if>

    <div class="flex flex-row gap-4">
        <div class="flex-2 min-w-0">
            <paw:card classname="relative">
                <c:set var="imageUrlsList">
                    <c:forEach items="${listing.imageIds}" var="imageId" varStatus="status">
                        <c:url value="/image/${imageId}" var="imageUrl"/>
                        <c:out value="${imageUrl}"/>
                        <c:if test="${not status.last}">,</c:if>
                    </c:forEach>
                </c:set>
                <paw:imageGallery id="listing-gallery" images="${fn:split(imageUrlsList, ',')}" alt="${listing.title}" />
                <paw:listingHotBadge listing="${listing}" />
            </paw:card>
        </div>

        <div class="flex-1 min-w-sm relative">
            <paw:card classname="sticky top-26">
                <div class="flex flex-col gap-4">
                    <h1 class="text-2xl font-semibold"><c:out value="${listing.title}"/></h1>
                    <paw:product product="${listing.product}" />

                    <spring:message code="condition.${listing.condition}" var="conditionLabel"/>
                    <spring:message code="condition.description.${listing.condition}" var="conditionDescription"/>
                    <spring:message code="listing.detail.condition" var="conditionTitle"/>
                    <paw:collapsible title="${conditionTitle} ${conditionLabel}" classname="w-full">
                        <p class="text-sm text-black/70"><c:out value="${conditionDescription}"/></p>
                    </paw:collapsible>

                    <paw:divider />

                    <paw:user user="${listing.creator}" showSellerRating="true" />

                    <p class="text-sm text-black/60 flex items-center gap-1">
                        <paw:icon name="map-pin" />
                        <c:choose>
                            <c:when test="${listing.creator.province.present}">
                                <spring:message code="province.${listing.creator.province.get().name}" var="sellerProvinceLabel"/>
                                <c:out value="${sellerProvinceLabel}"/><c:if test="${listing.creator.locationDetail.present}">, <c:out value="${listing.creator.locationDetail.get()}"/></c:if>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="profile.location.notSpecified"/>
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <p class="text-3xl font-bold">$<c:out value="${listing.price.getAmount()}"/></p>

                    <c:choose>
                        <c:when test="${isCanceled}">
                            <paw:banner text="${canceledMsg}" icon="circle-alert" role="danger" classname="mb-4" />
                        </c:when>
                        <c:when test="${isSold}">
                            <paw:banner text="${soldMsg}" icon="info" role="secondary" classname="mb-4" />
                        </c:when>
                        <c:when test="${isCreator}">
                            <spring:message code="listing.detail.edit" var="editLabel"/>
                            <c:url value="/listing/${listing.id}/edit" var="editUrl"/>
                            <paw:linkButton href="${editUrl}" size="lg" classname="w-full" variant="outline" text="${editLabel}"/>

                            <spring:message code="listing.detail.cancel" var="cancelLabel"/>
                            <paw:button text="${cancelLabel}" variant="ghost" role="danger" onclick="document.getElementById('cancelListingDialog').showModal()" />

                            <spring:message code="listing.cancel.confirm.title" var="cancelTitle"/>
                            <spring:message code="listing.cancel.confirm.confirm" var="cancelConfirm"/>
                            <spring:message code="listing.cancel.confirm.cancel" var="cancelCancel"/>
                            <spring:message code="listing.cancel.confirm.message" var="cancelMessage"/>
                            <c:url value="/listing/${listing.id}/cancel" var="cancelUrl"/>
                            <paw:confirmDialog id="cancelListingDialog" title="${cancelTitle}" confirmText="${cancelConfirm}"
                                                cancelText="${cancelCancel}" formAction="${cancelUrl}">
                                <c:out value="${cancelMessage}"/>
                            </paw:confirmDialog>
                        </c:when>
                        <c:when test="${userPendingOffer.isPresent()}">
                            <div class="flex flex-col gap-2">
                                <spring:message code="listing.detail.pendingOffer" arguments="${userPendingOffer.get().amount}" var="pendingOfferMsg"/>
                                <p class="text-black/60 text-sm"><c:out value="${pendingOfferMsg}"/></p>
                                <form action="<c:url value='/offer/${userPendingOffer.get().id}/withdraw'/>" method="POST">
                                    <spring:message code="account.myOffers.withdraw" var="withdrawLabel"/>
                                    <paw:button type="submit" variant="outline" role="danger" classname="w-full" icon="x" text="${withdrawLabel}" />
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="flex flex-col gap-2">
                                <c:if test="${listing.pendingOffersCount > 0}">
                                    <c:choose>
                                        <c:when test="${listing.pendingOffersCount > 1}">
                                            <spring:message code="listing.detail.hotItem" arguments="${listing.pendingOffersCount}" var="hotItemMsg"/>
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message code="listing.detail.hotItem1" arguments="${listing.pendingOffersCount}" var="hotItemMsg"/>
                                        </c:otherwise>
                                    </c:choose>
                                    <p class="text-red-600 text-sm"><c:out value="${hotItemMsg}"/></p>
                                </c:if>
                                <c:if test="${listing.acceptsTrade}">
                                    <spring:message code="listing.detail.acceptsTrade" var="acceptsTradeMsg"/>
                                    <p class="text-lime-600 text-sm flex items-center gap-2">
                                        <span class="text-sm text-lime-600 bg-lime-50 border border-lime-200 rounded-full w-5 h-5 flex items-center justify-center shrink-0">
                                            <paw:icon name="arrow-right-left" />
                                        </span>
                                        <c:out value="${acceptsTradeMsg}"/>
                                    </p>
                                </c:if>
                                <c:choose>
                                    <c:when test="${listing.acceptsShipping}">
                                        <spring:message code="listing.detail.ships" var="shippingMsg"/>
                                        <p class="text-lime-600 text-sm flex items-center gap-2">
                                            <span class="text-sm text-lime-600 bg-lime-50 border border-lime-200 rounded-full w-5 h-5 flex items-center justify-center shrink-0">
                                                <paw:icon name="truck" />
                                            </span>
                                            <c:out value="${shippingMsg}"/>
                                        </p>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="listing.detail.doesNotShip" var="shippingMsg"/>
                                        <p class="text-black/50 text-sm flex items-center gap-2">
                                            <span class="text-sm text-black/40 bg-black/5 border border-black/10 rounded-full w-5 h-5 flex items-center justify-center shrink-0">
                                                <paw:icon name="truck" />
                                            </span>
                                            <c:out value="${shippingMsg}"/>
                                        </p>
                                    </c:otherwise>
                                </c:choose>
                                <spring:message code="listing.detail.makeOffer" var="makeOfferLabel"/>
                                <c:url value="/checkout?listingId=${listing.id}" var="checkoutUrl"/>
                                <paw:linkButton href="${checkoutUrl}" size="lg" classname="w-full" text="${makeOfferLabel}"/>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <paw:divider />

                    <c:url value="/listing/new/choose-product?productId=${listing.product.getId()}" var="sellSameProductUrl" />
                    <spring:message code="listing.detail.sellSameProduct" var="sellSameProductLabel" />
                    <paw:linkButton href="${sellSameProductUrl}" variant="ghost" size="sm" role="secondary" text="${sellSameProductLabel}" />
                </div>
            </paw:card>
        </div>
    </div>

    <c:if test="${not empty listing.description}">
        <spring:message code="listing.detail.description" var="descriptionLabel"/>
        <paw:card classname="mt-8">
            <h2 class="text-xl font-semibold mb-4"><c:out value="${descriptionLabel}"/></h2>
            <p class="whitespace-pre-wrap text-black/70"><c:out value="${listing.description}"/></p>
        </paw:card>
    </c:if>
</paw:layout>
