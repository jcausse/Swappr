<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="false" %>
<%@ attribute name="icon" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="classname" required="false" %>
<%@ attribute name="color" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:set var="badgeSize" value="${not empty size ? size : 'md'}"/>
<c:set var="badgeClass" value="${not empty classname ? classname : ''}"/>
<c:set var="badgeColor" value="${not empty color ? color : ''}"/>

<c:set var="sizeClassnames" value="${
  badgeSize eq 'sm'
    ? 'px-1.5 text-xs/4 min-w-8'
    : badgeSize eq 'lg'
    ? 'px-3 text-base/6 min-w-16'
    : 'px-2 text-sm/5 min-w-12'
}"/>

<div class="font-semibold rounded-full text-center border border-current/20 bg-current/10 flex-shrink-0 ${sizeClassnames} ${badgeClass} ${badgeColor}">
    <c:if test="${not empty icon}">
        <paw:icon name="${icon}" />
    </c:if>
    <c:if test="${not empty text}">
        <c:out value="${text}"/>
    </c:if>
</div>
