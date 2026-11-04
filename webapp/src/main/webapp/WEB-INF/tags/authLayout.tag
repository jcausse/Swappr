<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="titleParams" required="false" %>
<%@ attribute name="linkHref" required="false" %>
<%@ attribute name="linkTextKey" required="false" %>
<%@ attribute name="secondaryLinkHref" required="false" %>
<%@ attribute name="secondaryLinkTextKey" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">

<paw:head titleKey="${titleKey}" titleParams="${titleParams}"/>

<body class="min-h-screen max-h-screen flex flex-col relative bg-neutral-50">
    <div class="fixed inset-0 z-0 overflow-hidden bg-neutral-900">
        <img src="<c:url value='/static-image/banner.png'/>" class="w-full h-full object-cover blur-md opacity-40 scale-105" alt=""/>
    </div>

    <main class="flex-grow flex flex-col items-center justify-center p-4 md:p-10 relative z-10 min-h-screen max-h-screen">
        <div class="w-96 px-6 mb-auto">
            <a href="<c:url value='/'/>" class="w-full flex justify-center items-center bg-white/90 backdrop-blur-sm p-4 rounded-2xl shadow-lg border border-black/10 transition-transform hover:scale-105">
                <spring:message code="a11y.logo" var="logoAlt"/>
                <img src="<c:url value='/static-image/logo.svg'/>" alt="<c:out value='${logoAlt}'/>" class="h-12"/>
            </a>
        </div>

        <h2 class="text-3xl font-bold mb-4 text-white drop-shadow-md mt-8">
            <spring:message code="${titleKey}" arguments="${titleParams}"/>
        </h2>

        <div class="w-96 bg-white border border-black/10 shadow-2xl rounded-2xl p-6 flex-shrink-1 overflow-y-auto">
            <jsp:doBody/>
        </div>

        <c:if test="${not empty linkHref and not empty linkTextKey}">
            <c:url value="${linkHref}" var="primaryLinkUrl"/>
            <a href="${primaryLinkUrl}" class="text-sm font-medium text-white drop-shadow-md hover:text-neutral-200 underline mt-4 ${empty secondaryLinkHref ? 'mb-8' : ''}">
                <spring:message code="${linkTextKey}"/>
            </a>
        </c:if>

        <c:if test="${not empty secondaryLinkHref and not empty secondaryLinkTextKey}">
            <c:url value="${secondaryLinkHref}" var="secondaryLinkUrl"/>
            <a href="${secondaryLinkUrl}" class="text-sm font-medium text-white drop-shadow-md hover:text-neutral-200 underline mt-2 mb-8">
                <spring:message code="${secondaryLinkTextKey}"/>
            </a>
        </c:if>

        <div class="w-96 px-6 mt-auto">
            <c:url value="/" var="homeUrl"/>
            <spring:message code="auth.goHome" var="goHomeLabel"/>
            <paw:linkButton href="${homeUrl}" text="${goHomeLabel}" variant="outline" classname="w-full bg-white/90 backdrop-blur-sm shadow-lg border-black/10 hover:bg-white transition-colors text-black"/>
        </div>
    </main>
</body>

</html>
