<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>

<c:url value="/account/my-offers/${offer.id}/shipping" var="uploadAction"/>
<c:url value="/account/incoming-offers" var="backUrl"/>
<spring:message code="offer.proofOfShipping.title" var="titleMsg"/>
<spring:message code="offer.proofOfShipping.subtitle" var="subtitleMsg"/>
<spring:message code="offer.proofOfShipping.trackingNumberLabelForm" var="trackingNumberLabelMsg"/>
<spring:message code="offer.proofOfShipping.trackingNumberPlaceholder" var="trackingNumberPlaceholderMsg"/>
<spring:message code="offer.proofOfShipping.uploadLabel" var="uploadLabelMsg"/>
<spring:message code="offer.detail.back" var="backLabelMsg"/>
<spring:message code="offer.proofOfShipping.submit" var="submitLabelMsg"/>
<spring:message code="offer.proofOfShipping.fileHint" var="fileHintMsg"/>
<spring:message code="offer.proofOfShipping.fileLabelForm" var="fileLabelMsg"/>

<account:layout titleKey="offer.proofOfShipping.title"  title="${titleMsg}" subtitle="${subtitleMsg}">
    <!-- Back link -->
    <div class="mb-4">
        <paw:linkButton href="${backUrl}" text="${backLabelMsg}" variant="ghost" icon="chevron-left" classname="justify-start" />
    </div>

    <paw:card classname="flex flex-col gap-4">
        <div class="flex flex-row gap-4">
            <paw:listingImage listing="${offer.listing}" size="xl" />

            <div class="flex-1 min-w-0 flex flex-col justify-center">
                <h2 class="text-xl font-semibold truncate"><c:out value="${offer.listing.title}"/></h2>
                <p class="text-sm text-black/60 mt-1 truncate">
                    <c:out value="${offer.listing.product.brand}"/>
                    <c:out value="${offer.listing.product.model}"/>
                    (<c:out value="${offer.listing.product.year}"/>)
                </p>
            </div>
        </div>

        <div class="flex flex-col gap-4">
            <div class="flex flex-col">
                <spring:message code="offer.detail.amount" var="amountLabel"/>
                <span class="text-sm text-black/60"><c:out value="${amountLabel}"/></span>
                <p class="text-3xl font-bold">
                    $<c:out value="${offer.amount}"/>
                </p>
            </div>
        </div>

        <paw:divider />

        <form:form method="POST" action="${uploadAction}" enctype="multipart/form-data" class="space-y-4" modelAttribute="proofOfShippingUploadForm">
            <form:errors path="*" element="div" cssClass="text-xs text-red-600 mb-4" />
            <div>
                <spring:message code="offer.proofOfShipping.trackingNumberLabelForm" var="trackingNumberLabelMsg"/>
                <paw:formInput path="trackingNumber" label="${trackingNumberLabelMsg}" placeholder="${trackingNumberPlaceholderMsg}" />
            </div>
            <div>
                <paw:fileUpload path="file" label="${fileLabelMsg}" accept="image/*,application/pdf" />
                <p class="text-xs text-black/50 mt-1">
                    <c:out value="${fileHintMsg}"/>
                </p>
            </div>

            <paw:button type="submit" variant="default" size="lg" classname="w-full" text="${submitLabelMsg}"/>
        </form:form>
    </paw:card>
</account:layout>
