<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="multiple" required="false" type="java.lang.Boolean" %>
<%@ attribute name="accept" required="false" %>
<%@ attribute name="maxSizeBytes" required="false" type="java.lang.Long" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="fileUpload.clearLabel" var="clearLabel"/>
<spring:message code="fileUpload.changeLabel" var="changeLabel"/>
<spring:message code="fileUpload.removeLabel" var="removeLabel"/>
<spring:message code="fileUpload.sizeError" var="sizeErrorMsg"/>

<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>
<c:set var="inputDisabled" value="${disabled ne null ? disabled : false}"/>
<c:set var="inputMultiple" value="${multiple ne null ? multiple : false}"/>
<c:set var="inputAccept" value="${not empty accept ? accept : 'image/*,application/pdf'}"/>
<c:set var="inputMaxSizeBytes" value="${maxSizeBytes ne null ? maxSizeBytes : 5242880}"/>

<div class="flex flex-col gap-2 ${inputClass}">
    <div class="flex flex-row gap-2 items-center min-h-5.5 justify-between">
        <c:if test="${not empty label}">
            <div class="text-xs text-black/70 font-medium">
                <c:out value="${label}"/>
            </div>
        </c:if>
        <c:if test="${inputMultiple}">
            <paw:button id="${path}-clear" variant="outline" role="danger" size="sm" classname="hidden" onclick="clearAllFiles('${path}')">
                <paw:icon name="trash-2" />
                <span class="text-center px-2"><c:out value="${clearLabel}"/></span>
            </paw:button>
        </c:if>
    </div>

    <!-- File size error banner -->
    <div id="${path}-size-error" class="hidden px-3 py-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2">
        <paw:icon name="circle-alert" classname="text-red-600 text-sm" />
        <span class="flex-1 error-message"></span>
        <paw:button size="sm" variant="ghost" role="danger" icon="x" type="button" onclick="dismissSizeError('${path}')" />
    </div>

    <div id="${path}-previews" class="grid ${inputMultiple ? 'grid-cols-5' : 'grid-cols-2 max-w-66 w-full'} gap-2">
        <div id="${path}-preview-template" class="relative group aspect-square rounded-lg border border-black/10 overflow-hidden hidden">
            <div class="w-full h-full flex flex-col gap-1 items-center justify-center bg-neutral-50">
                <img class="w-full h-full object-cover hidden" />
                <div class="file-icon-container hidden flex flex-col items-center justify-center relative">
                    <paw:icon name="file" classname="text-black/40 text-4xl" />
                    <div class="rounded-full bg-white absolute left-full top-full -translate-x-2/3 -translate-y-3/4">
                        <paw:badge size="sm" classname="file-extension text-lime-600" />
                    </div>
                </div>
                <div class="file-name hidden text-black/60 text-xs"></div>
            </div>
            <div class="absolute top-1 right-1 bg-white rounded-lg">
                <paw:button size="sm" variant="outline" icon="x" type="button" role="danger" />
            </div>
            <c:if test="${not inputMultiple}">
                <div class="absolute bottom-1 left-1 right-1 bg-white rounded-lg">
                    <paw:button size="sm" variant="outline" text="${changeLabel}" type="button" classname="w-full" onclick="changeFile('${path}')" />
                </div>
            </c:if>
        </div>

        <label id="${path}-add" for="${path}-file" class="aspect-square rounded-lg border-2 border-dashed border-black/20 bg-neutral-50 flex flex-col items-center justify-center gap-2 cursor-pointer hover:border-lime-600 hover:bg-lime-50 transition-all order-999">
            <c:choose>
                <c:when test="${inputMultiple}">
                    <spring:message code="fileUpload.uploadLabelMultiple" var="uploadLabel"/>
                    <form:input path="${path}" id="${path}-file" type="file" accept="${inputAccept}" disabled="${inputDisabled}" class="hidden" onchange="handleFileUpload(this, '${path}')" multiple="true" data-max-size-bytes="${inputMaxSizeBytes}" />
                </c:when>
                <c:otherwise>
                    <spring:message code="fileUpload.uploadLabel" var="uploadLabel"/>
                    <form:input path="${path}" id="${path}-file" type="file" accept="${inputAccept}" disabled="${inputDisabled}" class="hidden" onchange="handleFileUpload(this, '${path}')" data-max-size-bytes="${inputMaxSizeBytes}"/>
                </c:otherwise>
            </c:choose>
            <paw:icon name="plus" classname="text-black/40 text-3xl" />
            <span class="text-sm text-black/60 font-medium text-center px-2"><c:out value="${uploadLabel}"/></span>
        </label>
    </div>

    <div class="flex flex-wrap gap-2">
    </div>

    <form:errors path="${path}" element="div" cssClass="text-xs text-red-600" />
</div>

