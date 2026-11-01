<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="offer" required="true" type="ar.edu.itba.paw.model.Offer" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:url value="/my-offers/${offer.id}/shipping/download" var="downloadUrl"/>

<div class="flex flex-col gap-2">
    <c:if test="${not empty offer.trackingNumber}">
        <spring:message code="offer.proofOfShipping.trackingNumberLabel" var="trackingLabel"/>
        <div class="flex-1 min-w-0">
            <p class="text-xs font-medium text-black/60"><c:out value="${trackingLabel}"/></p>
            <div class="flex items-center gap-2 mt-2">
                <div class="text-base font-mono font-semibold p-1 pl-3 border border-black/10 rounded-lg self-start min-w-0 select-all flex-1 flex flex-row gap-2 items-center">
                    <span id="trackingNumberText"><c:out value="${offer.trackingNumber}"/></span>
                    <paw:button variant="ghost" icon="copy" classname="flex-shrink-0 ml-auto"
                        onclick="navigator.clipboard.writeText(document.querySelector('#trackingNumberText').textContent); this.querySelector('i').className='icon-check'; setTimeout(() => this.querySelector('i').className='icon-copy', 2000);"
                    />
                </div>
            </div>
        </div>
    </c:if>
    <c:if test="${offer.proofOfShippingId != null}">
        <spring:message code="offer.proofOfShipping.label" var="shippingLabel"/>
        <div class="text-xs font-medium text-black/60"><c:out value="${shippingLabel}"/></div>
        <paw:linkButton variant="outline" href="${downloadUrl}" icon="download" classname="w-full justify-start text-start px-3!">
            <div class="flex flex-col items-start ml-1">
                <span class="text-black font-medium"><c:out value="${offer.proofOfShippingFilename}"/></span>
                <span class="text-xs text-black/60 font-normal">
                    <c:out value="${offer.getProofOfShippingExtension()}"/>
                    &nbsp;|&nbsp;
                    <c:out value="${offer.getProofOfShippingSizeKb()}"/> KB
                </span>
            </div>
        </paw:linkButton>
        <c:if test="${offer.proofOfShippingIsImage()}">
            <img src="${downloadUrl}" alt="Proof of shipping" class="max-w-full h-auto rounded-lg border border-black/10" />
        </c:if>
    </c:if>
</div>
