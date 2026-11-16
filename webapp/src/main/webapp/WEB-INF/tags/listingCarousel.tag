<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="listings" required="true" type="java.util.List" %>
<%@ attribute name="seeMoreUrl" required="true" %>
<%@ attribute name="seeMoreLabel" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<spring:message code="carousel.previous" var="prevLabel"/>
<spring:message code="carousel.next" var="nextLabel"/>
<spring:message code="card.noImage" var="noImageLabel"/>

<div class="w-full">
    <div class="flex items-center justify-between">
        <div>
            <h2 class="text-xl font-bold"><c:out value="${title}"/></h2>
            <p class="text-sm text-black/60"><c:out value="${subtitle}"/></p>
        </div>
        <paw:linkButton href="${seeMoreUrl}" variant="ghost" size="sm" role="secondary" classname="self-end">
            <c:out value="${seeMoreLabel}"/>
            <paw:icon name="chevron-right" classname="w-4 h-4"/>
        </paw:linkButton>
    </div>

    <div class="relative group -mx-6">
        <div class="absolute left-0 top-0 bottom-0 w-6 bg-gradient-to-r from-neutral-50 to-transparent z-5"></div>
        <paw:button
            type="button"
            onclick="document.getElementById('${id}').scrollBy({left: -280, behavior: 'smooth'})"
            variant="outline"
            icon="chevron-left"
            classname="absolute left-8 top-1/2 -translate-y-1/2 -translate-x-1/2 z-10 opacity-0 group-hover:opacity-100 transition bg-white hover:bg-lime-50 active:bg-lime-100"
            ariaLabel="${prevLabel}"
        />

        <div
            id="<c:out value='${id}'/>"
            class="flex gap-2 overflow-x-auto overflow-y-visible scroll-smooth snap-x snap-start snap-mandatory [scrollbar-width:none] py-4 px-6 scroll-px-6"
        >
            <c:forEach var="listing" items="${listings}" varStatus="status">
                <c:url value="/listing/${listing.id}" var="listingUrl"/>
                <paw:listingCard
                    listing="${listing}"
                    listingUrl="${listingUrl}"
                    noImageLabel="${noImageLabel}"
                    linkClassname="snap-start w-56 shrink-0"
                    variant="compact"
                />
            </c:forEach>
        </div>

        <div class="absolute right-0 top-0 bottom-0 w-6 bg-gradient-to-l from-neutral-50 to-transparent z-5"></div>
        <paw:button
            type="button"
            onclick="document.getElementById('${id}').scrollBy({left: 280, behavior: 'smooth'})"
            variant="outline"
            icon="chevron-right"
            classname="absolute right-8 top-1/2 -translate-y-1/2 translate-x-1/2 z-10 opacity-0 group-hover:opacity-100 transition bg-white hover:bg-lime-50 active:bg-lime-100"
            ariaLabel="${nextLabel}"
        />
    </div>
</div>
