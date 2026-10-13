<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<spring:message code="admin.offers.title" var="offersTitle"/>
<spring:message code="admin.offers.subtitle" var="offersSubtitle"/>

<c:url value="/admin/offers" var="offersBaseUrl"/>
<c:url value="/admin/offers/takedown" var="takedownOfferUrl"/>

<admin:layout titleKey="admin.offers.title" title="${offersTitle}" subtitle="${offersSubtitle}">
    <div class="flex flex-col gap-6">
        <%-- Quick Takedown by ID Form --%>
        <spring:message code="admin.offers.takedown.title" var="takedownCardTitle"/>
        <spring:message code="admin.offers.takedown.desc" var="takedownCardDesc"/>
        <paw:card title="${takedownCardTitle}" subtitle="${takedownCardDesc}">
            <form:form method="post" action="${takedownOfferUrl}" modelAttribute="takeDownOfferForm" class="flex flex-col sm:flex-row gap-3 items-end mt-2">
                <input type="hidden" name="redirect" value="/admin/offers?success=offer_takedown"/>
                <div class="flex-1 w-full">
                    <spring:message code="admin.offers.takedown.idLabel" var="offerIdLabel"/>
                    <spring:message code="admin.offers.takedown.idPlaceholder" var="offerIdPlaceholder"/>
                    <paw:formInput path="offerId" type="number" min="1" label="${offerIdLabel}" placeholder="${offerIdPlaceholder}" variant="outline"/>
                </div>
                <spring:message code="admin.offers.takedown.submit" var="submitOfferTakedown"/>
                <paw:button text="${submitOfferTakedown}" type="submit" variant="default" role="danger" icon="trash-2" classname="w-full sm:w-auto"/>
            </form:form>
        </paw:card>

        <%-- Filters --%>
        <paw:card>
            <form method="get" action="${offersBaseUrl}" class="flex flex-col sm:flex-row gap-3 items-end">
                <div class="flex-1 w-full">
                    <spring:message code="admin.offers.statusGroup.label" var="statusGroupLabel"/>
                    <div class="flex flex-col gap-1">
                        <label for="admin-offer-status" class="text-xs text-black/70 font-medium">
                            <c:out value="${statusGroupLabel}"/>
                        </label>
                        <select id="admin-offer-status" name="statusGroup" class="w-full px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2 bg-white">
                            <spring:message code="admin.offers.statusGroup.all" var="allStatusGroupText"/>
                            <spring:message code="admin.offers.statusGroup.pending" var="pendingStatusGroupText"/>
                            <spring:message code="admin.offers.statusGroup.pendingPayment" var="pendingPaymentStatusGroupText"/>
                            <spring:message code="admin.offers.statusGroup.resolved" var="resolvedStatusGroupText"/>
                            <option value="" ${empty currentStatusGroup ? 'selected' : ''}><c:out value="${allStatusGroupText}"/></option>
                            <option value="pending" ${currentStatusGroup eq 'pending' ? 'selected' : ''}><c:out value="${pendingStatusGroupText}"/></option>
                            <option value="pending_payment" ${currentStatusGroup eq 'pending_payment' ? 'selected' : ''}><c:out value="${pendingPaymentStatusGroupText}"/></option>
                            <option value="resolved" ${currentStatusGroup eq 'resolved' ? 'selected' : ''}><c:out value="${resolvedStatusGroupText}"/></option>
                        </select>
                    </div>
                </div>

                <spring:message code="admin.offers.filter.submit" var="submitFilter"/>
                <paw:button text="${submitFilter}" type="submit" variant="default" role="default" icon="filter" classname="w-full sm:w-auto"/>
            </form>
        </paw:card>

        <%-- Offers List --%>
        <c:choose>
            <c:when test="${empty offersPage.content}">
                <paw:card>
                    <div class="py-12 flex flex-col items-center text-center">
                        <span class="p-3 rounded-full bg-neutral-100 text-neutral-400 mb-3">
                            <paw:icon name="handshake"/>
                        </span>
                        <spring:message code="admin.offers.empty" var="emptyOffersMsg"/>
                        <p class="text-black/60 text-base"><c:out value="${emptyOffersMsg}"/></p>
                    </div>
                </paw:card>
            </c:when>

            <c:otherwise>
                <div class="flex flex-col gap-3">
                    <c:forEach var="offer" items="${offersPage.content}">
                        <c:url value="/listing/${offer.listing.id}" var="listingViewUrl"/>
                        <c:url value="/profile/${offer.buyer.id}" var="buyerProfileUrl"/>
                        <c:url value="/profile/${offer.listing.creator.id}" var="sellerProfileUrl"/>
                        <c:url value="/admin/offers/${offer.id}/takedown" var="takedownOfferActionUrl"/>

                        <c:choose>
                            <c:when test="${offer.status.name() == 'PENDING'}">
                                <c:set var="statusBadgeClass" value="text-amber-700 bg-amber-50 border-amber-200"/>
                                <spring:message code="offer.status.PENDING" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${offer.status.name() == 'PENDING_PAYMENT'}">
                                <c:set var="statusBadgeClass" value="text-blue-700 bg-blue-50 border-blue-200"/>
                                <spring:message code="offer.status.PENDING_PAYMENT" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${offer.status.name() == 'ACCEPTED'}">
                                <c:set var="statusBadgeClass" value="text-green-700 bg-green-50 border-green-200"/>
                                <spring:message code="offer.status.ACCEPTED" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${offer.status.name() == 'REJECTED'}">
                                <c:set var="statusBadgeClass" value="text-red-700 bg-red-50 border-red-200"/>
                                <spring:message code="offer.status.REJECTED" var="statusBadgeText"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="statusBadgeClass" value="text-neutral-700 bg-neutral-50 border-neutral-200"/>
                                <spring:message code="offer.status.WITHDRAWN" var="statusBadgeText"/>
                            </c:otherwise>
                        </c:choose>

                        <paw:card classname="p-4">
                            <div class="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
                                <div class="flex flex-col min-w-0">
                                    <div class="flex items-center gap-2 mb-1">
                                        <span class="text-xs font-mono font-medium px-1.5 py-0.5 rounded bg-black/5 text-black/70">
                                            #<c:out value="${offer.id}"/>
                                        </span>
                                        <paw:badge text="${statusBadgeText}" classname="${statusBadgeClass}" size="sm"/>
                                        <span class="text-xs text-black/50 ml-1">
                                            <spring:message code="admin.offers.onListing" var="onListingText"/>
                                            <c:out value="${onListingText}"/>
                                            <a href="${listingViewUrl}" class="hover:underline font-semibold text-black">
                                                #<c:out value="${offer.listing.id}"/> - <c:out value="${offer.listing.title}"/>
                                            </a>
                                        </span>
                                    </div>

                                    <div class="flex items-center gap-4 text-xs text-black/70 mt-1 flex-wrap">
                                        <span>
                                            <spring:message code="admin.offers.buyer" var="buyerLabel"/>
                                            <span class="font-medium text-black/50"><c:out value="${buyerLabel}"/>:</span>
                                            <a href="${buyerProfileUrl}" class="hover:underline font-medium text-black">
                                                <c:out value="${offer.buyer.username}"/>
                                            </a>
                                        </span>
                                        <span>
                                            <spring:message code="admin.offers.seller" var="sellerLabel"/>
                                            <span class="font-medium text-black/50"><c:out value="${sellerLabel}"/>:</span>
                                            <a href="${sellerProfileUrl}" class="hover:underline font-medium text-black">
                                                <c:out value="${offer.listing.creator.username}"/>
                                            </a>
                                        </span>
                                        <span class="font-bold text-sm text-black">
                                            $<c:out value="${offer.amount}"/>
                                        </span>
                                        <c:if test="${offer.offeredListing != null}">
                                            <c:url value="/listing/${offer.offeredListing.id}" var="offeredListingUrl"/>
                                            <span class="inline-flex items-center gap-1 text-purple-700 bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
                                                <paw:icon name="arrow-right-left"/>
                                                <spring:message code="admin.offers.tradeItem" var="tradeItemLabel"/>
                                                <c:out value="${tradeItemLabel}"/>:
                                                <a href="${offeredListingUrl}" class="hover:underline font-medium">
                                                    #<c:out value="${offer.offeredListing.id}"/>
                                                </a>
                                            </span>
                                        </c:if>
                                    </div>

                                    <c:if test="${not empty offer.message}">
                                        <p class="text-xs text-black/60 italic mt-2 bg-black/2 p-2 rounded border border-black/5">
                                            &ldquo;<c:out value="${offer.message}"/>&rdquo;
                                        </p>
                                    </c:if>
                                </div>

                                <div class="flex items-center gap-2 self-end sm:self-center">
                                    <spring:message code="admin.offers.viewListing" var="viewListingBtnText"/>
                                    <paw:linkButton href="${listingViewUrl}" text="${viewListingBtnText}" variant="outline" size="sm" icon="external-link"/>

                                    <c:if test="${offer.status.name() == 'PENDING' || offer.status.name() == 'PENDING_PAYMENT'}">
                                        <form method="post" action="${takedownOfferActionUrl}">
                                            <input type="hidden" name="redirect" value="/admin/offers?page=${offersPage.page}&statusGroup=<c:out value='${currentStatusGroup}'/>"/>
                                            <spring:message code="admin.offers.takedown.btn" var="takeDownOfferBtnText"/>
                                            <paw:button text="${takeDownOfferBtnText}" type="submit" variant="outline" role="danger" size="sm" icon="trash-2"/>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </paw:card>
                    </c:forEach>

                    <%-- Pagination --%>
                    <paw:pagination page="${offersPage}" baseUrl="${offersBaseUrl}"/>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</admin:layout>
