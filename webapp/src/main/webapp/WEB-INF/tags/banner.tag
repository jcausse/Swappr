<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="false" %>
<%@ attribute name="icon" required="false" %>
<%@ attribute name="role" required="false" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:set var="bannerRole" value="${not empty role ? role : 'default'}"/>
<c:set var="bannerClass" value="${not empty classname ? classname : ''}"/>

<c:set var="roleClassnames" value="${
  bannerRole eq 'danger'
    ? 'bg-red-50 border-red-200 text-red-800'
    : bannerRole eq 'warning'
    ? 'bg-amber-50 border-amber-200 text-amber-800'
    : bannerRole eq 'success'
    ? 'bg-green-50 border-green-200 text-green-800'
    : bannerRole eq 'info'
    ? 'bg-blue-50 border-blue-200 text-blue-800'
    : bannerRole eq 'secondary'
    ? 'bg-stone-50 border-stone-200 text-stone-800'
    : 'bg-lime-50 border-lime-200 text-lime-800'
}"/>

<div class="p-3 rounded-lg border flex flex-row gap-2 items-center text-start ${roleClassnames} ${bannerClass}">
    <c:if test="${not empty icon}"><paw:icon name="${icon}" /></c:if>
    <c:if test="${not empty text}"><p class="text-sm"><c:out value="${text}"/></p></c:if>
    <jsp:doBody />
</div>
