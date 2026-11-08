<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<c:url value="/listing/new/choose-product" var="backUrl">
    <c:param name="modelId" value="${detailsForm.modelId}"/>
    <c:param name="year" value="${detailsForm.year}"/>
</c:url>
<c:url value="${actionUrl}" var="detailsUrl"/>
<spring:message code="listing.new.back" var="backLabel"/>
<spring:message code="listing.new.changeProduct" var="changeProductLabel"/>

<paw:layout variant="narrow" titleKey="listing.new.title">
    <paw:card>
        <jsp:body>
            <c:choose>
                <c:when test="${edit}">
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
                <form:hidden path="modelId"/>
                <form:hidden path="year"/>

                <div class="flex flex-col">
                    <div class="text-xs text-black/70 font-medium">
                        <spring:message code="listing.new.product"/>
                    </div>
                    <div class="flex flex-row items-center justify-between">
                        <paw:product model="${model}" year="${detailsForm.year}" />
                        <c:if test="${not edit}">
                            <paw:linkButton href="${backUrl}" text="${changeProductLabel}" variant="outline" />
                        </c:if>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-4 items-start">
                    <div class="md:col-span-2">
                        <spring:message code="listing.new.titleLabel" var="titleLabel"/>
                        <paw:formInput path="title" label="${titleLabel}" />
                    </div>

                    <div class="flex flex-col gap-4">
                        <spring:message code="listing.new.price" var="priceLabel"/>
                        <spring:message code="currency.symbol" var="currencySymbol"/>
                        <div class="flex flex-col gap-1">
                            <label for="price" class="text-xs text-black/70 font-medium"><c:out value="${priceLabel}"/></label>
                            <div class="flex items-start gap-2">
                                <span class="py-1 border border-transparent text-sm text-black/70"><c:out value="${currencySymbol}"/></span>
                                <paw:formInput path="price" type="number" step="0.01" min="0" classname="flex-1"/>
                            </div>
                            <spring:message code="listing.new.price.freeHint" var="freeHint"/>
                            <p class="text-sm text-black/60 mt-1"><c:out value="${freeHint}"/></p>
                        </div>

                        <c:choose>
                            <c:when test="${empty priceSummary}">
                                <spring:message code="listing.new.priceSummary.empty" var="priceSummaryEmpty"/>
                                <paw:banner text="${priceSummaryEmpty}" icon="info" role="secondary"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="listing.new.priceSummary.title" var="priceSummaryTitle"/>
                                <div class="flex flex-col gap-2">
                                    <span class="text-sm text-black/70 font-medium"><c:out value="${priceSummaryTitle}"/></span>
                                    <div class="flex flex-col gap-1">
                                        <c:forEach var="info" items="${priceSummary.listingInfoByCondition}">
                                            <spring:message code="condition.${info.condition}" var="infoConditionLabel"/>
                                            <div class="flex w-full items-center gap-2 text-sm">
                                                <span class="text-black font-medium">
                                                    <c:out value="${infoConditionLabel}"/>
                                                    <span class="text-black/40 font-normal">(<c:out value="${info.listingCount}"/>)</span>
                                                </span>
                                                <span class="ml-auto font-semibold text-black text-sm whitespace-nowrap">
                                                    <c:choose>
                                                        <c:when test="${info.minPrice.amount == info.maxPrice.amount}">
                                                            <paw:price amount="${info.minPrice.amount}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <paw:price amount="${info.minPrice.amount}"/> - <c:out value="${info.maxPrice.amount}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="flex flex-col gap-4">
                        <spring:message code="listing.new.conditionLabel" var="conditionLabel"/>
                        <spring:message code="listing.new.condition.select" var="conditionPlaceholder"/>
                        <paw:formSelect path="condition" label="${conditionLabel}" placeholder="${conditionPlaceholder}"
                                        items="${conditionOptions}" itemLabel="label" itemValue="value" translateLabels="true" />

                        <spring:message code="listing.new.acceptsTradeLabel" var="acceptsTradeLabel"/>
                        <spring:message code="listing.new.acceptsTrade.freeHint" var="acceptsTradeFreeHint"/>
                        <div>
                            <paw:formCheckbox path="acceptsTrade" label="${acceptsTradeLabel}" />
                            <p id="acceptsTradeFreeHint" class="hidden text-sm text-black/60 mt-1"><c:out value="${acceptsTradeFreeHint}"/></p>
                        </div>

                        <spring:message code="listing.new.acceptsShippingLabel" var="acceptsShippingLabel"/>
                        <paw:formCheckbox path="acceptsShipping" label="${acceptsShippingLabel}" />
                    </div>
                </div>

                <spring:message code="listing.new.imagesLabel" var="imagesLabel"/>
                <paw:fileUpload path="images" label="${imagesLabel}" multiple="true" accept="image/*" />

                <spring:message code="listing.new.descriptionLabel" var="descriptionLabel"/>
                <paw:formInput type="textarea" path="description" label="${descriptionLabel}" inputClassname="min-h-40 resize-none" />

                <div class="mt-2 flex justify-center gap-4">
                    <c:if test="${not edit}">
                        <c:url value="/listing/new/choose-product" var="backUrl">
                            <c:param name="modelId" value="${detailsForm.modelId}"/>
                            <c:param name="year" value="${detailsForm.year}"/>
                        </c:url>
                        <spring:message code="listing.new.back" var="backLabel"/>
                        <paw:linkButton href="${backUrl}" text="${backLabel}" size="lg" variant="outline" classname="w-40" />
                    </c:if>
                    <c:choose>
                        <c:when test="${edit}">
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

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const priceInput = document.getElementById('price');
            const acceptsTradeCheckbox = document.getElementById('acceptsTrade');
            const freeHint = document.getElementById('acceptsTradeFreeHint');

            function syncAcceptsTrade() {
                const isFree = priceInput.value !== '' && parseFloat(priceInput.value) === 0;
                if (isFree) {
                    acceptsTradeCheckbox.checked = false;
                }
                acceptsTradeCheckbox.disabled = isFree;
                freeHint.classList.toggle('hidden', !isFree);
            }

            priceInput.addEventListener('input', syncAcceptsTrade);
            syncAcceptsTrade();
        });
    </script>
</paw:layout>
