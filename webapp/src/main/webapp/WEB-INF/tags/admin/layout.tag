<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<paw:layout variant="wide" titleKey="${titleKey}">
    <div class="mb-8 flex flex-row items-center justify-between">
        <div>
            <h1 class="text-3xl font-bold"><c:out value="${title}"/></h1>
            <c:if test="${not empty subtitle}">
                <p class="text-black/60 mt-2"><c:out value="${subtitle}"/></p>
            </c:if>
        </div>
    </div>

    <c:if test="${not empty param.success}">
        <div class="mb-6">
            <spring:message code="admin.success.${param.success}" var="successMsg" text="${param.success}"/>
            <paw:banner text="${successMsg}" role="success" icon="check-circle" />
        </div>
    </c:if>

    <c:if test="${not empty param.error}">
        <div class="mb-6">
            <spring:message code="admin.error.${param.error}" var="errorMsg" text="${param.error}"/>
            <paw:banner text="${errorMsg}" role="danger" icon="alert-circle" />
        </div>
    </c:if>

    <div class="grid grid-cols-1 md:grid-cols-10 gap-6">
        <div class="md:col-span-3">
            <admin:sidebar />
        </div>

        <div class="md:col-span-7">
            <jsp:doBody />
        </div>
    </div>
</paw:layout>
