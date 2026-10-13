<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<paw:layout titleKey="checkout.title">
    <div class="flex flex-row gap-4">
        <div class="flex-1 min-w-md">
            <paw:card>
                <div class="flex flex-col gap-4">
                    <h1 class="text-2xl font-semibold"><spring:message code="checkout.form.title"/></h1>

                    <form:form modelAttribute="checkoutForm" method="POST" class="flex flex-col gap-6">
                        <form:hidden path="listingId"/>

                        <spring:message code="checkout.form.offerType" var="offerTypeLabel"/>
                        <div class="flex flex-col gap-3">
                            <div class="flex flex-col gap-2">
                                <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                    <form:radiobutton path="offerType" value="full" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                    <spring:message code="checkout.form.fullPrice" var="fullPriceLabel"/>
                                    <span class="text-sm text-black/90"><c:out value="${fullPriceLabel}"/> - $<c:out value="${listing.price.getAmount()}"/></span>
                                </label>

                                <div class="flex flex-row gap-2 items-center justify-between">
                                    <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                        <form:radiobutton path="offerType" value="custom" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                        <spring:message code="checkout.form.customPrice" var="customPriceLabel"/>
                                        <span class="text-sm text-black/90"><c:out value="${customPriceLabel}"/></span>
                                    </label>

                                    <spring:message code="checkout.form.customAmount.placeholder" var="customAmountPlaceholder"/>
                                    <div id="customAmountField" class="hidden w-1/2">
                                        <paw:formInput path="customAmount" type="number" step="0.01" placeholder="${customAmountPlaceholder}" variant="outline"/>
                                    </div>
                                </div>

                                <c:if test="${listing.acceptsTrade and not empty userListings}">
                                    <div class="flex flex-col gap-2">
                                        <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                            <form:radiobutton path="offerType" value="trade" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                            <spring:message code="checkout.form.tradePrice" var="tradePriceLabel"/>
                                            <span class="text-sm text-black/90"><c:out value="${tradePriceLabel}"/></span>
                                        </label>

                                        <div id="tradeFields" class="hidden">
                                            <spring:message code="checkout.form.tradeSelectListing" var="tradeSelectListingLabel"/>
                                            <paw:formSelect path="offeredListingId" label="${tradeSelectListingLabel}" items="${userListings}" itemValue="id" itemLabel="title" placeholder="${tradeSelectListingLabel}" variant="outline" classname="w-full max-w-md" />

                                            <spring:message code="checkout.form.tradeAmount" var="tradeAmountLabel"/>
                                            <spring:message code="checkout.form.tradeAmount.placeholder" var="tradeAmountPlaceholder"/>
                                            <div class="w-full max-w-md">
                                                <paw:formInput path="tradeAmount" type="number" step="0.01" min="0" placeholder="${tradeAmountPlaceholder}" label="${tradeAmountLabel}" variant="outline"/>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <spring:message code="checkout.form.message" var="messageLabel"/>
                        <spring:message code="checkout.form.message.placeholder" var="messagePlaceholder"/>
                        <paw:formInput path="message" type="textarea" label="${messageLabel}" placeholder="${messagePlaceholder}" variant="outline" inputClassname="min-h-40 resize-none"/>

                        <spring:message code="checkout.form.submit" var="submitLabel"/>
                        <paw:button type="submit" size="lg" classname="w-full" text="${submitLabel}"/>
                    </form:form>
                </div>
            </paw:card>
        </div>

        <div class="flex-1 min-w-md flex flex-col">
            <c:set var="coverImageUrl" value=""/>
            <c:forEach items="${listing.imageIds}" var="imageId" varStatus="status">
                <c:if test="${status.first}">
                    <c:url value="/image/${imageId}" var="coverImageUrl"/>
                </c:if>
            </c:forEach>

            <spring:message code="category.${listing.product.subcategory.category.name}" var="productCategory" />
            <spring:message code="subcategory.${listing.product.subcategory.name}" var="productSubcategory" />
            <spring:message code="card.noImage" var="noImageLabel"/>
            <paw:card showImage="true"
                title="${listing.title}"
                subtitle="${listing.product.brand} ${listing.product.model} (${listing.product.year}) — ${productCategory} / ${productSubcategory}"
                imageUrl="${coverImageUrl}"
                imageAlt="${listing.title}"
                noImageLabel="${noImageLabel}"
            >
                <div class="flex flex-col gap-4 mt-auto">
                    <paw:user user="${listing.creator}" />

                    <paw:divider />

                    <div class="flex flex-col">
                        <spring:message code="checkout.summary.price" var="priceLabel"/>
                        <span class="text-sm text-black/60"><c:out value="${priceLabel}"/></span>
                        <p class="text-3xl font-bold">$<c:out value="${listing.price.getAmount()}"/></p>
                    </div>

                    <spring:message code="checkout.summary.condition" var="conditionLabel"/>
                    <div class="flex items-center gap-2">
                        <span class="text-sm text-black/60"><c:out value="${conditionLabel}"/></span>
                        <span class="px-2 py-0.5 text-xs font-medium rounded-full bg-neutral-100 text-black/60">
                            <spring:message code="condition.${listing.condition}"/>
                        </span>
                    </div>
                </div>
            </paw:card>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const fullRadio = document.querySelector('input[name="offerType"][value="full"]');
            const customRadio = document.querySelector('input[name="offerType"][value="custom"]');
            const tradeRadio = document.querySelector('input[name="offerType"][value="trade"]');
            const customField = document.getElementById('customAmountField');
            const customInput = document.querySelector('input[name="customAmount"]');
            const tradeFields = document.getElementById('tradeFields');
            const tradeListingSelect = document.querySelector('select[name="offeredListingId"]');
            const tradeAmountInput = document.querySelector('input[name="tradeAmount"]');

            function toggleFields() {
                if (customRadio.checked) {
                    customField.classList.remove('hidden');
                    customInput.required = true;
                } else {
                    customField.classList.add('hidden');
                    customInput.required = false;
                }

                if (tradeRadio && tradeRadio.checked) {
                    tradeFields.classList.remove('hidden');
                    if (tradeListingSelect) tradeListingSelect.required = true;
                    if (tradeAmountInput) tradeAmountInput.required = true;
                } else {
                    tradeFields.classList.add('hidden');
                    if (tradeListingSelect) tradeListingSelect.required = false;
                    if (tradeAmountInput) tradeAmountInput.required = false;
                }
            }

            fullRadio.addEventListener('change', toggleFields);
            customRadio.addEventListener('change', toggleFields);
            if (tradeRadio) {
                tradeRadio.addEventListener('change', toggleFields);
            }

            toggleFields();
        });
    </script>
</paw:layout>
