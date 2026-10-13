<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<c:url value="/listing/new/choose-product" var="backUrl"> <c:param name="productId" value="${detailsForm.productId}"/> </c:url>
<c:url value="/listing/new/details" var="detailsUrl"/>
<spring:message code="listing.new.back" var="backLabel"/>
<spring:message code="listing.new.changeProduct" var="changeProductLabel"/>

<paw:layout variant="narrow" titleKey="listing.new.title">
    <paw:card>
        <jsp:body>
            <c:choose>
                <c:when test="${not empty detailsForm.editListingId}">
                    <div class="mb-2 flex items-center gap-2">
                        <div class="flex-1 text-lime-600 font-medium text-sm flex items-center gap-2 bg-lime-100 p-3 rounded-lg">
                            <spring:message code="listing.new.editing" var="editingLabel"/>
                            <span><c:out value="${editingLabel}"/></span>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="mb-2 flex items-center gap-2">
                        <div class="flex-1 text-lime-600 font-medium text-sm flex items-center gap-2 p-3 rounded-lg">
                            <div class="w-6 h-6 rounded-full border-2 border-lime-600 flex items-center justify-center text-xs text-lime-600">✓</div>
                            <spring:message code="listing.new.step1" var="step1Label"/>
                            <span><c:out value="${step1Label}"/></span>
                        </div>
                        <div class="flex-1 text-lime-600 font-medium text-sm flex items-center gap-2 bg-lime-100 p-3 rounded-lg">
                            <div class="w-6 h-6 rounded-full border-2 border-lime-600 bg-lime-600 flex items-center justify-center text-xs text-lime-100">2</div>
                            <spring:message code="listing.new.step2" var="step2Label"/>
                            <span><c:out value="${step2Label}"/></span>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="text-sm text-black/60 mb-2">
                <spring:message code="listing.new.details.desc" var="detailsDesc"/>
                <c:out value="${detailsDesc}"/>
            </div>

            <paw:divider />

            <form:form id="detailsForm" modelAttribute="detailsForm" action="${detailsUrl}" method="post" class="flex flex-col gap-6 mt-4" enctype="multipart/form-data">
                <form:hidden path="productId"/>
                <form:hidden path="editListingId"/>

                <div class="flex flex-col">
                    <div class="text-xs text-black/70 font-medium">
                        <spring:message code="listing.new.product"/>
                    </div>
                    <div class="flex flex-row items-center justify-between">
                        <paw:product product="${product}" />
                        <paw:linkButton href="${backUrl}" text="${changeProductLabel}" variant="outline" />
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-4 items-start">
                    <spring:message code="listing.new.titleLabel" var="titleLabel"/>
                    <paw:formInput path="title" label="${titleLabel}" />

                    <spring:message code="listing.new.price" var="priceLabel"/>
                    <paw:formInput path="price" label="${priceLabel}" type="number" step="0.01" min="0" />

                    <spring:message code="listing.new.conditionLabel" var="conditionLabel"/>
                    <spring:message code="listing.new.condition.select" var="conditionPlaceholder"/>
                    <paw:formSelect path="condition" label="${conditionLabel}" placeholder="${conditionPlaceholder}"
                                    items="${conditionOptions}" itemLabel="label" itemValue="value" />
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-4 items-start">
                    <spring:message code="listing.new.acceptsTradeLabel" var="acceptsTradeLabel"/>
                    <paw:formCheckbox path="acceptsTrade" label="${acceptsTradeLabel}" />

                    <spring:message code="listing.new.acceptsShippingLabel" var="acceptsShippingLabel"/>
                    <paw:formCheckbox path="acceptsShipping" label="${acceptsShippingLabel}" />
                </div>

                <spring:message code="listing.new.descriptionLabel" var="descriptionLabel"/>
                <paw:formInput type="textarea" path="description" label="${descriptionLabel}" inputClassname="min-h-40 resize-none" />

                <spring:message code="listing.new.imagesLabel" var="imagesLabel"/>
                <paw:fileUpload path="images" label="${imagesLabel}" multiple="true" accept="image/*" />

                <div class="mt-2 flex justify-center gap-4">
                    <c:if test="${empty detailsForm.editListingId}">
                        <c:url value="/listing/new/choose-product" var="backUrl">
                            <c:param name="productId" value="${detailsForm.productId}"/>
                        </c:url>
                        <spring:message code="listing.new.back" var="backLabel"/>
                        <paw:linkButton href="${backUrl}" text="${backLabel}" size="lg" variant="outline" classname="w-40" />
                    </c:if>
                    <c:choose>
                        <c:when test="${not empty detailsForm.editListingId}">
                            <spring:message code="listing.new.updateListing" var="submitLabel"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="listing.new.submitListing" var="submitLabel"/>
                        </c:otherwise>
                    </c:choose>
                    <paw:button text="${submitLabel}" size="lg" classname="w-60" type="submit" variant="primary"/>
                </div>
            </form:form>
        </jsp:body>
    </paw:card>
</paw:layout>
