<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="offers" required="true" type="java.util.List" %>
<%@ attribute name="user" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<div class="overflow-x-auto mt-2">
    <table class="w-full text-left text-sm">
        <thead>
            <tr class="border-b border-black/10">
                <th class="pb-2 font-medium text-black/60"><spring:message code="account.incomingOffers.table.listing"/></th>
                <th class="pb-2 font-medium text-black/60"><spring:message code="account.incomingOffers.table.${user}"/></th>
                <th class="pb-2 font-medium text-black/60"><spring:message code="account.incomingOffers.table.amount"/></th>
                <th class="pb-2 font-medium text-black/60"><spring:message code="account.incomingOffers.table.status"/></th>
                <th class="pb-2 font-medium text-black/60"><spring:message code="account.incomingOffers.table.review"/></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="offer" items="${offers}">
                <c:choose>
                    <c:when test="${offer.status.name() == 'ACCEPTED'}">
                        <c:set var="statusClass" value="text-lime-600"/>
                        <spring:message code="offer.status.ACCEPTED" var="statusLabel"/>
                    </c:when>
                    <c:when test="${offer.status.name() == 'REJECTED'}">
                        <c:set var="statusClass" value="text-red-600"/>
                        <spring:message code="offer.status.REJECTED" var="statusLabel"/>
                    </c:when>
                    <c:when test="${offer.status.name() == 'WITHDRAWN'}">
                        <c:set var="statusClass" value="text-stone-600"/>
                        <spring:message code="offer.status.WITHDRAWN" var="statusLabel"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="statusClass" value="text-stone-600"/>
                        <c:set var="statusLabel" value="${offer.status.name()}"/>
                    </c:otherwise>
                </c:choose>

                <c:set var="myRating" value="${user == 'buyer' ? offer.buyerRating : offer.sellerRating}"/>
                <tr class="border-b border-black/5 last:border-0">
                    <td class="py-1">
                        <c:url value="/listing/${offer.listing.id}" var="listingUrl"/>
                        <a href="${listingUrl}" class="hover:text-lime-600 transition font-medium"><c:out value="${offer.listing.title}"/></a>
                    </td>
                    <td class="py-1 pr-2">
                        <c:if test="${user == 'buyer'}"><paw:user user="${offer.buyer}" variant="compact" /></c:if>
                        <c:if test="${user == 'seller'}"><paw:user user="${offer.listing.creator}" variant="compact" /></c:if>
                    </td>
                    <td class="py-1 font-semibold">
                        <div>
                            <c:if test="${not offer.isFullPrice}">
                                <p class="text-xs font-medium line-through text-black/60">
                                    <paw:price amount="${offer.listing.price.amount}"/>
                                </p>
                            </c:if>
                            <paw:price amount="${offer.amount}"/>
                        </div>
                    </td>
                    <td class="py-1">
                        <div class="flex flex-row">
                            <paw:badge text="${statusLabel}" classname="${statusClass}" size="sm" />
                        </div>
                    </td>
                    <td class="py-1">
                        <c:choose>
                            <c:when test="${offer.status.name() == 'ACCEPTED' and not myRating.isPresent()}">
                                <div class="flex flex-row gap-1 justify-start">
                                    <c:choose>
                                        <c:when test="${user == 'buyer'}">
                                            <c:url value="/account/rate/${offer.id}?role=SELLER&rating=POSITIVE" var="positiveUrl"/>
                                            <c:url value="/account/rate/${offer.id}?role=SELLER&rating=NEUTRAL" var="neutralUrl"/>
                                            <c:url value="/account/rate/${offer.id}?role=SELLER&rating=NEGATIVE" var="negativeUrl"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url value="/account/rate/${offer.id}?role=BUYER&rating=POSITIVE" var="positiveUrl"/>
                                            <c:url value="/account/rate/${offer.id}?role=BUYER&rating=NEUTRAL" var="neutralUrl"/>
                                            <c:url value="/account/rate/${offer.id}?role=BUYER&rating=NEGATIVE" var="negativeUrl"/>
                                        </c:otherwise>
                                    </c:choose>
                                    <paw:linkButton href="${positiveUrl}" variant="outline" size="sm" icon="arrow-up" />
                                    <paw:linkButton href="${neutralUrl}" variant="outline" size="sm" icon="minus" role="secondary" />
                                    <paw:linkButton href="${negativeUrl}" variant="outline" size="sm" icon="arrow-down" role="danger" />
                                </div>
                            </c:when>
                            <c:when test="${offer.status.name() == 'ACCEPTED' and myRating.isPresent()}">
                                <div class="flex flex-row justify-start">
                                    <c:choose>
                                        <c:when test="${myRating.get().name() == 'POSITIVE'}">
                                            <paw:icon name="arrow-up" classname="text-lime-600"/>
                                        </c:when>
                                        <c:when test="${myRating.get().name() == 'NEUTRAL'}">
                                            <paw:icon name="minus" classname="text-stone-500"/>
                                        </c:when>
                                        <c:otherwise>
                                            <paw:icon name="arrow-down" classname="text-red-600"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
