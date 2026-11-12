<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="listing" required="true" type="ar.edu.itba.paw.model.Listing" %>
<%@ attribute name="size" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:set var="imageSize" value="${not empty size ? size : 'md'}"/>
<c:set var="imageClass" value="${not empty classname ? classname : ''}"/>

<c:set var="sizeClassnames" value="${
  imageSize eq 'sm'
    ? 'w-16 h-16'
    : imageSize eq 'lg'
    ? 'w-24 h-24'
    : imageSize eq 'xl'
    ? 'w-28 h-28'
    : 'w-20 h-20'
}"/>

<c:set var="coverUrl" value=""/>
<c:if test="${not empty listing.coverImageId}">
    <c:url value="/image/${listing.coverImageId}" var="coverUrl"/>
</c:if>

<div class="flex-shrink-0 rounded-lg overflow-hidden border border-black/10 bg-neutral-200 ${sizeClassnames}">
    <c:choose>
        <c:when test="${not empty coverUrl}">
            <img src="${coverUrl}" alt="<c:out value='${listing.title}'/>" class="w-full h-full object-cover"/>
        </c:when>
        <c:otherwise>
            <div class="w-full h-full grid place-items-center text-black/30 text-xs px-2 text-center">
                <spring:message code="card.noImage" var="noImageLabel"/>
                <c:out value="${noImageLabel}"/>
            </div>
        </c:otherwise>
    </c:choose>
</div>
