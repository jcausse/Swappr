<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<spring:message code="admin.listings.title" var="listingsTitle"/>
<spring:message code="admin.listings.subtitle" var="listingsSubtitle"/>

<c:url value="/admin/listings" var="listingsBaseUrl"/>
<c:url value="/admin/listings/takedown" var="takedownListingUrl"/>

<admin:layout titleKey="admin.listings.title" title="${listingsTitle}" subtitle="${listingsSubtitle}">
    <div class="flex flex-col gap-6">
        <%-- Quick Takedown by ID Form --%>
        <spring:message code="admin.listings.takedown.title" var="takedownCardTitle"/>
        <spring:message code="admin.listings.takedown.desc" var="takedownCardDesc"/>
        <paw:card title="${takedownCardTitle}" subtitle="${takedownCardDesc}">
            <form:form method="post" action="${takedownListingUrl}" modelAttribute="takeDownListingForm" class="flex flex-col sm:flex-row gap-3 items-end mt-2">
                <input type="hidden" name="redirect" value="/admin/listings?success=listing_takedown"/>
                <div class="flex-1 w-full">
                    <spring:message code="admin.listings.takedown.idLabel" var="listingIdLabel"/>
                    <spring:message code="admin.listings.takedown.idPlaceholder" var="listingIdPlaceholder"/>
                    <paw:formInput path="listingId" type="number" min="1" label="${listingIdLabel}" placeholder="${listingIdPlaceholder}" variant="outline"/>
                </div>
                <spring:message code="admin.listings.takedown.submit" var="submitListingTakedown"/>
                <paw:button text="${submitListingTakedown}" type="submit" variant="default" role="danger" icon="trash-2" classname="w-full sm:w-auto"/>
            </form:form>
        </paw:card>

        <%-- Search and Filters --%>
        <paw:card>
            <form method="get" action="${listingsBaseUrl}" class="flex flex-col sm:flex-row gap-3 items-end">
                <div class="flex-1 w-full">
                    <spring:message code="admin.listings.search.label" var="searchLabel"/>
                    <spring:message code="admin.listings.search.placeholder" var="searchPlaceholder"/>
                    <div class="flex flex-col gap-1">
                        <label for="admin-listing-search" class="text-xs text-black/70 font-medium">
                            <c:out value="${searchLabel}"/>
                        </label>
                        <input id="admin-listing-search" name="query" type="text" value="<c:out value='${currentQuery}'/>" placeholder="${searchPlaceholder}"
                               class="w-full px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2"/>
                    </div>
                </div>

                <div class="w-full sm:w-48">
                    <spring:message code="admin.listings.status.label" var="statusFilterLabel"/>
                    <div class="flex flex-col gap-1">
                        <label for="admin-listing-status" class="text-xs text-black/70 font-medium">
                            <c:out value="${statusFilterLabel}"/>
                        </label>
                        <select id="admin-listing-status" name="status" class="w-full px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2 bg-white">
                            <spring:message code="admin.listings.status.all" var="allStatusText"/>
                            <spring:message code="listing.status.ACTIVE" var="statusActiveText"/>
                            <spring:message code="listing.status.SOLD" var="statusSoldText"/>
                            <spring:message code="listing.status.PENDING_TRANSACTION" var="statusPendingTxText"/>
                            <spring:message code="listing.status.CANCELED" var="statusCanceledText"/>
                            <option value="" ${empty currentStatus ? 'selected' : ''}><c:out value="${allStatusText}"/></option>
                            <option value="ACTIVE" ${currentStatus eq 'ACTIVE' ? 'selected' : ''}><c:out value="${statusActiveText}"/></option>
                            <option value="SOLD" ${currentStatus eq 'SOLD' ? 'selected' : ''}><c:out value="${statusSoldText}"/></option>
                            <option value="PENDING_TRANSACTION" ${currentStatus eq 'PENDING_TRANSACTION' ? 'selected' : ''}><c:out value="${statusPendingTxText}"/></option>
                            <option value="CANCELED" ${currentStatus eq 'CANCELED' ? 'selected' : ''}><c:out value="${statusCanceledText}"/></option>
                        </select>
                    </div>
                </div>

                <spring:message code="admin.listings.search.submit" var="submitSearch"/>
                <paw:button text="${submitSearch}" type="submit" variant="default" role="default" icon="search" classname="w-full sm:w-auto"/>
            </form>
        </paw:card>

        <%-- Listings List --%>
        <c:choose>
            <c:when test="${empty listingsPage.content}">
                <paw:card>
                    <div class="py-12 flex flex-col items-center text-center">
                        <span class="p-3 rounded-full bg-neutral-100 text-neutral-400 mb-3">
                            <paw:icon name="package-search"/>
                        </span>
                        <spring:message code="admin.listings.empty" var="emptyListingsMsg"/>
                        <p class="text-black/60 text-base"><c:out value="${emptyListingsMsg}"/></p>
                    </div>
                </paw:card>
            </c:when>

            <c:otherwise>
                <div class="flex flex-col gap-3">
                    <c:forEach var="listing" items="${listingsPage.content}">
                        <c:url value="/listing/${listing.id}" var="listingViewUrl"/>
                        <c:url value="/profile/${listing.creator.id}" var="creatorProfileUrl"/>
                        <c:url value="/admin/listings/${listing.id}/takedown" var="takedownActionUrl"/>

                        <c:choose>
                            <c:when test="${listing.status.name() == 'ACTIVE'}">
                                <c:set var="statusBadgeClass" value="text-green-600 bg-green-50 border-green-200"/>
                                <spring:message code="listing.status.ACTIVE" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${listing.status.name() == 'SOLD'}">
                                <c:set var="statusBadgeClass" value="text-neutral-600 bg-neutral-50 border-neutral-200"/>
                                <spring:message code="listing.status.SOLD" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${listing.status.name() == 'PENDING_TRANSACTION'}">
                                <c:set var="statusBadgeClass" value="text-blue-600 bg-blue-50 border-blue-200"/>
                                <spring:message code="listing.status.PENDING_TRANSACTION" var="statusBadgeText"/>
                            </c:when>
                            <c:when test="${listing.status.name() == 'CANCELED'}">
                                <c:set var="statusBadgeClass" value="text-red-600 bg-red-50 border-red-200"/>
                                <spring:message code="listing.status.CANCELED" var="statusBadgeText"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="statusBadgeClass" value="text-yellow-600 bg-yellow-50 border-yellow-200"/>
                                <c:set var="statusBadgeText" value="${listing.status.name()}"/>
                            </c:otherwise>
                        </c:choose>

                        <paw:card classname="p-4">
                            <div class="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
                                <div class="flex gap-3 items-center min-w-0">
                                    <paw:listingImage listing="${listing}" size="sm"/>
                                    <div class="flex flex-col min-w-0">
                                        <div class="flex items-center gap-2 mb-1">
                                            <span class="text-xs font-mono font-medium px-1.5 py-0.5 rounded bg-black/5 text-black/70">
                                                #<c:out value="${listing.id}"/>
                                            </span>
                                            <paw:badge text="${statusBadgeText}" classname="${statusBadgeClass}" size="sm"/>
                                        </div>
                                        <a href="${listingViewUrl}" class="font-semibold text-base hover:text-lime-600 truncate transition">
                                            <c:out value="${listing.title}"/>
                                        </a>
                                        <div class="flex items-center gap-3 text-xs text-black/60 mt-1">
                                            <span>
                                                <spring:message code="admin.listings.by" var="byPrefix"/>
                                                <c:out value="${byPrefix}"/>
                                                <a href="${creatorProfileUrl}" class="hover:underline font-medium text-black">
                                                    <c:out value="${listing.creator.username}"/>
                                                </a>
                                            </span>
                                            <span class="font-bold text-sm text-black">
                                                $<c:out value="${listing.price.amount}"/>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="flex items-center gap-2 self-end sm:self-center">
                                    <spring:message code="admin.listings.view" var="viewListingText"/>
                                    <paw:linkButton href="${listingViewUrl}" text="${viewListingText}" variant="outline" size="sm" icon="external-link"/>

                                    <c:if test="${listing.status.name() ne 'CANCELED' and listing.status.name() ne 'SOLD'}">
                                        <form method="post" action="${takedownActionUrl}">
                                            <input type="hidden" name="redirect" value="/admin/listings?page=${listingsPage.page}&query=<c:out value='${currentQuery}'/>&status=<c:out value='${currentStatus}'/>"/>
                                            <spring:message code="admin.listings.takedown.btn" var="takeDownBtnText"/>
                                            <paw:button text="${takeDownBtnText}" type="submit" variant="outline" role="danger" size="sm" icon="trash-2"/>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </paw:card>
                    </c:forEach>

                    <%-- Pagination --%>
                    <paw:pagination page="${listingsPage}" baseUrl="${listingsBaseUrl}"/>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</admin:layout>
