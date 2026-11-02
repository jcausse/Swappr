<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="items" required="true" type="java.lang.Object" %>
<%@ attribute name="badgeCounts" required="false" type="java.lang.Object" %>
<%@ attribute name="translateLabels" required="false" type="java.lang.Boolean" %>
<%@ attribute name="selectedOption" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="isTranslateLabels" value="${translateLabels ne null ? translateLabels : false}"/>

<div class="flex flex-row w-full gap-2 relative" id="${path}_base">
    <c:forEach var="option" items="${items}">
        <form:radiobutton path="${path}" id="${path}_${option.getValue()}" value="${option.getValue()}" cssClass="hidden" />
        <c:choose>
            <c:when test="${isTranslateLabels}">
                <spring:message code="${option.getLabel()}" var="optionLabel"/>
            </c:when>
            <c:otherwise>
                <c:set var="optionLabel" value="${option.getLabel()}"/>
            </c:otherwise>
        </c:choose>
        <paw:fakeButton htmlFor="${path}_${option.getValue()}" variant="ghost" classname="grow" on="${(empty option.getValue() and empty selectedOption) or option.getValue() == selectedOption ? 'true' : 'false'}">
            <c:out value="${optionLabel}" />
            <c:if test="${not empty badgeCounts}">
                <c:set var="countValue" value="${badgeCounts.get(option.getValue())}" />
                <c:if test="${countValue != null and countValue > 0}">
                    <div class="rounded-full bg-white">
                        <paw:badge text="${countValue}" classname="text-lime-600 min-w-5! p-0.25!" size="sm" />
                    </div>
                </c:if>
            </c:if>
        </paw:fakeButton>
    </c:forEach>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const root = document.getElementById('${path}' + '_base');
        if (root) {
            const radios = root.querySelectorAll("input");
            radios.forEach((radio) =>
                radio.addEventListener('change', function() {
                    document.getElementById('filterForm').submit();
                })
            );
        }
    });
</script>
