<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<html lang="${pageContext.response.locale.language}">
<paw:head titleKey="register.title"/>
<body class="min-h-screen flex flex-col relative bg-neutral-50">
    <div class="fixed inset-0 z-0 overflow-hidden bg-neutral-900">
        <img src="<c:url value='/static-image/banner.png'/>" class="w-full h-full object-cover blur-md opacity-40 scale-105" alt=""/>
    </div>

    <%-- Logo at top --%>
    <div class="absolute top-10 left-0 right-0 flex justify-center z-20">
        <div class="w-96 px-6">
            <a href="<c:url value='/'/>" class="w-full flex justify-center items-center bg-white/90 backdrop-blur-sm p-4 rounded-2xl shadow-lg border border-black/10 transition-transform hover:scale-105">
                <img src="<c:url value='/static-image/logo.svg'/>" alt="Swappr Logo" class="h-12"/>
            </a>
        </div>
    </div>

    <main class="flex-grow flex flex-col items-center justify-center p-4 relative z-10">
        <h2 class="text-3xl font-bold mb-4 text-white drop-shadow-md"><spring:message code="register.title"/></h2>

        <div class="w-96 bg-white border border-black/10 shadow-2xl rounded-2xl p-6">
        <c:url value="/register" var="registerUrl"/>
        <form:form modelAttribute="userForm" action="${registerUrl}" method="post" enctype="multipart/form-data" cssClass="flex flex-col gap-4">
            <form:errors path="" element="div" cssClass="text-xs text-red-600"/>

            <spring:message code="field.username" var="usernameLabel"/>
            <paw:formInput path="username" label="${usernameLabel}" variant="outline"/>

            <spring:message code="register.displayName" var="displayNameLabel"/>
            <paw:formInput path="displayName" label="${displayNameLabel}" variant="outline"/>

            <spring:message code="register.email" var="emailLabel"/>
            <paw:formInput path="email" type="email" label="${emailLabel}" variant="outline"/>

            <spring:message code="register.profilePicture" var="profilePictureLabel"/>
            <paw:fileUpload path="profilePicture" label="${profilePictureLabel}" accept="image/*" />

            <spring:message code="field.password" var="passwordLabel"/>
            <paw:formInput path="password" type="password" label="${passwordLabel}" variant="outline"/>

            <spring:message code="field.confirmPassword" var="confirmPasswordLabel"/>
            <paw:formInput path="confirmPassword" type="password" label="${confirmPasswordLabel}" variant="outline"/>

            <spring:message code="register.submit" var="submitLabel"/>
            <paw:button text="${submitLabel}" type="submit"/>
        </form:form>
    </div>
    <a href="<c:url value="/login"/>" class="text-sm font-medium text-white drop-shadow-md hover:text-neutral-200 underline mt-4"><spring:message code="register.haveAccount"/></a>
    </main>

    <%-- Go Home button at bottom --%>
    <div class="absolute bottom-10 left-0 right-0 flex justify-center z-20">
        <div class="w-96 px-6">
            <c:url value="/" var="homeUrl"/>
            <spring:message code="auth.goHome" var="goHomeLabel"/>
            <paw:linkButton href="${homeUrl}" text="${goHomeLabel}" variant="outline" classname="w-full bg-white/90 backdrop-blur-sm shadow-lg border-black/10 hover:bg-white transition-colors text-black"/>
        </div>
    </div>
</body>
</html>
