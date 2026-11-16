<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<html lang="${pageContext.response.locale.language}">
<paw:head titleKey="unauthorized.heading"/>
<body class="min-h-screen bg-neutral-50 flex items-center justify-center p-6">
    <div class="max-w-md w-full text-center flex flex-col items-center gap-6 p-8 rounded-2xl border border-black/15 bg-white shadow-sm">
        <div class="flex flex-col items-center gap-2">
            <span class="px-3 py-1 text-xs font-bold tracking-wider uppercase rounded-full bg-red-100 text-red-700">
                <spring:message code="unauthorized.badge"/>
            </span>
            <h1 class="text-3xl font-extrabold text-neutral-900 mt-2">
                <spring:message code="unauthorized.heading"/>
            </h1>
        </div>

        <p class="text-neutral-600 text-base">
            <spring:message code="unauthorized.message"/>
        </p>

        <a href="<c:url value="/login"/>"
           class="inline-flex items-center justify-center font-semibold rounded-lg p-2.5 px-5 text-sm text-white bg-sky-600 hover:bg-sky-700 transition duration-150 shadow-sm focus-visible:outline outline-offset-2 outline-sky-600">
            <spring:message code="unauthorized.login"/>
        </a>
    </div>
</body>
</html>
