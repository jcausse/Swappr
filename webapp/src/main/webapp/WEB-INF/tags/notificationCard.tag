<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="notification" required="true" type="ar.edu.itba.paw.model.SiteNotification" %>
<%@ attribute name="compact" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="isCompact" value="${compact ne null ? compact : false}"/>

<c:choose>
    <c:when test="${notification.type.name() eq 'NEW_OFFER'}">
        <c:set var="typeBadgeClass" value="text-blue-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'OFFER_ACCEPTED'}">
        <c:set var="typeBadgeClass" value="text-lime-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'OFFER_REJECTED'}">
        <c:set var="typeBadgeClass" value="text-red-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'OFFER_WITHDRAWN'}">
        <c:set var="typeBadgeClass" value="text-neutral-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'PROOF_OF_PAYMENT_UPLOADED'}">
        <c:set var="typeBadgeClass" value="text-amber-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'PROOF_OF_SHIPPING_UPLOADED'}">
        <c:set var="typeBadgeClass" value="text-purple-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'PURCHASE_CONFIRMED'}">
        <c:set var="typeBadgeClass" value="text-green-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'BRAND_MODEL_REQUEST_ACCEPTED'}">
        <c:set var="typeBadgeClass" value="text-lime-600"/>
    </c:when>
    <c:when test="${notification.type.name() eq 'BRAND_MODEL_REQUEST_REJECTED'}">
        <c:set var="typeBadgeClass" value="text-red-600"/>
    </c:when>
    <c:otherwise>
        <c:set var="typeBadgeClass" value="text-neutral-600"/>
    </c:otherwise>
</c:choose>

<spring:message code="notification.type.${notification.type.name()}" var="typeBadgeLabel"/>
<spring:message code="${notification.titleKey}" var="notificationTitle"/>
<spring:message code="${notification.bodyKey}" var="notificationBody">
    <c:forEach var="arg" items="${notification.bodyArgs}">
        <spring:argument value="${arg}"/>
    </c:forEach>
</spring:message>
<c:set var="cardSpacingClass" value="${isCompact ? 'p-3! gap-1.5!' : ''}"/>
<c:set var="cardStateClass" value="${not notification.read ? 'border-lime-600/30!' : 'opacity-80'}"/>

<paw:card classname="${cardSpacingClass} ${cardStateClass}">
    <div class="flex flex-row items-center gap-2 flex-wrap">
        <c:if test="${not notification.read}">
            <span class="w-2 h-2 rounded-full bg-lime-600 shrink-0" aria-hidden="true"></span>
        </c:if>
        <paw:badge text="${typeBadgeLabel}" size="sm" classname="${typeBadgeClass}"/>
        <span class="text-xs text-black/50 ml-auto">
            <spring:message code="notifications.timestamp" arguments="${notification.createdAt.toEpochMilli()}"/>
        </span>
    </div>

    <div class="flex flex-row items-center gap-3 mt-0.5">
        <div class="flex-1 min-w-0 flex flex-col gap-1">
            <h4 class="text-sm font-semibold text-black leading-snug">
                <c:out value="${notificationTitle}"/>
            </h4>
            <p class="${isCompact ? 'text-xs' : 'text-sm'} text-black/70 leading-relaxed break-words">
                <c:out value="${notificationBody}"/>
            </p>

            <c:if test="${notification.actionUrl.isPresent() and notification.showCallToAction()}">
                <c:url value="${notification.actionUrl.get()}" var="ctaUrl"/>
                <c:set var="ctaKey" value="${notification.actionLabelKey.isPresent() ? notification.actionLabelKey.get() : 'notifications.action.open'}"/>
                <spring:message code="${ctaKey}" var="ctaLabel"/>
                <div class="mt-1.5 flex flex-row items-center">
                    <paw:linkButton href="${ctaUrl}" text="${ctaLabel}" size="sm" icon="arrow-up-right"/>
                </div>
            </c:if>
        </div>

        <div class="flex flex-row items-center gap-1 shrink-0 self-center">
            <c:if test="${not notification.read}">
                <c:url value="/notifications/${notification.id}/read" var="markReadUrl"/>
                <spring:message code="notifications.action.markRead" var="markReadLabel"/>
                <form action="${markReadUrl}" method="post" class="inline-flex">
                    <paw:button type="submit" variant="outline" size="sm" icon="check" ariaLabel="${markReadLabel}"/>
                </form>
            </c:if>

            <c:url value="/notifications/${notification.id}/delete" var="deleteUrl"/>
            <spring:message code="notifications.action.delete" var="deleteLabel"/>
            <form action="${deleteUrl}" method="post" class="inline-flex">
                <paw:button type="submit" variant="outline" role="danger" size="sm" icon="trash-2" ariaLabel="${deleteLabel}"/>
            </form>
        </div>
    </div>
</paw:card>
