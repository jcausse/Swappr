<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="amount" required="true" type="java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%-- Renders the "Free" label for a zero amount, "<symbol> <amount>" (e.g. "US$ 1500.00") otherwise. Renders nothing for a missing amount (trade without cash). --%>
<c:if test="${not empty amount}">
    <c:choose>
        <c:when test="${amount.signum() == 0}">
            <spring:message code="listing.price.free" var="freeLabel"/>
            <c:out value="${freeLabel}"/>
        </c:when>
        <c:otherwise>
            <spring:message code="currency.symbol" var="currencySymbol"/>
            <c:out value="${currencySymbol} ${amount}"/>
        </c:otherwise>
    </c:choose>
</c:if>
