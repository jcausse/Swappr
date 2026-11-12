<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="page" required="true" type="ar.edu.itba.paw.model.Page" %>
<%@ attribute name="baseUrl" required="true" %>
<%@ attribute name="pageParam" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<%--
  Renders Previous / Page X of Y / Next controls for a Page.
  Preserves every current query param except "page", so active filters/sorting survive navigation.
--%>
<c:set var="pageParamName" value="${not empty pageParam ? pageParam : 'page'}"/>

<c:if test="${page.totalPages > 1}">
    <c:url value="${baseUrl}" var="prevUrl">
        <c:forEach var="entry" items="${paramValues}">
            <c:if test="${entry.key ne pageParamName}">
                <c:forEach var="val" items="${entry.value}">
                    <c:param name="${entry.key}" value="${val}"/>
                </c:forEach>
            </c:if>
        </c:forEach>
        <c:param name="${pageParamName}" value="${page.page - 1}"/>
    </c:url>
    <c:url value="${baseUrl}" var="nextUrl">
        <c:forEach var="entry" items="${paramValues}">
            <c:if test="${entry.key ne pageParamName}">
                <c:forEach var="val" items="${entry.value}">
                    <c:param name="${entry.key}" value="${val}"/>
                </c:forEach>
            </c:if>
        </c:forEach>
        <c:param name="${pageParamName}" value="${page.page + 1}"/>
    </c:url>
    <c:url value="${baseUrl}" var="firstUrl">
        <c:forEach var="entry" items="${paramValues}">
            <c:if test="${entry.key ne pageParamName}">
                <c:forEach var="val" items="${entry.value}">
                    <c:param name="${entry.key}" value="${val}"/>
                </c:forEach>
            </c:if>
        </c:forEach>
        <c:param name="${pageParamName}" value="1"/>
    </c:url>
    <c:url value="${baseUrl}" var="lastUrl">
        <c:forEach var="entry" items="${paramValues}">
            <c:if test="${entry.key ne pageParamName}">
                <c:forEach var="val" items="${entry.value}">
                    <c:param name="${entry.key}" value="${val}"/>
                </c:forEach>
            </c:if>
        </c:forEach>
        <c:param name="${pageParamName}" value="${page.totalPages}"/>
    </c:url>

    <div class="flex items-center justify-center gap-1 mt-8">
        <paw:linkButton href="${firstUrl}" variant="outline" icon="chevrons-left" disabled="${not page.hasPrevious}" />
        <paw:linkButton href="${prevUrl}" variant="outline" icon="chevron-left" disabled="${not page.hasPrevious}" />

        <span class="px-3 py-1.5 text-sm text-black/60 min-w-40 text-center">
            <spring:message code="pagination.current" arguments="${page.page},${page.totalPages}"/>
        </span>

        <paw:linkButton href="${nextUrl}" variant="outline" icon="chevron-right" disabled="${not page.hasNext}" />
        <paw:linkButton href="${lastUrl}" variant="outline" icon="chevrons-right" disabled="${not page.hasNext}" />
    </div>
</c:if>
