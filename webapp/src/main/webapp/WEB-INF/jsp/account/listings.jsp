<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>


<spring:message code="account.listings.title" var="titleMsg"/>
<spring:message code="account.listings.subtitle" var="subtitleMsg"/>
<spring:message code="account.listings.newListing" var="newListingLabel"/>
<spring:message code="account.listings.filter.status" var="statusLabel"/>
<spring:message code="account.listings.filter.all" var="allLabel"/>
<spring:message code="account.listings.filter.sort" var="sortLabel"/>
<spring:message code="account.listings.filter.default" var="defaultLabel"/>
<spring:message code="listing.status.ACTIVE" var="statusActive"/>
<spring:message code="listing.status.SOLD" var="statusSold"/>
<spring:message code="listing.status.CANCELED" var="statusCanceled"/>
<spring:message code="discovery.sort.recent" var="sortRecent"/>
<spring:message code="discovery.sort.price_asc" var="sortPriceAsc"/>
<spring:message code="discovery.sort.price_desc" var="sortPriceDesc"/>
<spring:message code="account.listings.sort.name_asc" var="sortNameAsc"/>
<spring:message code="account.listings.sort.name_desc" var="sortNameDesc"/>

<c:url value="/account/listings" var="filterAction"/>
<c:url value="/listing/new/choose-product" var="newListingUrl"/>

<account:layout titleKey="account.listings.title" title="${titleMsg}" subtitle="${subtitleMsg}" actionHref="${newListingUrl}" actionText="${newListingLabel}">
    <paw:card>
        <form:form modelAttribute="filterForm" action="${filterAction}" method="get" id="filterForm">
            <div class="flex items-center justify-between gap-4 mb-4">
                <div class="flex gap-2">
                    <paw:formSelect path="status" label="${statusLabel}" placeholder="${allLabel}" items="${statusOptions}" itemLabel="label" itemValue="value" classname="w-auto" />
                </div>
                <div class="flex items-center gap-2">
                    <paw:formSelect path="sort" label="${sortLabel}" placeholder="${defaultLabel}" items="${sortOptions}" itemLabel="label" itemValue="value" classname="w-auto" />
                </div>
            </div>
        </form:form>
        <paw:divider />

        <c:choose>
            <c:when test="${empty listings}">
                <div class="mt-12 mb-12 flex flex-col items-center">
                    <spring:message code="account.listings.empty" var="emptyMsg"/>
                    <p class="text-black/60 text-lg mb-4"><c:out value="${emptyMsg}"/></p>
                    <paw:linkButton text="${newListingLabel}" size="lg" href="${newListingUrl}"/>
                </div>
            </c:when>

            <c:otherwise>
                <div class="flex flex-col gap-4 mt-4">
                    <c:forEach var="listing" items="${listings}" varStatus="loop">
                        <c:url value="/listing/${listing.id}" var="listingUrl"/>
                        <div class="flex flex-col gap-2">
                            <c:choose>
                                <c:when test="${listing.status.name() == 'ACTIVE'}">
                                    <c:set var="statusClass" value="text-green-600"/>
                                    <spring:message code="listing.status.ACTIVE" var="statusLabel"/>
                                </c:when>
                                <c:when test="${listing.status.name() == 'SOLD'}">
                                    <c:set var="statusClass" value="text-neutral-600"/>
                                    <spring:message code="listing.status.SOLD" var="statusLabel"/>
                                </c:when>
                                <c:when test="${listing.status.name() == 'PENDING_TRANSACTION'}">
                                    <c:set var="statusClass" value="text-blue-600"/>
                                    <spring:message code="listing.status.PENDING_TRANSACTION" var="statusLabel"/>
                                </c:when>
                                <c:when test="${listing.status.name() == 'OFFERED_IN_TRADE'}">
                                    <c:set var="statusClass" value="text-purple-600"/>
                                    <spring:message code="listing.status.OFFERED_IN_TRADE" var="statusLabel"/>
                                </c:when>
                                <c:when test="${listing.status.name() == 'CANCELED'}">
                                    <c:set var="statusClass" value="text-red-600"/>
                                    <spring:message code="listing.status.CANCELED" var="statusLabel"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="statusClass" value="text-yellow-600"/>
                                    <c:set var="statusLabel" value="${listing.status.name()}"/>
                                </c:otherwise>
                            </c:choose>

                            <div class="flex flex-col sm:flex-row sm:items-center gap-4">
                                <div class="min-w-0">
                                    <h3 class="text-base font-semibold truncate">
                                        <a href="${listingUrl}" class="hover:text-lime-600 transition block truncate"><c:out value="${listing.title}"/></a>
                                    </h3>
                                </div>
                                <div class="flex items-center gap-2 ml-auto">
                                    <paw:badge text="${statusLabel}" classname="${statusClass}" />
                                    <c:if test="${listing.pendingOffersCount > 0}">
                                        <spring:message code="listing.detail.pendingOffersBadge" arguments="${listing.pendingOffersCount}" var="pendingOffersBadge"/>
                                        <paw:badge text="${pendingOffersBadge}" classname="text-amber-600" />
                                    </c:if>
                                </div>
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
                                    <paw:linkButton variant="outline" href="${listingUrl}" icon="eye" />
                                    <c:if test="${listing.status.name() == 'ACTIVE'}">
                                        <c:url value="/listing/${listing.id}/edit" var="editListingUrl"/>
                                        <paw:linkButton variant="outline" href="${editListingUrl}" icon="pencil" />

                                        <paw:button variant="outline" role="danger" icon="trash-2"
                                                    onclick="document.getElementById('cancelDialog-${listing.id}').showModal()"/>

                                        <spring:message code="listing.cancel.confirm.title" var="cancelTitle"/>
                                        <spring:message code="listing.cancel.confirm.confirm" var="cancelConfirm"/>
                                        <spring:message code="listing.cancel.confirm.cancel" var="cancelCancel"/>
                                        <spring:message code="listing.cancel.confirm.message" var="cancelMessage"/>
                                        <c:url value="/listing/${listing.id}/cancel" var="cancelListingUrl"/>
                                        <paw:confirmDialog id="cancelDialog-${listing.id}" title="${cancelTitle}" confirmText="${cancelConfirm}"
                                                            cancelText="${cancelCancel}" formAction="${cancelListingUrl}">
                                            <c:out value="${cancelMessage}"/>
                                        </paw:confirmDialog>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <c:if test="${!loop.last}">
                            <paw:divider />
                        </c:if>
                    </c:forEach>
                </div>

                <paw:pagination page="${listingPage}" baseUrl="/account/listings"/>
            </c:otherwise>
        </c:choose>
    </paw:card>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const statusSelect = document.getElementById('status');
            const sortSelect = document.getElementById('sort');
            const isAutoSubmitInput = document.getElementById('isAutoSubmit');

            function submitForm() {
                if (isAutoSubmitInput) {
                    isAutoSubmitInput.value = 'true';
                }
                document.querySelector('form#filterForm').submit();
            }

            if (statusSelect) {
                statusSelect.addEventListener('change', submitForm);
            }
            if (sortSelect) {
                sortSelect.addEventListener('change', submitForm);
            }
        });
    </script>
</account:layout>
