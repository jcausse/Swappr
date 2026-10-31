<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:url value="/listing/by-product" var="filterAction"/>
<spring:message code="discovery.filters" var="filtersTitle"/>
<spring:message code="discovery.filter.category" var="categoryLabel"/>
<spring:message code="discovery.filter.subcategory" var="subcategoryLabel"/>
<spring:message code="discovery.filter.brand" var="brandLabel"/>
<spring:message code="discovery.filter.model" var="modelLabel"/>
<spring:message code="discovery.filter.yearMin" var="yearMinLabel"/>
<spring:message code="discovery.filter.yearMax" var="yearMaxLabel"/>
<spring:message code="discovery.filter.apply" var="applyLabel"/>
<spring:message code="discovery.filter.clear" var="clearLabel"/>
<spring:message code="discovery.filter.all" var="allLabel"/>

<paw:layout variant="wide" titleKey="byProduct.title">
    <form:form modelAttribute="productForm" action="${filterAction}" method="get" id="filterForm" class="flex flex-col gap-6">
        <div class="flex flex-col md:flex-row gap-8">
            <aside class="w-full md:w-56 shrink-0">
                <div class="sticky top-24">
                    <paw:card title="${filtersTitle}">
                        <div class="flex flex-col gap-4">
                            <paw:formSelect path="categoryId" label="${categoryLabel}" placeholder="${allLabel}" items="${categoryOptions}" />
                            <c:if test="${not empty subcategoryOptions}">
                                <paw:formSelect path="subcategoryId" label="${subcategoryLabel}" placeholder="${allLabel}" items="${subcategoryOptions}" />
                            </c:if>
                            <c:if test="${not empty brandOptions}">
                                <paw:formSelect path="brand" label="${brandLabel}" placeholder="${allLabel}" items="${brandOptions}" itemLabel="label" itemValue="value" />
                            </c:if>
                            <c:if test="${not empty modelOptions}">
                                <paw:formSelect path="model" label="${modelLabel}" placeholder="${allLabel}" items="${modelOptions}" itemLabel="label" itemValue="value" />
                            </c:if>

                            <div class="flex flex-row gap-2">
                                <paw:formInput path="yearMin" type="number" step="1" min="0" label="${yearMinLabel}" classname="min-w-0" />
                                <paw:formInput path="yearMax" type="number" step="1" min="0" label="${yearMaxLabel}" classname="min-w-0" />
                            </div>

                            <div class="flex flex-col gap-2 pt-1">
                                <paw:button text="${applyLabel}" type="submit" classname="w-full"/>
                                <paw:linkButton href="${filterAction}" text="${clearLabel}" variant="ghost" size="sm" role="secondary" />
                            </div>
                        </div>
                    </paw:card>
                </div>
            </aside>

            <section class="flex-1">
                <div class="flex items-center justify-between gap-4 mb-4">
                    <span class="text-sm text-black/60">
                        <spring:message code="byProduct.results.count" arguments="${fn:length(productSummaries)}"/>
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty productForm.subcategoryId}">
                        <div class="flex items-center justify-center h-64 text-black/50">
                            <spring:message code="byProduct.selectSubcategory"/>
                        </div>
                    </c:when>
                    <c:when test="${empty productSummaries}">
                        <div class="flex items-center justify-center h-64 text-black/50">
                            <spring:message code="byProduct.empty"/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            <c:forEach var="summary" items="${productSummaries}">
                                <paw:card title="${summary.brand} ${summary.model}" classname="h-full">
                                    <div class="flex flex-col gap-2 mt-2">
                                        <c:forEach var="info" items="${summary.listingInfoByCondition}">
                                            <spring:message code="condition.${info.condition}" var="conditionLabel"/>
                                            <div class="flex items-center justify-between gap-3 text-sm border-t border-black/5 pt-2 first:border-t-0 first:pt-0">
                                                <span class="text-black/70">
                                                    <c:out value="${conditionLabel}"/>
                                                    <span class="text-black/40">(<c:out value="${info.listingCount}"/>)</span>
                                                </span>
                                                <span class="font-semibold text-right whitespace-nowrap">
                                                    <c:choose>
                                                        <c:when test="${info.minPrice.amount == info.maxPrice.amount}">
                                                            $<c:out value="${info.minPrice.amount}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            $<c:out value="${info.minPrice.amount}"/> - $<c:out value="${info.maxPrice.amount}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </paw:card>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </div>
    </form:form>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const filterForm = document.getElementById('filterForm');

            // Reload the filters on change so dependent dropdowns get populated.
            // Each level clears the selects below it, since their options belong
            // to the previous selection: category -> subcategory -> brand -> model.
            function reloadOnChange(selectId, dependentIds) {
                const select = document.getElementById(selectId);
                if (!select) {
                    return;
                }
                select.addEventListener('change', function() {
                    dependentIds.forEach(function(id) {
                        const dependent = document.getElementById(id);
                        if (dependent) {
                            dependent.value = '';
                        }
                    });
                    filterForm.submit();
                });
            }

            reloadOnChange('categoryId', ['subcategoryId', 'brand', 'model']);
            reloadOnChange('subcategoryId', ['brand', 'model']);
            reloadOnChange('brand', ['model']);
        });
    </script>
</paw:layout>
