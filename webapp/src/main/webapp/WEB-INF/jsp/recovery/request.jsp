<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<paw:authLayout titleKey="recovery.request.title" linkHref="/login" linkTextKey="recovery.request.backToLogin">
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
</paw:authLayout>
