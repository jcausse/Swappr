<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:url value="/catalog-requests" var="requestUrl"/>
<c:url value="/catalog-requests/new" var="requestFormUrl"/>
<c:url value="/listing/new/choose-product" var="chooseProductUrl"/>

<spring:message code="catalogRequest.title" var="requestTitle"/>
<spring:message code="catalogRequest.subtitle" var="requestSubtitle"/>
<spring:message code="catalogRequest.typeLabel" var="typeLabel"/>
<spring:message code="catalogRequest.type.newBrand" var="newBrandTypeLabel"/>
<spring:message code="catalogRequest.type.existingBrand" var="existingBrandTypeLabel"/>
<spring:message code="catalogRequest.categoryLabel" var="categoryLabel"/>
<spring:message code="catalogRequest.categoryPlaceholder" var="categoryPlaceholder"/>
<spring:message code="catalogRequest.subcategoryLabel" var="subcategoryLabel"/>
<spring:message code="catalogRequest.subcategoryPlaceholder" var="subcategoryPlaceholder"/>
<spring:message code="catalogRequest.brandLabel" var="brandLabel"/>
<spring:message code="catalogRequest.brandPlaceholder" var="brandPlaceholder"/>
<spring:message code="catalogRequest.newBrandLabel" var="newBrandLabel"/>
<spring:message code="catalogRequest.newBrandPlaceholder" var="newBrandPlaceholder"/>
<spring:message code="catalogRequest.modelLabel" var="modelLabel"/>
<spring:message code="catalogRequest.modelPlaceholder" var="modelPlaceholder"/>
<spring:message code="catalogRequest.submit" var="submitLabel"/>
<spring:message code="catalogRequest.cancel" var="cancelLabel"/>

<c:set var="isExistingBrand" value="${requestForm.type.name() eq 'EXISTING_BRAND'}"/>

<paw:layout variant="narrow" titleKey="catalogRequest.title">
    <paw:card>
        <h1 class="text-2xl font-bold"><c:out value="${requestTitle}"/></h1>
        <p class="text-sm text-black/60 mt-2"><c:out value="${requestSubtitle}"/></p>

        <paw:divider />

        <form:form id="catalogRequestForm" modelAttribute="requestForm" action="${requestUrl}" method="post" class="flex flex-col gap-4 mt-4">
            <c:if test="${not empty formError}">
                <div class="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                    <spring:message code="${formError}"/>
                </div>
            </c:if>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <paw:formSelect
                    path="categoryId"
                    label="${categoryLabel}"
                    placeholder="${categoryPlaceholder}"
                    items="${categoryOptions}"
                    translateLabels="true"
                />

                <c:if test="${not empty requestForm.categoryId}">
                    <paw:formSelect
                        path="subcategoryId"
                        label="${subcategoryLabel}"
                        placeholder="${subcategoryPlaceholder}"
                        items="${subcategoryOptions}"
                        translateLabels="true"
                    />
                </c:if>
            </div>

            <div class="flex flex-col gap-2">
                <span class="text-xs text-black/70 font-medium"><c:out value="${typeLabel}"/></span>
                <div class="flex flex-col sm:flex-row gap-2">
                    <paw:radio path="type" id="requestTypeNew" value="NEW_BRAND" label="${newBrandTypeLabel}"/>
                    <paw:radio path="type" id="requestTypeExisting" value="EXISTING_BRAND" label="${existingBrandTypeLabel}"/>
                </div>
                <form:errors path="type" element="div" cssClass="text-xs text-red-600"/>
            </div>

            <div id="newBrandFields" class="flex flex-col gap-4" <c:if test="${isExistingBrand}">style="display: none;"</c:if>>
                <paw:formInput
                    path="newBrandName"
                    label="${newBrandLabel}"
                    placeholder="${newBrandPlaceholder}"
                    variant="outline"
                />
            </div>

            <div id="existingBrandFields" class="flex flex-col gap-4" <c:if test="${not isExistingBrand}">style="display: none;"</c:if>>
                <paw:formSelect
                    path="brandId"
                    label="${brandLabel}"
                    placeholder="${brandPlaceholder}"
                    items="${brandOptions}"
                    itemLabel="name"
                    itemValue="id"
                />
            </div>

            <paw:formInput
                path="modelName"
                label="${modelLabel}"
                placeholder="${modelPlaceholder}"
                variant="outline"
            />

            <div class="flex flex-col-reverse sm:flex-row justify-end gap-2 mt-2">
                <paw:linkButton href="${chooseProductUrl}" text="${cancelLabel}" variant="ghost" role="secondary"/>
                <paw:button text="${submitLabel}" type="submit" variant="primary" icon="send"/>
            </div>
        </form:form>
    </paw:card>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var categorySelect = document.getElementById('categoryId');
            var newBrandFields = document.getElementById('newBrandFields');
            var existingBrandFields = document.getElementById('existingBrandFields');
            var newBrandRadio = document.getElementById('requestTypeNew');
            var existingBrandRadio = document.getElementById('requestTypeExisting');

            if (categorySelect) {
                categorySelect.addEventListener('change', function () {
                    var url = '${requestFormUrl}';
                    if (categorySelect.value) {
                        url += '?categoryId=' + encodeURIComponent(categorySelect.value);
                    }
                    window.location.href = url;
                });
            }

            function updateFields() {
                var isExisting = existingBrandRadio && existingBrandRadio.checked;
                if (newBrandFields) {
                    newBrandFields.style.display = isExisting ? 'none' : '';
                }
                if (existingBrandFields) {
                    existingBrandFields.style.display = isExisting ? '' : 'none';
                }
            }

            if (newBrandRadio) {
                newBrandRadio.addEventListener('change', updateFields);
            }
            if (existingBrandRadio) {
                existingBrandRadio.addEventListener('change', updateFields);
            }
            updateFields();
        });
    </script>
</paw:layout>
