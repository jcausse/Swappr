<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<paw:authLayout titleKey="login.title" linkHref="/register" linkTextKey="login.noAccount">
    <c:url value="/login" var="loginUrl"/>
    <form action="${loginUrl}" method="post" class="flex flex-col gap-4">
        <c:if test="${param.verified != null}">
            <div class="text-xs text-lime-700 bg-lime-50 border border-lime-200 rounded-lg p-3 font-medium">
                <spring:message code="login.verifiedSuccess"/>
            </div>
        </c:if>
        <c:if test="${param.error != null}">
            <c:choose>
                <c:when test="${sessionScope.SPRING_SECURITY_LAST_EXCEPTION['class'].simpleName == 'DisabledException'}">
                    <div class="text-xs text-amber-800 bg-amber-50 border border-amber-200 rounded-lg p-3">
                        <p><spring:message code="login.error.unverified"/></p>
                        <div class="mt-2">
                            <c:url value="/verify" var="verifyUrl"/>
                            <a href="${verifyUrl}" class="underline font-semibold text-amber-900 hover:text-amber-950">
                                <spring:message code="login.error.unverified.verifyLink"/>
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:when test="${sessionScope.SPRING_SECURITY_LAST_EXCEPTION['class'].simpleName == 'LockedException'}">
                    <div class="text-xs text-red-800 bg-red-50 border border-red-200 rounded-lg p-3">
                        <p><spring:message code="login.error.suspended"/></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-xs text-red-600 font-medium">
                        <spring:message code="login.error.invalidCredentials"/>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <spring:message code="field.usernameOrEmail" var="usernameOrEmailLabel"/>
        <paw:input id="usernameOrEmail" name="usernameOrEmail" label="${usernameOrEmailLabel}" variant="outline"/>

        <spring:message code="field.password" var="passwordLabel"/>
        <paw:input id="password" name="password" type="password" label="${passwordLabel}" variant="outline"/>

        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
                <input
                    type="checkbox"
                    id="rememberMe"
                    name="rememberMe"
                    checked
                    class="w-4 h-4 text-lime-600 border-black/20 outline-0 outline-offset-0 outline-lime-600/30 focus-visible:outline-2 accent-lime-600"
                >
                <label for="rememberMe" class="text-sm font-medium select-none">
                    <spring:message code="field.rememberMe"/>
                </label>
            </div>
            <a href="<c:url value="/recovery/request"/>" class="text-xs text-neutral-600 hover:text-neutral-900 underline select-none">
                <spring:message code="login.forgotPassword"/>
            </a>
        </div>

        <spring:message code="login.submit" var="submitLabel"/>
        <paw:button text="${submitLabel}" type="submit"/>
    </form>
</paw:authLayout>
