<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="items" required="true" type="java.lang.Object" %>
<%@ attribute name="badgeCounts" required="false" type="java.lang.Object" %>
<%@ attribute name="itemLabel" required="false" type="java.lang.String" %>
<%@ attribute name="itemValue" required="false" type="java.lang.String" %>
<%@ attribute name="labelPrefix" required="false" type="java.lang.String" %>
<%@ attribute name="translateLabels" required="false" type="java.lang.Boolean" %>
<%@ attribute name="selectedOption" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="isTranslateLabels" value="${translateLabels ne null ? translateLabels : false}"/>
<c:set var="resolvedItemLabel" value="${not empty itemLabel ? itemLabel : 'label'}"/>
<c:set var="resolvedItemValue" value="${not empty itemValue ? itemValue : 'value'}"/>
<c:set var="resolvedLabelPrefix" value="${not empty labelPrefix ? labelPrefix : ''}"/>

<div class="flex flex-row w-full gap-2 relative" id="${path}_base">
    <c:forEach var="item" items="${items}">
        <c:set var="optionValue" value="${item[resolvedItemValue]}"/>
        <c:set var="optionLabelKey" value="${resolvedLabelPrefix}${item[resolvedItemLabel]}"/>
        <div class="grow">
            <form:radiobutton path="${path}" id="${path}_${optionValue}" value="${optionValue}" cssClass="sr-only peer" />
            <c:choose>
                <c:when test="${isTranslateLabels}">
                    <spring:message code="${optionLabelKey}" var="optionLabel"/>
                </c:when>
                <c:otherwise>
                    <c:set var="optionLabel" value="${item[resolvedItemLabel]}"/>
                </c:otherwise>
            </c:choose>
            <paw:fakeButton htmlFor="${path}_${optionValue}" variant="ghost" on="${(empty optionValue and empty selectedOption) or optionValue == selectedOption ? 'true' : 'false'}">
                <c:out value="${optionLabel}" />
                <c:if test="${not empty badgeCounts}">
                    <c:set var="countValue" value="${badgeCounts.get(optionValue)}" />
                    <c:if test="${countValue != null and countValue > 0}">
                        <div class="rounded-full bg-white">
                            <paw:badge text="${countValue}" classname="text-lime-600 min-w-5! p-0.25!" size="sm" />
                        </div>
                    </c:if>
                </c:if>
            </paw:fakeButton>
        </div>
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
