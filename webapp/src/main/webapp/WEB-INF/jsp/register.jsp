<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<paw:authLayout titleKey="register.title" linkHref="/login" linkTextKey="register.haveAccount">
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
</paw:authLayout>
