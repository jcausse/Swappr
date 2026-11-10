<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url value="/listing/new/choose-product" var="createListingUrl">
    <c:param name="modelId" value="${modelId}"/>
</c:url>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<paw:head titleKey="catalogRequest.modelExists.title"/>
<body class="min-h-screen bg-neutral-50 flex items-center justify-center p-6">
    <div class="max-w-md w-full text-center flex flex-col items-center gap-6 p-8 rounded-2xl border border-black/15 bg-white shadow-sm">
        <div class="flex flex-col items-center gap-2">
            <span class="px-3 py-1 text-xs font-bold tracking-wider uppercase rounded-full bg-amber-100 text-amber-800">
                <spring:message code="catalogRequest.modelExists.badge"/>
            </span>
            <h1 class="text-3xl font-extrabold text-neutral-900 mt-2">
                <spring:message code="catalogRequest.modelExists.title"/>
            </h1>
        </div>

        <p class="text-neutral-600 text-base">
            <spring:message code="catalogRequest.modelExists.message" arguments="${brandName},${modelName}"/>
        </p>

        <div class="flex flex-col sm:flex-row items-center gap-3">
            <a href="${createListingUrl}"
               class="inline-flex items-center justify-center font-semibold rounded-lg p-2.5 px-5 text-sm text-white bg-lime-600 hover:bg-lime-700 transition duration-150 shadow-sm focus-visible:outline outline-offset-2 outline-lime-600">
                <spring:message code="catalogRequest.modelExists.createListing"/>
            </a>
            <a href="<c:url value="/listing"/>"
               class="inline-flex items-center justify-center font-semibold rounded-lg p-2.5 px-5 text-sm text-black/60 hover:bg-black/5 transition duration-150 focus-visible:outline outline-offset-2 outline-black/30">
                <spring:message code="catalogRequest.modelExists.browseListings"/>
            </a>
        </div>
    </div>
</body>
</html>
