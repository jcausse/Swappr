<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="offer" required="true" type="ar.edu.itba.paw.model.Offer" %>
<%@ attribute name="role" required="true" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<spring:message code="offer.hint.${role}.${offer.status.name()}" var="hintMsg"/>
<p class="text-sm text-black/60 flex flex-row gap-1 items-center ${classname}">
    <span aria-hidden="true"><paw:icon name="info"/></span>
    <c:out value="${hintMsg}"/>
</p>
