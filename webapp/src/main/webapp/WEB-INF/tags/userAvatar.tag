<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="user" required="true" type="ar.edu.itba.paw.model.User" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="avatarSize" value="${not empty size ? size : 'md'}"/>
<c:set var="avatarClass" value="${not empty classname ? classname : ''}"/>

<c:set var="sizeClassnames" value="${
  avatarSize eq 'xs' ? 'w-6 h-6'
  : avatarSize eq 'sm' ? 'w-8 h-8'
  : avatarSize eq 'lg' ? 'w-16 h-16'
  : avatarSize eq 'xl' ? 'w-24 h-24'
  : avatarSize eq '2xl' ? 'w-32 h-32'
  : 'w-10 h-10'
}"/>

<div class="rounded-full border border-black/10 grid place-items-center overflow-hidden flex-shrink-0 ${sizeClassnames} ${avatarClass}">
    <c:choose>
        <c:when test="${user.getImageId().isPresent()}">
            <spring:message code="userAvatar.alt" var="avatarAlt"><spring:argument value="${user.displayName}"/></spring:message>
            <img
                src="<c:url value='/image/${user.getImageId().get()}'/>"
                alt="<c:out value='${avatarAlt}'/>"
                class="w-full h-full object-cover"
            >
        </c:when>
        <c:otherwise>
            <spring:message code="userAvatar.defaultAlt" var="avatarAlt"/>
            <img
                src="<c:url value='/static-image/defaultProfilePicture.svg'/>"
                alt="<c:out value='${avatarAlt}'/>"
                class="w-full h-full object-cover"
            >
        </c:otherwise>
    </c:choose>
</div>
