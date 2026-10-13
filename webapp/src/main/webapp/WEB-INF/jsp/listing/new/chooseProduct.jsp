<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<c:url value="/listing/new/choose-product" var="chooseProductUrl"/>

<paw:layout variant="narrow" titleKey="listing.new.title">
    <paw:card>
        <div class="mb-2 flex items-center gap-2">
            <div class="flex-1 text-lime-600 font-medium text-sm flex items-center gap-2 bg-lime-100 p-3 rounded-lg">
                <div class="w-6 h-6 rounded-full border-2 border-lime-600 bg-lime-600 flex items-center justify-center text-xs text-lime-100">1</div>
                <spring:message code="listing.new.step1" var="step1Label"/>
                <span><c:out value="${step1Label}"/></span>
            </div>
            <div class="flex-1 text-black/40 font-medium text-sm flex items-center gap-2 p-3 rounded-lg">
                <div class="w-6 h-6 rounded-full border-2 border-black/40 flex items-center justify-center text-xs text-black/40">2</div>
                <spring:message code="listing.new.step2" var="step2Label"/>
                <span><c:out value="${step2Label}"/></span>
            </div>
        </div>

        <div class="text-sm text-black/60 mb-2">
            <spring:message code="listing.new.chooseProduct.desc" var="chooseProductDesc"/>
            <c:out value="${chooseProductDesc}"/>
        </div>

        <paw:divider />

        <form:form id="chooseProductForm" modelAttribute="chooseProductForm" action="${chooseProductUrl}" method="post" class="flex flex-col gap-6 mt-4">
            <form:hidden path="step"/>
            <form:hidden path="isAutoSubmit"/>
            <form:hidden path="previousCategoryId"/>
            <form:hidden path="previousSubcategoryId"/>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-4 items-start">
                <%-- Step 1: Category Selection (always visible) --%>
                <spring:message code="listing.new.category" var="categoryLabel"/>
                <spring:message code="listing.new.category.select" var="categoryPlaceholder"/>
                <paw:formSelect path="categoryId" label="${categoryLabel}" placeholder="${categoryPlaceholder}" items="${categoryOptions}" />

                <%-- Step 2: Subcategory Selection (visible when category selected) --%>
                <c:if test="${chooseProductForm.step ge 2}">
                    <spring:message code="listing.new.subcategory" var="subcategoryLabel"/>
                    <spring:message code="listing.new.subcategory.select" var="subcategoryPlaceholder"/>
                    <paw:formSelect path="subcategoryId" label="${subcategoryLabel}" placeholder="${subcategoryPlaceholder}" items="${subcategoryOptions}" />
                </c:if>
            </div>

            <%-- Step 3: Product Selection (visible when subcategory selected) --%>
            <c:if test="${chooseProductForm.step ge 3}">
                <div class="flex flex-row gap-4 items-start">
                    <spring:message code="listing.new.filter.brand" var="filterBrandLabel"/>
                    <spring:message code="listing.new.filter.brand.select" var="brandPlaceholder"/>
                    <spring:message code="listing.new.filter.brand.other" var="brandOther"/>
                    <paw:formSelect path="newProductBrand" label="${filterBrandLabel}" placeholder="${brandPlaceholder}" items="${brands}" plainStrings="true" includeOther="true" otherValue="__OTHER__" otherLabel="${brandOther}" classname="flex-1 grow" />

                    <c:if test="${chooseProductForm.newProductBrand == '__OTHER__'}">
                        <spring:message code="listing.new.filter.brand.otherInput" var="otherBrandLabel"/>
                        <spring:message code="listing.new.filter.brand.otherInput.placeholder" var="otherBrandPlaceholder"/>
                        <paw:formInput path="otherBrand" label="${otherBrandLabel}" placeholder="${otherBrandPlaceholder}" classname="flex-1 grow" />
                    </c:if>
                </div>

                <div class="flex flex-row gap-4 items-start">
                    <spring:message code="listing.new.filter.model" var="filterModelLabel"/>
                    <spring:message code="listing.new.filter.model.select" var="modelPlaceholder"/>
                    <spring:message code="listing.new.filter.model.other" var="modelOther"/>
                    <paw:formSelect path="newProductModel" label="${filterModelLabel}" placeholder="${modelPlaceholder}" items="${models}" plainStrings="true" disabled="${modelsEmpty}" includeOther="true" otherValue="__OTHER__" otherLabel="${modelOther}" classname="flex-1 grow" />

                    <c:choose>
                        <c:when test="${chooseProductForm.newProductModel == '__OTHER__'}">
                            <spring:message code="listing.new.filter.model.otherInput" var="otherModelLabel"/>
                            <spring:message code="listing.new.filter.model.otherInput.placeholder" var="otherModelPlaceholder"/>
                            <paw:formInput path="otherModel" label="${otherModelLabel}" placeholder="${otherModelPlaceholder}" classname="flex-1 grow" />
                        </c:when>
                        <c:when test="${chooseProductForm.newProductBrand == '__OTHER__'}">
                            <spring:message code="listing.new.filter.model.otherInput" var="otherModelLabel"/>
                            <spring:message code="listing.new.filter.model.otherInput.placeholder" var="otherModelPlaceholder"/>
                            <paw:formInput path="otherModel" label="${otherModelLabel}" placeholder="${otherModelPlaceholder}" classname="flex-1 grow" />
                        </c:when>
                    </c:choose>

                    <spring:message code="listing.new.filter.year" var="filterYearLabel"/>
                    <spring:message code="listing.new.filter.year.select" var="yearPlaceholder"/>
                    <paw:formInput path="newProductYear" label="${filterYearLabel}" placeholder="${yearPlaceholder}" type="number" classname="w-32" />
                </div>
            </c:if>

            <div class="mt-2 flex justify-center gap-4">
                <spring:message code="listing.new.next" var="nextLabel"/>
                <paw:button text="${nextLabel}" size="lg" classname="w-60" type="submit" variant="primary"/>
            </div>
        </form:form>
    </paw:card>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const categorySelect = document.getElementById('categoryId');
            const subcategorySelect = document.getElementById('subcategoryId');
            const brandSelect = document.getElementById('newProductBrand');
            const modelSelect = document.getElementById('newProductModel');
            const yearInput = document.getElementById('newProductYear');
            const isAutoSubmitInput = document.getElementById('isAutoSubmit');

            function submitForm() {
                if (isAutoSubmitInput) {
                    isAutoSubmitInput.value = 'true';
                }
                document.querySelector('form#chooseProductForm').submit();
            }

            if (categorySelect) {
                categorySelect.addEventListener('change', submitForm);
            }
            if (subcategorySelect) {
                subcategorySelect.addEventListener('change', submitForm);
            }
            if (brandSelect) {
                brandSelect.addEventListener('change', submitForm);
            }
            if (modelSelect) {
                modelSelect.addEventListener('change', submitForm);
            }
        });
    </script>
</paw:layout>
