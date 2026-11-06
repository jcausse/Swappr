<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<spring:message code="admin.catalog.title" var="catalogTitle"/>
<spring:message code="admin.catalog.subtitle" var="catalogSubtitle"/>
<spring:message code="admin.catalog.brands.title" var="brandsTitle"/>
<spring:message code="admin.catalog.brands.subtitle" var="brandsSubtitle"/>
<spring:message code="admin.catalog.brands.id" var="brandIdLabel"/>
<spring:message code="admin.catalog.brands.name" var="brandNameLabel"/>
<spring:message code="admin.catalog.brands.modelCount" var="brandModelCountLabel"/>
<spring:message code="admin.catalog.brands.actions" var="brandActionsLabel"/>
<spring:message code="admin.catalog.brands.edit" var="editBrandLabel"/>
<spring:message code="admin.catalog.brands.delete" var="deleteBrandLabel"/>
<spring:message code="admin.catalog.brands.replace" var="replaceBrandLabel"/>
<spring:message code="admin.catalog.brands.replace.title" var="replaceBrandTitle"/>
<spring:message code="admin.catalog.brands.replace.label" var="replaceBrandSelectLabel"/>
<spring:message code="admin.catalog.brands.replace.placeholder" var="replaceBrandPlaceholder"/>
<spring:message code="admin.catalog.brands.replace.warning" var="replaceBrandWarning"/>
<spring:message code="admin.catalog.brands.replace.confirm" var="replaceBrandConfirm"/>
<spring:message code="admin.catalog.brands.replace.cancel" var="replaceBrandCancel"/>
<spring:message code="admin.catalog.brands.rename.title" var="renameBrandTitle"/>
<spring:message code="admin.catalog.brands.rename.confirm" var="renameBrandConfirm"/>
<spring:message code="admin.catalog.brands.rename.cancel" var="renameBrandCancel"/>
<spring:message code="admin.catalog.brands.delete.title" var="deleteBrandTitle"/>
<spring:message code="admin.catalog.brands.delete.message" var="deleteBrandMessage"/>
<spring:message code="admin.catalog.brands.delete.confirm" var="deleteBrandConfirm"/>
<spring:message code="admin.catalog.brands.delete.cancel" var="deleteBrandCancel"/>
<spring:message code="admin.catalog.brands.delete.blocked" var="deleteBrandBlocked"/>
<spring:message code="admin.catalog.brands.create.title" var="createBrandTitle"/>
<spring:message code="admin.catalog.brands.create.placeholder" var="createBrandPlaceholder"/>
<spring:message code="admin.catalog.brands.create.submit" var="createBrandSubmit"/>

