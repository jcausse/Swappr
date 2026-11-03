<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="items" required="true" type="java.lang.Object" %>
<%@ attribute name="plainStrings" required="false" type="java.lang.Boolean" %>
<%@ attribute name="translateLabels" required="false" type="java.lang.Boolean" %>
<%@ attribute name="itemLabel" required="false" type="java.lang.String" %>
<%@ attribute name="itemValue" required="false" type="java.lang.String" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="placeholder" required="true" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="includeOther" required="false" type="java.lang.Boolean" %>
<%@ attribute name="otherValue" required="false" type="java.lang.String" %>
<%-- Translated label of the "other" option; must be passed whenever includeOther is true --%>
<%@ attribute name="otherLabel" required="false" type="java.lang.String" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="inputVariant" value="${not empty variant ? variant : 'default'}"/>
<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>

<c:set var="variantClassnames" value="${
  inputVariant eq 'outline'
    ? 'border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2
       disabled:bg-black/5 disabled:text-black/40
       invalid:border-red-600! peer-[.errors]/errors:border-red-600! invalid:outline-red-600/30 peer-[.errors]/errors:outline-red-600/30'
    : 'border-t border-b border-black/15 border-b-white/20 bg-gradient-to-b from-black/5 to-black/2 bg-border
       focus-visible:outline outline-lime-600 focus-visible:shadow-[0_0_0_3px] shadow-lime-600/30
       disabled:from-black/10 disabled:to-black/8 disabled:text-black/40
       invalid:outline peer-[.errors]/errors:outline invalid:outline-red-600 peer-[.errors]/errors:outline-red-600 invalid:shadow-red-600/30 peer-[.errors]/errors:shadow-red-600/30
       invalid:from-red-600/5 invalid:to-red-600/2 peer-[.errors]/errors:from-red-600/5 peer-[.errors]/errors:to-red-600/2'
}"/>

<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>
<c:set var="isIncludeOther" value="${includeOther ne null ? includeOther : false}"/>
<c:set var="isTranslateLabels" value="${translateLabels ne null ? translateLabels : false}"/>
<c:set var="otherVal" value="${not empty otherValue ? otherValue : '__OTHER__'}"/>

<c:set var="resolvedItemLabel" value="${not empty itemLabel ? itemLabel : 'name'}"/>
<c:set var="resolvedItemValue" value="${not empty itemValue ? itemValue : 'id'}"/>

<%-- Define the markup in reverse order so we can use errors to conditionally style the input --%>
<div class="flex flex-col-reverse gap-1 ${inputClass}">
    <form:errors path="${path}" element="div" cssClass="text-xs text-red-600 peer/errors errors"/>

    <div class="relative">
        <form:select
            path="${path}"
            id="${path}"
            placeholder="${placeholder}"
            disabled="${isDisabled}"
            cssClass="px-2 py-1 rounded-lg text-sm outline-0 transition duration-150 outline-lime-600/30 placeholder:text-black/40 disabled:placeholder:text-black/20 appearance-none w-full peer ${variantClassnames}"
        >
            <form:option value="" label="${placeholder}" />

            <c:choose>
                <c:when test="${isTranslateLabels}">
                    <c:forEach var="item" items="${items}">
                        <c:choose>
                            <c:when test="${not empty plainStrings}">
                                <c:set var="optionValue" value="${item}"/>
                                <c:set var="optionLabelKey" value="${item}"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="optionValue" value="${item[resolvedItemValue]}"/>
                                <c:set var="optionLabelKey" value="${item[resolvedItemLabel]}"/>
                            </c:otherwise>
                        </c:choose>
                        <spring:message code="${optionLabelKey}" var="optionLabel"/>
                        <form:option value="${optionValue}" label="${optionLabel}"/>
                    </c:forEach>
                </c:when>
                <c:when test="${not empty plainStrings}">
                    <form:options items="${items}" />
                </c:when>
                <c:otherwise>
                    <form:options items="${items}" itemValue="${resolvedItemValue}" itemLabel="${resolvedItemLabel}" />
                </c:otherwise>
            </c:choose>

            <c:if test="${isIncludeOther}">
                <form:option value="${otherVal}" label="${otherLabel}" />
            </c:if>
        </form:select>
        <paw:icon name="chevron-down" classname="absolute top-1/2 right-1.5 -translate-y-1/2 pointer-events-none ${isDisabled ? 'text-black/40' : ''}" />
    </div>

    <c:if test="${not empty label}">
        <label for="${path}" class="text-xs text-black/70 font-medium">
            <c:out value="${label}" />
        </label>
      </c:if>
</div>
