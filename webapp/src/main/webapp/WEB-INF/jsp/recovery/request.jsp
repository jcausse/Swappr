<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html lang="${pageContext.response.locale.language}">
<paw:head titleKey="recovery.request.title"/>
<body class="min-h-screen flex flex-col relative bg-neutral-50">

    <div class="fixed inset-0 z-0 overflow-hidden bg-neutral-900">
        <img src="<c:url value='/static-image/banner.png'/>" class="w-full h-full object-cover blur-md opacity-40 scale-105" alt=""/>
    </div>

    <%-- Logo at top --%>
    <div class="absolute top-10 left-0 right-0 flex justify-center z-20">
        <div class="w-96 px-6">
            <a href="<c:url value='/'/>" class="w-full flex justify-center items-center bg-white/90 backdrop-blur-sm p-4 rounded-2xl shadow-lg border border-black/10 transition-transform hover:scale-105">
                <spring:message code="a11y.logo" var="logoAlt"/>
                <img src="<c:url value='/static-image/logo.svg'/>" alt="<c:out value='${logoAlt}'/>" class="h-12"/>
            </a>
        </div>
    </div>

    <main class="flex-grow flex flex-col items-center justify-center p-4 relative z-10">
        <h2 class="text-3xl font-bold mb-4 text-white drop-shadow-md"><spring:message code="recovery.request.title"/></h2>

        <div class="w-96 bg-white border border-black/10 shadow-2xl rounded-2xl p-6">
        <c:url value="/recovery/request" var="requestUrl"/>
        <form:form modelAttribute="passwordRecoveryRequestForm" action="${requestUrl}" method="post" class="flex flex-col gap-4">
            <p class="text-sm text-neutral-600 mb-1">
                <spring:message code="recovery.request.subtitle"/>
            </p>

            <spring:message code="recovery.request.label.usernameOrEmail" var="usernameOrEmailLabel"/>
            <spring:message code="recovery.request.placeholder.usernameOrEmail" var="usernameOrEmailPlaceholder"/>
            <paw:formInput path="usernameOrEmail" label="${usernameOrEmailLabel}" placeholder="${usernameOrEmailPlaceholder}" variant="outline"/>

            <spring:message code="recovery.request.submit" var="submitLabel"/>
            <paw:button text="${submitLabel}" type="submit"/>
        </form:form>
    </div>

    <a href="<c:url value="/login"/>" class="text-sm font-medium text-white drop-shadow-md hover:text-neutral-200 underline mt-4">
        <spring:message code="recovery.request.backToLogin"/>
    </a>
    </main>

    <%-- Go Home button at bottom --%>
    <div class="absolute bottom-10 left-0 right-0 flex justify-center z-20">
        <div class="w-96 px-6">
            <c:url value="/" var="homeUrl"/>
            <spring:message code="auth.goHome" var="goHomeLabel"/>
            <paw:linkButton href="${homeUrl}" text="${goHomeLabel}" variant="outline" classname="w-full bg-white/90 backdrop-blur-sm shadow-lg border-black/10 hover:bg-white transition-colors text-black"/>
        </div>
    </div>

    <c:if test="${codeSent}">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
            <div class="bg-white rounded-2xl p-6 w-96 max-w-full border border-black/10 shadow-xl flex flex-col items-center text-center gap-4">
                <div class="w-12 h-12 rounded-full bg-lime-100 flex items-center justify-center text-lime-600">
                    <paw:icon name="mail-check" />
                </div>
                <h3 class="text-xl font-bold text-neutral-900">
                    <spring:message code="recovery.request.modal.title"/>
                </h3>
                <p class="text-sm text-neutral-600">
                    <spring:message code="recovery.request.modal.message"/>
                </p>
                <c:url value="/recovery/verification" var="continueUrl">
                    <c:param name="usernameOrEmail" value="${usernameOrEmail}"/>
                </c:url>
                <spring:message code="recovery.request.modal.continue" var="continueLabel"/>
                <paw:linkButton href="${continueUrl}" text="${continueLabel}" classname="w-full"/>
            </div>
        </div>
    </c:if>
</body>
</html>
