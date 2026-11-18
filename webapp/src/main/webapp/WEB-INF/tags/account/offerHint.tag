<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="role" required="true" %>
<%@ attribute name="status" required="true" %>
<%@ attribute name="free" required="false" type="java.lang.Boolean" %>
<%@ attribute name="list" required="false" type="java.lang.Boolean" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%-- A free listing has nothing to pay, so its pending-payment step is a delivery step --%>
<c:set var="hintStatus" value="${free and status == 'PENDING_PAYMENT' ? 'PENDING_DELIVERY' : status}"/>
<%-- Statuses with no next step have no hint key: the empty default keeps the banner from rendering --%>
<%-- A list mixes free and paid offers, so it may have its own wording that covers both --%>
<spring:message code="offer.hint.${role}.${hintStatus}.list" text="" var="listHintMsg"/>
<spring:message code="offer.hint.${role}.${hintStatus}" text="" var="detailHintMsg"/>
<c:set var="hintMsg" value="${list and not empty listHintMsg ? listHintMsg : detailHintMsg}"/>
<c:if test="${not empty hintMsg}">
    <paw:banner text="${hintMsg}" icon="info" role="secondary" classname="${classname}"/>
</c:if>
