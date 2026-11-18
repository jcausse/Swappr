<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:url value="/listing/new/choose-product" var="chooseProductUrl"/>
<c:url value="/listing" var="listingsUrl"/>

<spring:message code="catalogRequest.success.title" var="successTitle"/>
<spring:message code="catalogRequest.success.message" var="successMessage"/>
<spring:message code="catalogRequest.success.createListing" var="createListingLabel"/>
<spring:message code="catalogRequest.success.browseListings" var="browseListingsLabel"/>

<paw:layout variant="narrow" titleKey="catalogRequest.success.title">
    <paw:card>
        <h1 class="text-2xl font-bold"><c:out value="${successTitle}"/></h1>
        <p class="text-sm text-black/60 mt-2"><c:out value="${successMessage}"/></p>

        <paw:divider />

        <div class="flex flex-col sm:flex-row gap-2 mt-4">
            <paw:linkButton href="${chooseProductUrl}" text="${createListingLabel}" variant="default" role="default" icon="plus"/>
            <paw:linkButton href="${listingsUrl}" text="${browseListingsLabel}" variant="outline" icon="search"/>
        </div>
    </paw:card>
</paw:layout>
