<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="role" required="true" %>
<%@ attribute name="status" required="true" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%-- Statuses with no next step have no hint key: the empty default keeps the banner from rendering --%>
<spring:message code="offer.hint.${role}.${status}" text="" var="hintMsg"/>
<c:if test="${not empty hintMsg}">
    <paw:banner text="${hintMsg}" icon="info" role="info" classname="${classname}"/>
</c:if>
