<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="profile" tagdir="/WEB-INF/tags/profile" %>


<spring:message code="profile.listings.title" var="listingsTitle"/>
<spring:message code="profile.listings.empty" var="listingsEmpty"/>
<spring:message code="profile.listings.view" var="viewListingLabel"/>
<spring:message code="profile.ratings.title" var="ratingsTitle"/>
<spring:message code="profile.ratings.filter.role" var="ratingFilterRole"/>
<spring:message code="profile.ratings.filter.type" var="ratingFilterType"/>
<spring:message code="profile.ratings.filter.all" var="ratingFilterAll"/>
<spring:message code="profile.ratings.filter.seller" var="ratingFilterSeller"/>
<spring:message code="profile.ratings.filter.buyer" var="ratingFilterBuyer"/>
<spring:message code="profile.ratings.filter.positive" var="ratingFilterPositive"/>
<spring:message code="profile.ratings.filter.neutral" var="ratingFilterNeutral"/>
<spring:message code="profile.ratings.filter.negative" var="ratingFilterNegative"/>
<spring:message code="profile.ratings.empty" var="ratingEmpty"/>

<c:url value="/profile/${user.id}" var="profileUrl"/>
<c:url value="/profile/${user.id}" var="filterAction"/>

<paw:layout titleKey="profile.title" titleParams="${user.displayName}">
    <div class="flex flex-col gap-6">
        <c:if test="${user.suspended}">
            <spring:message code="profile.suspended.banner" var="profileSuspendedMsg"/>
            <paw:banner text="${profileSuspendedMsg}" role="danger" icon="alert-triangle"/>
        </c:if>
        <paw:card classname="w-full flex flex-col gap-8">
            <div class="flex items-center gap-6">
                <paw:userAvatar user="${user}" size="xl" />
                <div class="flex flex-col">
                    <div class="flex items-center gap-3">
                        <h1 class="text-3xl font-bold tracking-tight"><c:out value="${user.displayName}"/></h1>
                        <c:if test="${user.suspended}">
                            <spring:message code="profile.badge.suspended" var="profileSuspendedBadge"/>
                            <paw:badge text="${profileSuspendedBadge}" classname="text-red-700 bg-red-100" size="sm"/>
                        </c:if>
                    </div>
                    <span class="text-lg text-black/60 font-medium">@<c:out value="${user.username}"/></span>

                    <c:if test="${not empty user.joinedAt}">
                        <span class="text-sm text-black/60 mt-1">
                            <spring:message code="profile.memberSince" arguments="${user.joinedAt.toEpochMilli()}"/>
                        </span>
                    </c:if>
                    <span class="text-sm text-neutral-500 mt-1 flex items-center gap-1">
                        <paw:icon name="map-pin" />
                        <c:choose>
                            <c:when test="${user.province.present}">
                                <spring:message code="province.${user.province.get().name}" var="provinceLabel"/>
                                <c:out value="${provinceLabel}"/><c:if test="${user.locationDetail.present}">, <c:out value="${user.locationDetail.get()}"/></c:if>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="profile.location.notSpecified"/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <c:if test="${allowEdit}">
                        <div class="mt-6">
                            <c:url value="/profile/edit" var="editUrl"/>
                            <spring:message code="profile.edit" var="editLabel"/>
                            <paw:linkButton href="${editUrl}" text="${editLabel}" size="sm" variant="outline"/>
                        </div>
                    </c:if>
                </div>
            </div>
        </paw:card>

        <paw:card title="${ratingsTitle}">
            <c:set var="sellerBalance" value="${user.sellerRatingBalance}"/>
            <c:set var="sellerTotalRatings" value="${user.sellerTotalRatings}"/>
            <c:set var="buyerBalance" value="${user.buyerRatingBalance}"/>
            <c:set var="buyerTotalRatings" value="${user.buyerTotalRatings}"/>

            <div class="flex flex-col sm:flex-row gap-8 my-4">
                <profile:reputation user="${user}" role="seller" classname="flex-1" />
                <profile:reputation user="${user}" role="buyer" classname="flex-1" />
            </div>

            <!-- Ratings list with filters -->
            <paw:divider />

            <form:form modelAttribute="ratingFilterForm" action="${filterAction}" method="get" id="filterForm" class="mt-4">
                <div class="flex flex-wrap gap-4 mb-4">
                    <div class="flex flex-col gap-1 grow">
                        <label class="text-xs font-medium text-black/60"><c:out value="${ratingFilterRole}"/></label>
                        <div class="border border-black/10 rounded-xl p-1">
                            <paw:formButtonToggle path="role" items="${roleOptions}" selectedOption="${ratingFilterForm.role}" />
                        </div>
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-medium text-black/60"><c:out value="${ratingFilterType}"/></label>
                        <div class="border border-black/10 rounded-xl p-1">
                            <paw:formButtonToggle path="type" items="${typeOptions}" selectedOption="${ratingFilterForm.type}" />
                        </div>
                    </div>
                </div>
            </form:form>

            <c:choose>
                <c:when test="${empty ratings}">
                    <div class="text-center py-8">
                        <p class="text-black/50"><c:out value="${ratingEmpty}"/></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="flex flex-col gap-4">
                        <c:forEach var="rating" items="${ratings}" varStatus="loop">
                            <div class="flex flex-row gap-4 items-center">
                                <c:choose>
                                    <c:when test="${rating.type.name() == 'POSITIVE'}">
                                        <paw:icon name="arrow-up" classname="font-bold border rounded-full w-6 h-6 bg-green-50 border-green-200 text-green-600" />
                                    </c:when>
                                    <c:when test="${rating.type.name() == 'NEUTRAL'}">
                                        <paw:icon name="minus" classname="font-bold border rounded-full w-6 h-6 bg-stone-50 border-stone-200 text-stone-500" />
                                    </c:when>
                                    <c:otherwise>
                                        <paw:icon name="arrow-down" classname="font-bold border rounded-full w-6 h-6 bg-red-50 border-red-200 text-red-600" />
                                    </c:otherwise>
                                </c:choose>
                                <div class="flex-1 min-w-0">
                                    <div class="flex flex-row gap-2 items-center mb-1">
                                        <span class="font-medium text-black truncate"><c:out value="${rating.listing.title}"/></span>
                                    </div>
                                    <p class="text-sm text-black/60 mt-1 whitespace-pre-wrap"><c:out value="${rating.reviewText}"/></p>
                                </div>
                                <div class="flex flex-col items-end">
                                    <paw:user user="${rating.creator}" variant="compact" />
                                </div>
                            </div>
                            <c:if test="${!loop.last}">
                                <paw:divider />
                            </c:if>
                        </c:forEach>

                        <paw:pagination page="${ratingPage}" baseUrl="/profile/${user.id}"/>
                    </div>
                </c:otherwise>
            </c:choose>
        </paw:card>

        <paw:card title="${listingsTitle}">
            <c:choose>
                <c:when test="${empty listings}">
                    <div class="mt-12 mb-12 flex flex-col items-center">
                        <p class="text-black/60 text-lg mb-4"><c:out value="${listingsEmpty}"/></p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="flex flex-col gap-4 mt-4">
                        <c:forEach var="listing" items="${listings}" varStatus="loop">
                            <c:url value="/listing/${listing.id}" var="listingUrl"/>
                            <div class="flex flex-col gap-2">
                                <div class="flex flex-col sm:flex-row sm:items-center gap-4">
                                    <div class="min-w-0">
                                        <h3 class="text-base font-semibold truncate">
                                            <a href="${listingUrl}" class="hover:text-lime-600 transition block truncate"><c:out value="${listing.title}"/></a>
                                        </h3>
                                    </div>
                                    <c:if test="${listing.pendingOffersCount > 0}">
                                        <spring:message code="listing.detail.pendingOffersBadge" arguments="${listing.pendingOffersCount}" var="pendingOffersBadge"/>
                                        <paw:badge text="${pendingOffersBadge}" classname="text-red-600 ml-auto" />
                                    </c:if>
                                </div>

                                <div class="flex flex-row gap-3 w-full sm:w-auto">
                                    <paw:listingImage listing="${listing}" />

                                    <div class="flex-1 min-w-0 flex flex-col justify-between">
                                        <paw:product product="${listing.product}" size="sm" />
                                        <div class="text-xl font-bold mt-2 sm:mt-0">
                                            $<c:out value="${listing.price.amount}"/>
                                        </div>
                                    </div>

                                    <div class="flex flex-row gap-1 self-end">
                                        <paw:linkButton variant="outline" href="${listingUrl}" text="${viewListingLabel}" />
                                    </div>
                                </div>
                            </div>

                            <c:if test="${!loop.last}">
                                <paw:divider />
                            </c:if>
                        </c:forEach>
                    </div>

                    <paw:pagination page="${listingPage}" baseUrl="/profile/${user.id}"/>
                </c:otherwise>
            </c:choose>
        </paw:card>
    </div>
</paw:layout>
