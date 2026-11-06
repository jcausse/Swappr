<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
<spring:message code="admin.catalog.models.title" var="modelsTitle"/>
<spring:message code="admin.catalog.models.subtitle" var="modelsSubtitle"/>
<spring:message code="admin.catalog.models.id" var="modelIdLabel"/>
<spring:message code="admin.catalog.models.name" var="modelNameLabel"/>
<spring:message code="admin.catalog.models.brand" var="modelBrandLabel"/>
<spring:message code="admin.catalog.models.subcategory" var="modelSubcategoryLabel"/>
<spring:message code="admin.catalog.models.actions" var="modelActionsLabel"/>
<spring:message code="admin.catalog.models.edit" var="editModelLabel"/>
<spring:message code="admin.catalog.models.replace" var="replaceModelLabel"/>
<spring:message code="admin.catalog.models.delete" var="deleteModelLabel"/>
<spring:message code="admin.catalog.models.filter.brand" var="modelFilterBrandLabel"/>
<spring:message code="admin.catalog.models.filter.subcategory" var="modelFilterSubcategoryLabel"/>
<spring:message code="admin.catalog.models.filter.all" var="modelFilterAllLabel"/>
<spring:message code="admin.catalog.models.filter.apply" var="modelFilterApplyLabel"/>
<spring:message code="admin.catalog.models.filter.clear" var="modelFilterClearLabel"/>
<spring:message code="admin.catalog.models.create.title" var="createModelTitle"/>
<spring:message code="admin.catalog.models.create.nameLabel" var="createModelNameLabel"/>
<spring:message code="admin.catalog.models.create.namePlaceholder" var="createModelNamePlaceholder"/>
<spring:message code="admin.catalog.models.create.brandLabel" var="createModelBrandLabel"/>
<spring:message code="admin.catalog.models.create.brandPlaceholder" var="createModelBrandPlaceholder"/>
<spring:message code="admin.catalog.models.create.subcategoryLabel" var="createModelSubcategoryLabel"/>
<spring:message code="admin.catalog.models.create.subcategoryPlaceholder" var="createModelSubcategoryPlaceholder"/>
<spring:message code="admin.catalog.models.create.submit" var="createModelSubmit"/>
<spring:message code="admin.catalog.models.edit.title" var="editModelTitle"/>
<spring:message code="admin.catalog.models.edit.confirm" var="editModelConfirm"/>
<spring:message code="admin.catalog.models.edit.cancel" var="editModelCancel"/>
<spring:message code="admin.catalog.models.replace.title" var="replaceModelTitle"/>
<spring:message code="admin.catalog.models.replace.label" var="replaceModelSelectLabel"/>
<spring:message code="admin.catalog.models.replace.placeholder" var="replaceModelPlaceholder"/>
<spring:message code="admin.catalog.models.replace.warning" var="replaceModelWarning"/>
<spring:message code="admin.catalog.models.replace.confirm" var="replaceModelConfirm"/>
<spring:message code="admin.catalog.models.replace.cancel" var="replaceModelCancel"/>
<spring:message code="admin.catalog.models.delete.title" var="deleteModelTitle"/>
<spring:message code="admin.catalog.models.delete.message" var="deleteModelMessage"/>
<spring:message code="admin.catalog.models.delete.confirm" var="deleteModelConfirm"/>
<spring:message code="admin.catalog.models.delete.cancel" var="deleteModelCancel"/>
<spring:message code="admin.catalog.models.delete.blocked" var="deleteModelBlocked"/>


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

                                            <c:if test="${not empty brandReplacementOptions[brand.id]}">
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
                modelAttribute="replaceBrandForm"
            >
                <div class="flex flex-col gap-3">
                    <paw:formSelect
                        id="replacementBrandId-${brand.id}"
                        path="replacementBrandId"
                        label="${replaceBrandSelectLabel}"
                        placeholder="${replaceBrandPlaceholder}"
                        items="${brandReplacementOptions[brand.id]}"
                        itemLabel="name"
                        itemValue="id"
                    />
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

        <c:url value="/admin/catalog" var="catalogUrl"/>
        <paw:card title="${modelsTitle}" subtitle="${modelsSubtitle}">
            <c:set var="modelIdSortIcon" value="${currentModelSort.key eq 'id' ? (currentModelSort.ascending ? 'chevron-up' : 'chevron-down') : 'chevrons-up-down'}"/>
            <c:set var="modelIdNextSort" value="${currentModelSort.key eq 'id' and currentModelSort.ascending ? 'ID_DESC' : 'ID_ASC'}"/>
            <c:set var="modelNameSortIcon" value="${currentModelSort.key eq 'name' ? (currentModelSort.ascending ? 'chevron-up' : 'chevron-down') : 'chevrons-up-down'}"/>
            <c:set var="modelNameNextSort" value="${currentModelSort.key eq 'name' and currentModelSort.ascending ? 'NAME_DESC' : 'NAME_ASC'}"/>

            <form:form
                modelAttribute="modelFilterForm"
                action="${catalogUrl}"
                method="get"
                class="mt-2 mb-3 flex flex-col sm:flex-row gap-3 items-end"
            >
                <input type="hidden" name="page" value="${brandsPage.page}"/>
                <input type="hidden" name="sort" value="${currentSort.name()}"/>
                <input type="hidden" name="modelSort" value="${currentModelSort.name()}"/>
                <div class="flex-1 w-full">
                    <paw:formSelect
                        path="brandId"
                        label="${modelFilterBrandLabel}"
                        placeholder="${modelFilterAllLabel}"
                        items="${brandOptions}"
                        itemLabel="name"
                        itemValue="id"
                    />
                </div>
                <div class="flex-1 w-full">
                    <paw:formSelect
                        path="subcategoryId"
                        label="${modelFilterSubcategoryLabel}"
                        placeholder="${modelFilterAllLabel}"
                        items="${subcategoryOptions}"
                        itemLabel="name"
                        itemValue="id"
                    />
                </div>
                <paw:button text="${modelFilterApplyLabel}" type="submit" variant="default" role="default" icon="search" classname="w-full sm:w-auto"/>
                <c:url value="/admin/catalog" var="clearModelFiltersUrl">
                    <c:param name="page" value="${brandsPage.page}"/>
                    <c:param name="sort" value="${currentSort.name()}"/>
                    <c:param name="modelSort" value="${currentModelSort.name()}"/>
                </c:url>
                <paw:linkButton href="${clearModelFiltersUrl}" text="${modelFilterClearLabel}" variant="ghost" size="sm" role="secondary" classname="w-full sm:w-auto"/>
            </form:form>

            <div class="overflow-x-auto">
                <table class="w-full text-sm">
                    <thead>
                        <tr class="border-b border-black/10 text-left text-xs uppercase tracking-wide text-black/50">
                            <th class="py-2 pr-4 font-medium">
                                <c:url value="/admin/catalog" var="modelIdSortUrl">
                                    <c:if test="${not empty modelFilterForm.brandId}"><c:param name="brandId" value="${modelFilterForm.brandId}"/></c:if>
                                    <c:if test="${not empty modelFilterForm.subcategoryId}"><c:param name="subcategoryId" value="${modelFilterForm.subcategoryId}"/></c:if>
                                    <c:param name="page" value="${brandsPage.page}"/>
                                    <c:param name="sort" value="${currentSort.name()}"/>
                                    <c:param name="modelSort" value="${modelIdNextSort}"/>
                                </c:url>
                                <a href="${modelIdSortUrl}" class="inline-flex items-center gap-1 hover:text-black transition">
                                    <c:out value="${modelIdLabel}"/>
                                    <paw:icon name="${modelIdSortIcon}"/>
                                </a>
                            </th>
                            <th class="py-2 pr-4 font-medium">
                                <c:url value="/admin/catalog" var="modelNameSortUrl">
                                    <c:if test="${not empty modelFilterForm.brandId}"><c:param name="brandId" value="${modelFilterForm.brandId}"/></c:if>
                                    <c:if test="${not empty modelFilterForm.subcategoryId}"><c:param name="subcategoryId" value="${modelFilterForm.subcategoryId}"/></c:if>
                                    <c:param name="page" value="${brandsPage.page}"/>
                                    <c:param name="sort" value="${currentSort.name()}"/>
                                    <c:param name="modelSort" value="${modelNameNextSort}"/>
                                </c:url>
                                <a href="${modelNameSortUrl}" class="inline-flex items-center gap-1 hover:text-black transition">
                                    <c:out value="${modelNameLabel}"/>
                                    <paw:icon name="${modelNameSortIcon}"/>
                                </a>
                            </th>
                            <th class="py-2 pr-4 font-medium"><c:out value="${modelBrandLabel}"/></th>
                            <th class="py-2 pr-4 font-medium"><c:out value="${modelSubcategoryLabel}"/></th>
                            <th class="py-2 text-right font-medium"><c:out value="${modelActionsLabel}"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="model" items="${models}">
                            <tr class="border-b border-black/5 last:border-0 align-top">
                                <td class="py-3 pr-4">
                                    <span class="text-xs font-mono font-medium px-1.5 py-0.5 rounded bg-black/5 text-black/70">
                                        #<c:out value="${model.id}"/>
                                    </span>
                                </td>
                                <td class="py-3 pr-4 font-medium"><c:out value="${model.name}"/></td>
                                <td class="py-3 pr-4 text-black/70"><c:out value="${model.brandName}"/></td>
                                <td class="py-3 pr-4 text-black/70"><c:out value="${model.subcategoryName}"/></td>
                                <td class="py-3">
                                    <div class="flex flex-col items-end gap-1">
                                        <div class="flex items-center justify-end gap-2">
                                            <paw:button
                                                text="${editModelLabel}"
                                                type="button"
                                                variant="outline"
                                                size="sm"
                                                icon="pencil"
                                                onclick="document.getElementById('editModelDialog-${model.id}').showModal()"
                                            />
                                            <c:if test="${not empty modelReplacementOptions[model.id]}">
                                                <paw:button
                                                    text="${replaceModelLabel}"
                                                    type="button"
                                                    variant="outline"
                                                    role="secondary"
                                                    size="sm"
                                                    icon="replace"
                                                    onclick="document.getElementById('replaceModelDialog-${model.id}').showModal()"
                                                />
                                            </c:if>
                                            <c:choose>
                                                <c:when test="${model.hasListings}">
                                                    <paw:button
                                                        text="${deleteModelLabel}"
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
                                                        text="${deleteModelLabel}"
                                                        type="button"
                                                        variant="outline"
                                                        role="danger"
                                                        size="sm"
                                                        icon="trash-2"
                                                        onclick="document.getElementById('deleteModelDialog-${model.id}').showModal()"
                                                    />
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <c:if test="${model.hasListings}">
                                            <p class="text-xs text-black/50 text-right">
                                                <c:out value="${deleteModelBlocked}"/>
                                            </p>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <paw:pagination page="${modelsPage}" baseUrl="/admin/catalog" pageParam="modelPage"/>

            <paw:divider />

            <div class="mt-4 flex flex-col gap-2">
                <h3 class="text-sm font-semibold"><c:out value="${createModelTitle}"/></h3>
                <c:url value="/admin/catalog/models" var="createModelUrl"/>
                <form:form
                    modelAttribute="createModelForm"
                    action="${createModelUrl}"
                    method="post"
                    class="flex flex-col gap-3"
                >
                    <div class="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
                        <paw:formInput
                            path="name"
                            label="${createModelNameLabel}"
                            placeholder="${createModelNamePlaceholder}"
                            variant="outline"
                        />
                        <paw:formSelect
                            path="brandId"
                            label="${createModelBrandLabel}"
                            placeholder="${createModelBrandPlaceholder}"
                            items="${brandOptions}"
                            itemLabel="name"
                            itemValue="id"
                        />
                        <paw:formSelect
                            path="subcategoryId"
                            label="${createModelSubcategoryLabel}"
                            placeholder="${createModelSubcategoryPlaceholder}"
                            items="${subcategoryOptions}"
                            itemLabel="name"
                            itemValue="id"
                        />
                        <paw:button
                            text="${createModelSubmit}"
                            type="submit"
                            variant="default"
                            role="default"
                            icon="plus"
                            classname="w-full"
                        />
                    </div>
                </form:form>
            </div>
        </paw:card>

        <c:forEach var="model" items="${models}">
            <c:url value="/admin/catalog/models/${model.id}/edit" var="editModelUrl"/>
            <c:url value="/admin/catalog/models/${model.id}/replace" var="replaceModelUrl"/>
            <c:url value="/admin/catalog/models/${model.id}/delete" var="deleteModelUrl"/>

            <paw:formDialog
                id="editModelDialog-${model.id}"
                title="${editModelTitle}"
                confirmText="${editModelConfirm}"
                cancelText="${editModelCancel}"
                formAction="${editModelUrl}"
            >
                <div class="flex flex-col gap-3">
                    <paw:input
                        id="modelName-${model.id}"
                        name="name"
                        value="${model.name}"
                        label="${modelNameLabel}"
                        variant="outline"
                    />
                    <div class="flex flex-col gap-1">
                        <label for="modelBrand-${model.id}" class="text-xs text-black/70 font-medium">
                            <c:out value="${modelBrandLabel}"/>
                        </label>
                        <select
                            id="modelBrand-${model.id}"
                            name="brandId"
                            class="px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2 bg-white"
                        >
                            <c:forEach var="brand" items="${brandOptions}">
                                <option value="${brand.id}" ${brand.id eq model.brandId ? 'selected' : ''}>
                                    <c:out value="${brand.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="flex flex-col gap-1">
                        <label for="modelSubcategory-${model.id}" class="text-xs text-black/70 font-medium">
                            <c:out value="${modelSubcategoryLabel}"/>
                        </label>
                        <select
                            id="modelSubcategory-${model.id}"
                            name="subcategoryId"
                            class="px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2 bg-white"
                        >
                            <c:forEach var="option" items="${subcategoryOptions}">
                                <option value="${option.id}" ${option.id eq model.subcategoryId ? 'selected' : ''}>
                                    <c:out value="${option.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </paw:formDialog>

            <paw:formDialog
                id="replaceModelDialog-${model.id}"
                title="${replaceModelTitle}"
                confirmText="${replaceModelConfirm}"
                cancelText="${replaceModelCancel}"
                formAction="${replaceModelUrl}"
                modelAttribute="replaceModelForm"
            >
                <div class="flex flex-col gap-3">
                    <paw:formSelect
                        id="replacementModelId-${model.id}"
                        path="replacementModelId"
                        label="${replaceModelSelectLabel}"
                        placeholder="${replaceModelPlaceholder}"
                        items="${modelReplacementOptions[model.id]}"
                        itemLabel="name"
                        itemValue="id"
                    />
                    <p class="text-xs text-red-600"><c:out value="${replaceModelWarning}"/></p>
                </div>
            </paw:formDialog>

            <paw:confirmDialog
                id="deleteModelDialog-${model.id}"
                title="${deleteModelTitle}"
                confirmText="${deleteModelConfirm}"
                cancelText="${deleteModelCancel}"
                formAction="${deleteModelUrl}"
            >
                <c:out value="${deleteModelMessage}"/>
            </paw:confirmDialog>
        </c:forEach>

    </div>
</admin:layout>