<script>
    // Store original file input reference and object URLs
    let fileUploadState = new Map();
    const sizeErrorMsg = '<spring:message code="fileUpload.sizeError" javaScriptEscape="true"/>';

    function changeFile(fieldId) {
      const addBtn = document.getElementById(fieldId + '-add');
      if (addBtn) addBtn.click();
    }

    function dismissSizeError(fieldId) {
        const errorBanner = document.getElementById(fieldId + '-size-error');
        if (errorBanner) {
            errorBanner.classList.add('hidden');
        }
    }

    function handleFileUpload(fileInput, fieldId) {
        const previewsContainer = document.getElementById(fieldId + '-previews');
        const clearBtn = document.getElementById(fieldId + '-clear');
        const addBtn = document.getElementById(fieldId + '-add');
        const errorBanner = document.getElementById(fieldId + '-size-error');
        const errorMessage = errorBanner ? errorBanner.querySelector('span.error-message') : null;
        const maxSizeBytes = parseInt(fileInput.dataset.maxSizeBytes || '5242880', 10);
        const isMultiple = fileInput.multiple;

        if (!fileUploadState.has(fieldId)) {
            fileUploadState.set(fieldId, {
                objectUrls: new Map(),
                originalFiles: []
            });
        }

        const state = fileUploadState.get(fieldId);
        const files = Array.from(fileInput.files);

        if (!isMultiple) {
            // Single file mode: clear existing previews
            const previews = previewsContainer.querySelectorAll('[data-index]');
            previews.forEach((preview) => preview.remove());
            state.objectUrls.forEach(url => URL.revokeObjectURL(url));
            state.objectUrls.clear();
            state.originalFiles = [];

            addBtn.classList.add('hidden');
        }

        let hasSizeError = false;

        files.forEach((file, index) => {
            const isImage = file.type.startsWith('image/');
            if (!isImage && file.type !== 'application/pdf') return;

            // Check file size
            if (file.size > maxSizeBytes) {
                hasSizeError = true;
                return; // Ignore this file
            }

            const objectUrl = isImage ? URL.createObjectURL(file) : null;
            const fileIndex = state.originalFiles.length;
            if (objectUrl) {
                state.objectUrls.set(fileIndex, objectUrl);
            }
            state.originalFiles.push(file);

            createPreview(previewsContainer, fieldId, objectUrl, file.name, file.type, fileIndex, state);
        });

        if (hasSizeError) {
            errorMessage.textContent = sizeErrorMsg || 'File exceeds maximum size of 5MB';
            errorBanner.classList.remove('hidden');
            addBtn.classList.remove('hidden');
        } else {
          errorBanner.classList.add('hidden');
        }

        if (isMultiple && state.originalFiles.length > 0) {
            clearBtn.classList.remove('hidden');
        }

        updateFileInput(fileInput, fieldId, state);
    }

    function createPreview(container, fieldId, objectUrl, fileName, fileType, index, state) {
        const template = document.getElementById(fieldId + '-preview-template');
        const preview = template.cloneNode(true);
        preview.id = fieldId + '-preview-' + index;
        preview.dataset.index = index;
        preview.classList.remove('hidden');

        const contentDiv = preview.querySelector('.w-full.h-full');
        const img = preview.querySelector('img');
        const iconContainer = preview.querySelector('.file-icon-container');
        const filenameElement = preview.querySelector('.file-name');
        const iconElement = iconContainer ? iconContainer.querySelector('paw-icon') : null;
        const extensionElement = iconContainer ? iconContainer.querySelector('.file-extension') : null;

        const isImage = objectUrl !== null;

        if (isImage) {
            img.src = objectUrl;
            img.alt = fileName;
            img.classList.remove('hidden');
            if (iconContainer) iconContainer.classList.add('hidden');
        } else {
            img.classList.add('hidden');
            if (iconContainer) {
                iconContainer.classList.remove('hidden');
                if (iconElement) {
                    iconElement.setAttribute('name', 'file');
                }
                if (extensionElement) {
                    const ext = fileName.split('.').pop().toUpperCase();
                    extensionElement.textContent = ext ?? '';
                }
            }
            if (filenameElement) {
              filenameElement.classList.remove('hidden');
              filenameElement.textContent = fileName;
            }
        }

        const removeBtn = preview.querySelector('button');
        removeBtn.addEventListener('click', function(e) {
            e.preventDefault();
            removePreview(fieldId, index, state);
        });

        container.appendChild(preview);
    }

    function removePreview(fieldId, index, state) {
        const previewsContainer = document.getElementById(fieldId + '-previews');
        const preview = previewsContainer.querySelector('[data-index="' + index + '"]');
        if (preview) {
            preview.remove();
        }

        const objectUrl = state.objectUrls.get(index);
        if (objectUrl) {
            URL.revokeObjectURL(objectUrl);
            state.objectUrls.delete(index);
        }

        state.originalFiles[index] = null;

        const fileInput = document.getElementById(fieldId + '-file');
        updateFileInput(fileInput, fieldId, state);

        const clearBtn = document.getElementById(fieldId + '-clear');
        const addBtn = document.getElementById(fieldId + '-add');
        if (state.originalFiles.filter(f => f !== null).length === 0) {
            if (clearBtn) clearBtn.classList.add('hidden');
            if (addBtn) addBtn.classList.remove('hidden');
        }
    }

    function clearAllFiles(fieldId) {
        const previewsContainer = document.getElementById(fieldId + '-previews');
        const state = fileUploadState.get(fieldId);

        if (state) {
            state.objectUrls.forEach(url => URL.revokeObjectURL(url));
            state.objectUrls.clear();
            state.originalFiles = [];
        }

        const previews = previewsContainer.querySelectorAll('[data-index]');
        previews.forEach((preview) => preview.remove());

        const fileInput = document.getElementById(fieldId + '-file');
        fileInput.value = '';

        const clearBtn = document.getElementById(fieldId + '-clear');
        const addBtn = document.getElementById(fieldId + '-add');
        if (clearBtn) clearBtn.classList.add('hidden');
        if (addBtn) addBtn.classList.remove('hidden');
    }

    function updateFileInput(fileInput, fieldId, state) {
        const dataTransfer = new DataTransfer();
        state.originalFiles.forEach(file => {
            if (file) {
                dataTransfer.items.add(file);
            }
        });
        fileInput.files = dataTransfer.files;
    }

    // Cleanup on form submit
    document.addEventListener('DOMContentLoaded', function() {
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', function() {
                fileUploadState.forEach(state => {
                    state.objectUrls.forEach(url => URL.revokeObjectURL(url));
                });
            });
        });
    });
</script>