<admin:layout titleKey="admin.catalog.title" title="${catalogTitle}" subtitle="${catalogSubtitle}">
    <div class="flex flex-col gap-6">
        <paw:card title="${brandsTitle}" subtitle="${brandsSubtitle}">
            <c:set var="idSortIcon" value="${currentSort.key eq 'id' ? (currentSort.ascending ? 'chevron-up' : 'chevron-down') : 'chevrons-up-down'}"/>
            <c:set var="idNextSort" value="${currentSort.key eq 'id' and currentSort.ascending ? 'ID_DESC' : 'ID_ASC'}"/>
            <c:set var="brandSortIcon" value="${currentSort.key eq 'brand' ? (currentSort.ascending ? 'chevron-up' : 'chevron-down') : 'chevrons-up-down'}"/>
            <c:set var="brandNextSort" value="${currentSort.key eq 'brand' and currentSort.ascending ? 'BRAND_DESC' : 'BRAND_ASC'}"/>

            <div class="mt-4 overflow-x-auto">
                <table class="w-full text-sm">
                    <thead>
                        <tr class="border-b border-black/10 text-left text-xs uppercase tracking-wide text-black/50">
                            <th class="py-2 pr-4 font-medium">
                                <c:url value="/admin/catalog" var="idSortUrl">
                                    <c:param name="sort" value="${idNextSort}"/>
                                </c:url>
                                <a href="${idSortUrl}" class="inline-flex items-center gap-1 hover:text-black transition">
                                    <c:out value="${brandIdLabel}"/>
                                    <paw:icon name="${idSortIcon}"/>
                                </a>
                            </th>
                            <th class="py-2 pr-4 font-medium">
                                <c:url value="/admin/catalog" var="brandSortUrl">
                                    <c:param name="sort" value="${brandNextSort}"/>
                                </c:url>
                                <a href="${brandSortUrl}" class="inline-flex items-center gap-1 hover:text-black transition">
                                    <c:out value="${brandNameLabel}"/>
                                    <paw:icon name="${brandSortIcon}"/>
                                </a>
                            </th>
                            <th class="py-2 pr-4 font-medium"><c:out value="${brandModelCountLabel}"/></th>
                            <th class="py-2 text-right font-medium"><c:out value="${brandActionsLabel}"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="brand" items="${brands}">
                            <tr class="border-b border-black/5 last:border-0 align-top">
                                <td class="py-3 pr-4">
                                    <span class="text-xs font-mono font-medium px-1.5 py-0.5 rounded bg-black/5 text-black/70">
                                        #<c:out value="${brand.id}"/>
                                    </span>
                                </td>
                                <td class="py-3 pr-4 font-medium">
                                    <c:out value="${brand.name}"/>
                                </td>
                                <td class="py-3 pr-4 text-black/70">
                                    <c:out value="${brand.modelCount}"/>
                                </td>
                                <td class="py-3">
                                    <div class="flex flex-col items-end gap-1">
                                        <div class="flex items-center justify-end gap-2">
                                            <paw:button
                                                text="${editBrandLabel}"
                                                type="button"
                                                variant="outline"
                                                size="sm"
                                                icon="pencil"
                                                onclick="document.getElementById('renameBrandDialog-${brand.id}').showModal()"
                                            />

                                            <c:if test="${fn:length(brandOptions) > 1}">
                                                <paw:button
                                                    text="${replaceBrandLabel}"
                                                    type="button"
                                                    variant="outline"
                                                    role="secondary"
                                                    size="sm"
                                                    icon="replace"
                                                    onclick="document.getElementById('replaceBrandDialog-${brand.id}').showModal()"
                                                />
                                            </c:if>

                                            <c:choose>
                                                <c:when test="${brand.hasListings}">
                                                    <paw:button
                                                        text="${deleteBrandLabel}"
                                                        type="button"
                                                        variant="outline"
                                                        role="danger"
                                                        size="sm"
                                                        icon="trash-2"
                                                        disabled="true"
                                                    />
                                                </c:when>
                                                <c:otherwise>
                                                    <paw:button
                                                        text="${deleteBrandLabel}"
                                                        type="button"
                                                        variant="outline"
                                                        role="danger"
                                                        size="sm"
                                                        icon="trash-2"
                                                        onclick="document.getElementById('deleteBrandDialog-${brand.id}').showModal()"
                                                    />
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <c:if test="${brand.hasListings}">
                                            <p class="text-xs text-black/50 text-right">
                                                <c:out value="${deleteBrandBlocked}"/>
                                            </p>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <paw:pagination page="${brandsPage}" baseUrl="/admin/catalog"/>

            <paw:divider />

            <div class="mt-4 flex flex-col gap-2">
                <h3 class="text-sm font-semibold"><c:out value="${createBrandTitle}"/></h3>
                <c:url value="/admin/catalog/brands" var="createBrandUrl"/>
                <form action="${createBrandUrl}" method="post" class="flex flex-col sm:flex-row gap-3 items-end">
                    <div class="flex-1 w-full">
                        <paw:input
                            id="newBrandName"
                            name="name"
                            label="${brandNameLabel}"
                            placeholder="${createBrandPlaceholder}"
                            variant="outline"
                        />
                    </div>
                    <paw:button
                        text="${createBrandSubmit}"
                        type="submit"
                        variant="default"
                        role="default"
                        icon="plus"
                        classname="w-full sm:w-auto"
                    />
                </form>
            </div>
        </paw:card>

        <c:forEach var="brand" items="${brands}">
            <c:url value="/admin/catalog/brands/${brand.id}/rename" var="renameBrandUrl"/>
            <c:url value="/admin/catalog/brands/${brand.id}/delete" var="deleteBrandUrl"/>
            <c:url value="/admin/catalog/brands/${brand.id}/replace" var="replaceBrandUrl"/>

            <paw:formDialog
                id="renameBrandDialog-${brand.id}"
                title="${renameBrandTitle}"
                confirmText="${renameBrandConfirm}"
                cancelText="${renameBrandCancel}"
                formAction="${renameBrandUrl}"
            >
                <paw:input
                    id="brandName-${brand.id}"
                    name="name"
                    value="${brand.name}"
                    label="${brandNameLabel}"
                    variant="outline"
                />
            </paw:formDialog>

            <paw:formDialog
                id="replaceBrandDialog-${brand.id}"
                title="${replaceBrandTitle}"
                confirmText="${replaceBrandConfirm}"
                cancelText="${replaceBrandCancel}"
                formAction="${replaceBrandUrl}"
            >
                <div class="flex flex-col gap-3">
                    <label for="replacementBrandId-${brand.id}" class="text-xs text-black/70 font-medium">
                        <c:out value="${replaceBrandSelectLabel}"/>
                    </label>
                    <select
                        id="replacementBrandId-${brand.id}"
                        name="replacementBrandId"
                        class="px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2 bg-white"
                    >
                        <option value=""><c:out value="${replaceBrandPlaceholder}"/></option>
                        <c:forEach var="candidate" items="${brandOptions}">
                            <c:if test="${candidate.id ne brand.id}">
                                <option value="${candidate.id}"><c:out value="${candidate.name}"/></option>
                            </c:if>
                        </c:forEach>
                    </select>
                    <p class="text-xs text-red-600"><c:out value="${replaceBrandWarning}"/></p>
                </div>
            </paw:formDialog>

            <paw:confirmDialog
                id="deleteBrandDialog-${brand.id}"
                title="${deleteBrandTitle}"
                confirmText="${deleteBrandConfirm}"
                cancelText="${deleteBrandCancel}"
                formAction="${deleteBrandUrl}"
            >
                <c:out value="${deleteBrandMessage}"/>
            </paw:confirmDialog>
        </c:forEach>
    </div>
</admin:layout>
