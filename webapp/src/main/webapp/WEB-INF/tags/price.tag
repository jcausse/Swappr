<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="amount" required="true" type="java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%-- Renders "<symbol> <amount>" (e.g. "US$ 1500.00"). Renders nothing for a missing amount (trade without cash). --%>
<c:if test="${not empty amount}">
    <spring:message code="currency.symbol" var="currencySymbol"/>
    <c:out value="${currencySymbol} ${amount}"/>
</c:if>
