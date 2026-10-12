<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>


<c:url value="/account/my-offers" var="filterAction"/>
<spring:message code="account.myOffers.title" var="titleMsg"/>
<spring:message code="account.myOffers.subtitle" var="subtitleMsg"/>
<spring:message code="account.myOffers.pendingTitle" var="pendingTitleMsg"/>
<spring:message code="account.myOffers.pendingPaymentTitle" var="pendingPaymentTitleMsg"/>
<spring:message code="account.myOffers.resolvedTitle" var="resolvedTitleMsg"/>

<account:layout titleKey="account.myOffers.title" title="${titleMsg}" subtitle="${subtitleMsg}">
    <form:form modelAttribute="filterForm" action="${filterAction}" method="get" id="filterForm">
        <paw:card classname="p-2! mb-4">
            <paw:formButtonToggle path="statusGroup" items="${statusGroupOptions}" selectedOption="${filterForm.getStatusGroup()}" />
        </paw:card>
    </form:form>

    <c:choose>
        <c:when test="${empty offers}">
            <div class="text-center py-12 flex flex-col items-center gap-4">
                <spring:message code="account.myOffers.empty" var="emptyMsg"/>
                <p class="text-black/50 text-lg"><c:out value="${emptyMsg}"/></p>
                <c:url value="/listing" var="exploreUrl"/>
                <spring:message code="landing.hero.cta.explore" var="exploreLabel"/>
                <paw:linkButton href="${exploreUrl}" text="${exploreLabel}" icon="search"/>
            </div>
        </c:when>
        <c:when test="${filterForm.getStatusGroup() == 'pending' || filterForm.getStatusGroup() == 'pending_payment'}">
            <div class="flex flex-col gap-4 mb-8">
                <c:forEach var="offer" items="${offers}">
                    <account:offerCard offer="${offer}" user="seller">
                        <paw:menu id="my${offer.id}" variant="outline" icon="ellipsis" panelWidth="w-56" panelAlign="right">
                            <c:if test="${offer.status.name() == 'PENDING_PAYMENT'}">
                                <c:url value="/account/my-offers/${offer.id}/payment" var="proofUrl"/>
                                <spring:message code="offer.proofOfPayment.uploadLabel" var="proofLabel"/>
                                <paw:linkButton href="${proofUrl}" variant="ghost" icon="upload" text="${proofLabel}" classname="w-full justify-start" />
                            </c:if>
                            <spring:message code="account.myOffers.withdraw" var="withdrawLabel"/>
                            <form action="<c:url value='/offer/${offer.id}/withdraw'/>" method="POST">
                                <paw:button type="submit" variant="ghost" icon="x" role="danger" text="${withdrawLabel}" classname="w-full justify-start" />
                            </form>
                        </paw:menu>
                    </account:offerCard>
                </c:forEach>

                <paw:pagination page="${offerPage}" baseUrl="/account/my-offers"/>
            </div>
        </c:when>
        <c:when test="${filterForm.getStatusGroup() == 'resolved'}">
            <paw:card>
                <account:offerTable offers="${offers}" user="seller" />
                <paw:pagination page="${offerPage}" baseUrl="/account/my-offers"/>
            </paw:card>
        </c:when>
    </c:choose>
</account:layout>
