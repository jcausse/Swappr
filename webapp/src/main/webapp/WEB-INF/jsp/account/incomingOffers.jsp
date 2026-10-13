<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>


<c:url value="/account/incoming-offers" var="filterAction"/>
<spring:message code="account.incomingOffers.title" var="titleMsg"/>
<spring:message code="account.incomingOffers.subtitle" var="subtitleMsg"/>
<spring:message code="account.incomingOffers.pendingTitle" var="pendingTitleMsg"/>
<spring:message code="account.incomingOffers.pendingPaymentTitle" var="pendingPaymentTitleMsg"/>
<spring:message code="account.incomingOffers.resolvedTitle" var="resolvedTitleMsg"/>

<account:layout titleKey="account.incomingOffers.title" title="${titleMsg}" subtitle="${subtitleMsg}">
    <form:form modelAttribute="filterForm" action="${filterAction}" method="get" id="filterForm">
        <paw:card classname="p-2! mb-4">
            <paw:formButtonToggle path="statusGroup" items="${statusGroupOptions}" selectedOption="${filterForm.getStatusGroup()}" />
        </paw:card>
    </form:form>

    <c:choose>
        <c:when test="${empty offers}">
            <div class="text-center py-12">
                <spring:message code="account.incomingOffers.empty" var="emptyMsg"/>
                <p class="text-black/50 text-lg"><c:out value="${emptyMsg}"/></p>
            </div>
        </c:when>
        <c:when test="${filterForm.getStatusGroup() == 'pending' || filterForm.getStatusGroup() == 'pending_payment'}">
            <div class="flex flex-col gap-4 mb-8">
                <c:forEach var="offer" items="${offers}">
                    <account:offerCard offer="${offer}" user="buyer">
                        <c:url value="/account/incoming-offers/${offer.id}" var="offerUrl"/>
                        <paw:linkButton variant="outline" href="${offerUrl}" icon="eye" />
                        <paw:menu id="incoming${offer.id}" variant="outline" icon="ellipsis" panelWidth="w-66" panelAlign="right">
                            <c:if test="${offer.offeredListing != null}">
                                <c:url value="/listing/${offer.offeredListing.id}" var="offeredListingUrl"/>
                                <spring:message code="offer.viewOfferedListing" var="viewOfferedListingLabel"/>
                                <paw:linkButton href="${offeredListingUrl}" variant="ghost" icon="eye" text="${viewOfferedListingLabel}" classname="w-full justify-start" />
                            </c:if>
                            <c:if test="${offer.status.name() == 'PENDING_PAYMENT'}">
                                <c:url value="/account/my-offers/${offer.id}/shipping" var="shippingUrl"/>
                                <spring:message code="offer.proofOfShipping.uploadLabel" var="shippingLabel"/>
                                <paw:linkButton href="${shippingUrl}" variant="ghost" icon="truck" text="${shippingLabel}" classname="w-full justify-start" />
                                <form action="<c:url value='/offer/${offer.id}/confirm-payment'/>" method="POST">
                                    <spring:message code="offer.action.confirmPayment" var="confirmLabel"/>
                                    <paw:button type="submit" variant="ghost" icon="check" text="${confirmLabel}" classname="w-full justify-start" />
                                </form>
                            </c:if>
                            <c:if test="${offer.status.name() == 'PENDING'}">
                                <spring:message code="offer.action.accept" var="acceptLabel"/>
                                <form action="<c:url value='/offer/${offer.id}/accept'/>" method="POST">
                                    <paw:button type="submit" variant="ghost" icon="check" text="${acceptLabel}" classname="w-full justify-start" />
                                </form>
                            </c:if>
                            <spring:message code="offer.action.reject" var="rejectLabel"/>
                            <form action="<c:url value='/offer/${offer.id}/reject'/>" method="POST">
                                <paw:button type="submit" variant="ghost" icon="x" role="danger" text="${rejectLabel}" classname="w-full justify-start" />
                            </form>
                        </paw:menu>
                    </account:offerCard>
                </c:forEach>

                <paw:pagination page="${offerPage}" baseUrl="/account/incoming-offers"/>
            </div>
        </c:when>
        <c:when test="${filterForm.getStatusGroup() == 'resolved'}">
            <paw:card>
                <account:offerTable offers="${offers}" user="buyer" />
                <paw:pagination page="${offerPage}" baseUrl="/account/incoming-offers"/>
            </paw:card>
        </c:when>
    </c:choose>
</account:layout>
