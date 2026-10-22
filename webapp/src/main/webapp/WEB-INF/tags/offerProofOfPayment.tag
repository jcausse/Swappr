<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="offer" required="true" type="ar.edu.itba.paw.model.Offer" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:url value="/my-offers/${offer.id}/payment/download" var="downloadUrl"/>

<div class="flex flex-col gap-2">
    <spring:message code="offer.proofOfPayment.label" var="proofOfPaymentLabel"/>
    <div class="text-xs font-medium text-black/60"><c:out value="${proofOfPaymentLabel}"/></div>
    <paw:linkButton variant="outline" href="${downloadUrl}" icon="download" classname="w-full justify-start text-start px-3!">
        <div class="flex flex-col items-start ml-1">
            <span class="text-black font-medium"><c:out value="${offer.proofOfPaymentFilename}"/></span>
            <span class="text-xs text-black/60 font-normal">
                <c:out value="${offer.getProofOfPaymentExtension()}"/>
                &nbsp;|&nbsp;
                <c:out value="${offer.getProofOfPaymentSizeKb()}"/> KB
            </span>
        </div>
    </paw:linkButton>
    <c:if test="${offer.proofOfPaymentIsImage()}">
        <img src="${downloadUrl}" alt="Proof of payment" class="max-w-full h-auto rounded-lg border border-black/10" />
    </c:if>
</div>
