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
                        <spring:message code="currency.symbol" var="currencySymbol"/>
                        <div class="flex flex-col gap-3">
                            <div class="flex flex-col gap-2">
                                <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                    <form:radiobutton path="offerType" value="FULL" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                    <spring:message code="checkout.form.fullPrice" var="fullPriceLabel"/>
                                    <span class="text-sm text-black/90"><c:out value="${fullPriceLabel}"/> - <paw:price amount="${listing.price.amount}"/></span>
                                </label>

                                <div class="flex flex-row gap-2 items-center justify-between">
                                    <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                        <form:radiobutton path="offerType" value="CUSTOM" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                        <spring:message code="checkout.form.customPrice" var="customPriceLabel"/>
                                        <span class="text-sm text-black/90"><c:out value="${customPriceLabel}"/></span>
                                    </label>

                                    <spring:message code="checkout.form.customAmount.placeholder" var="customAmountPlaceholder"/>
                                    <div id="customAmountField" class="hidden w-1/2">
                                        <div class="flex items-center gap-2">
                                            <span class="text-sm text-black/70"><c:out value="${currencySymbol}"/></span>
                                            <paw:formInput path="customAmount" type="number" step="0.01" placeholder="${customAmountPlaceholder}" variant="outline" classname="flex-1"/>
                                        </div>
                                    </div>
                                </div>

                                <c:if test="${listing.acceptsTrade and not empty userListings}">
                                    <div class="flex flex-col gap-2">
                                        <label class="flex items-center gap-2 min-h-8 cursor-pointer">
                                            <form:radiobutton path="offerType" value="TRADE" class="w-4 h-4 text-lime-600 border-black/20 focus:ring-lime-500"/>
                                            <spring:message code="checkout.form.tradePrice" var="tradePriceLabel"/>
                                            <span class="text-sm text-black/90"><c:out value="${tradePriceLabel}"/></span>
                                        </label>

                                        <div id="tradeFields" class="hidden flex flex-col gap-3">
                                            <spring:message code="checkout.form.tradeSelectListing" var="tradeSelectListingLabel"/>
                                            <p class="text-xs text-black/70 font-medium"><c:out value="${tradeSelectListingLabel}"/></p>

                                            <form:hidden path="offeredListingId" id="offeredListingId"/>
                                            <form:hidden path="page" id="page"/>
                                            <form:errors path="offeredListingId" element="div" cssClass="text-xs text-red-600"/>

                                            <div id="tradeListingList" class="flex flex-col gap-2">
                                                <c:forEach var="userListing" items="${userListings}">
                                                    <button type="button"
                                                            class="trade-listing-option group flex w-full items-center gap-3 rounded-xl border p-2 text-left transition hover:bg-lime-50 focus-visible:border-lime-600 focus-visible:outline-2 cursor-pointer ${checkoutForm.offeredListingId == userListing.id ? 'border-lime-600 bg-lime-50' : 'border-black/15'}"
                                                            data-listing-id="${userListing.id}"
                                                            aria-pressed="${checkoutForm.offeredListingId == userListing.id}">
                                                        <paw:listingImage listing="${userListing}" size="sm"/>
                                                        <span class="flex-1 min-w-0">
                                                            <span class="block truncate text-sm font-medium text-black"><c:out value="${userListing.title}"/></span>
                                                            <span class="block truncate text-xs text-black/60">
                                                                <c:out value="${userListing.product.brand}"/>
                                                                <c:out value="${userListing.product.model}"/>
                                                                (<c:out value="${userListing.product.year}"/>)
                                                            </span>
                                                        </span>
                                                        <span class="trade-listing-check flex-shrink-0 text-lime-600 ${checkoutForm.offeredListingId == userListing.id ? '' : 'hidden'}">
                                                            <paw:icon name="check"/>
                                                        </span>
                                                    </button>
                                                </c:forEach>
                                            </div>

                                            <div id="tradeListingPagination" class="-mt-8 mb-4">
                                                <c:url value="/checkout" var="checkoutBaseUrl"/>
                                                <paw:pagination page="${userListingsPage}" baseUrl="${checkoutBaseUrl}"/>
                                            </div>

                                            <spring:message code="checkout.form.tradeAmount" var="tradeAmountLabel"/>
                                            <spring:message code="checkout.form.tradeAmount.placeholder" var="tradeAmountPlaceholder"/>
                                            <div class="flex flex-col gap-1 w-full max-w-md">
                                                <label for="tradeAmount" class="text-xs text-black/70 font-medium"><c:out value="${tradeAmountLabel}"/></label>
                                                <div class="flex items-start gap-2">
                                                    <span class="py-1 border border-transparent text-sm text-black/70"><c:out value="${currencySymbol}"/></span>
                                                    <paw:formInput path="tradeAmount" type="number" step="0.01" placeholder="${tradeAmountPlaceholder}" variant="outline" classname="flex-1"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <spring:message code="checkout.form.message" var="messageLabel"/>
                        <spring:message code="checkout.form.message.placeholder" var="messagePlaceholder"/>
                        <paw:formInput path="message" type="textarea" label="${messageLabel}" placeholder="${messagePlaceholder}" variant="outline" inputClassname="min-h-40 resize-none"/>

                        <spring:message code="checkout.form.hint" var="checkoutHintMsg"/>
                        <paw:banner text="${checkoutHintMsg}" icon="info" role="secondary"/>
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
                        <p class="text-3xl font-bold"><paw:price amount="${listing.price.amount}"/></p>
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
            const fullRadio = document.querySelector('input[name="offerType"][value="FULL"]');
            const customRadio = document.querySelector('input[name="offerType"][value="CUSTOM"]');
            const tradeRadio = document.querySelector('input[name="offerType"][value="TRADE"]');
            const customField = document.getElementById('customAmountField');
            const tradeFields = document.getElementById('tradeFields');
            const offeredListingInput = document.getElementById('offeredListingId');
            const pageInput = document.getElementById('page');
            const tradeListingList = document.getElementById('tradeListingList');
            const tradeListingPagination = document.getElementById('tradeListingPagination');

            function toggleFields() {
                if (customField) {
                    customField.classList.toggle('hidden', !customRadio.checked);
                }
                if (tradeFields) {
                    const isTrade = tradeRadio && tradeRadio.checked;
                    tradeFields.classList.toggle('hidden', !isTrade);
                }
            }

            function applyListingSelection() {
                if (!tradeListingList || !offeredListingInput) return;
                const selectedId = offeredListingInput.value;
                tradeListingList.querySelectorAll('.trade-listing-option').forEach(function(option) {
                    const isSelected = selectedId !== '' && option.dataset.listingId === selectedId;
                    option.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
                    option.classList.toggle('border-lime-600', isSelected);
                    option.classList.toggle('bg-lime-50', isSelected);
                    option.classList.toggle('border-black/15', !isSelected);
                    const check = option.querySelector('.trade-listing-check');
                    if (check) check.classList.toggle('hidden', !isSelected);
                });
            }

            if (fullRadio) fullRadio.addEventListener('change', toggleFields);
            if (customRadio) customRadio.addEventListener('change', toggleFields);
            if (tradeRadio) tradeRadio.addEventListener('change', toggleFields);

            if (tradeListingList && offeredListingInput) {
                tradeListingList.addEventListener('click', function(event) {
                    const option = event.target.closest('.trade-listing-option');
                    if (!option) return;
                    offeredListingInput.value = option.dataset.listingId;
                    applyListingSelection();
                });
            }

            if (tradeListingPagination && tradeFields) {
                tradeListingPagination.addEventListener('click', function(event) {
                    const link = event.target.closest('a[href]');
                    if (!link || link.getAttribute('aria-disabled') === 'true') return;
                    event.preventDefault();

                    const url = new URL(link.href, window.location.origin);
                    const formFields = tradeFields.closest('form').querySelectorAll('input[name], select[name], textarea[name]');

                    formFields.forEach(function(field) {
                        if (field.name === 'page' || field.name === 'listingId') return;
                        url.searchParams.delete(field.name);

                        if ((field.type === 'radio' || field.type === 'checkbox') && !field.checked) return;
                        if (field.value === '') return;
                        url.searchParams.set(field.name, field.value);
                    });

                    const page = url.searchParams.get('page');
                    if (pageInput && page) pageInput.value = page;
                    window.location.href = url.toString();
                });
            }

            applyListingSelection();
            toggleFields();
        });
    </script>
</paw:layout>
