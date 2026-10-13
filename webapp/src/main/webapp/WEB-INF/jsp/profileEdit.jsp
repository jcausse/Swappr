<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<paw:layout variant="narrow" titleKey="profileEdit.title" titleParams="${user.displayName}">
    <h1 class="text-3xl font-bold mb-6"><spring:message code="profileEdit.heading"/></h1>
    <div class="bg-white border border-black/10 rounded-2xl p-6">
        <c:url value="/profile/edit" var="editUrl"/>
        <form:form modelAttribute="userEditForm" action="${editUrl}" method="post" enctype="multipart/form-data" cssClass="flex flex-col gap-4">
            <form:errors path="" element="div" cssClass="text-xs text-red-600"/>

            <spring:message code="register.displayName" var="displayNameLabel"/>
            <paw:formInput path="displayName" label="${displayNameLabel}" variant="outline"/>

            <spring:message code="profileEdit.profilePicture" var="profilePictureLabel"/>
            <paw:fileUpload path="profilePicture" label="${profilePictureLabel}" accept="image/*" />

            <spring:message code="profileEdit.newPassword" var="newPasswordLabel"/>
            <paw:formInput path="password" type="password" label="${newPasswordLabel}" variant="outline"/>

            <spring:message code="field.confirmPassword" var="confirmPasswordLabel"/>
            <paw:formInput path="confirmPassword" type="password" label="${confirmPasswordLabel}" variant="outline"/>

            <spring:message code="profileEdit.province" var="provinceLabel"/>
            <spring:message code="profileEdit.province.select" var="provincePlaceholder"/>
            <paw:formSelect path="provinceId" label="${provinceLabel}" placeholder="${provincePlaceholder}"
                            items="${provinces}" itemLabel="name" itemValue="id" variant="outline"/>

            <spring:message code="profileEdit.locationDetail" var="locationDetailLabel"/>
            <spring:message code="profileEdit.locationDetail.placeholder" var="locationDetailPlaceholder"/>
            <paw:formInput path="locationDetail" label="${locationDetailLabel}" placeholder="${locationDetailPlaceholder}" variant="outline"/>

            <spring:message code="profileEdit.language" var="languageLabel"/>
            <spring:message code="profileEdit.language.select" var="languagePlaceholder"/>
            <paw:formSelect path="preferredLanguage" label="${languageLabel}" placeholder="${languagePlaceholder}"
                            items="${languages}" itemLabel="label" itemValue="value" variant="outline"/>

            <div class="flex gap-2 mt-2">
                <spring:message code="profileEdit.submit" var="submitLabel"/>
                <paw:button text="${submitLabel}" type="submit"/>

                <c:url value="/profile" var="profileUrl"/>
                <spring:message code="profileEdit.cancel" var="cancelLabel"/>
                <paw:linkButton href="${profileUrl}" text="${cancelLabel}" variant="outline" role="secondary"/>
            </div>
        </form:form>
    </div>
</paw:layout>
