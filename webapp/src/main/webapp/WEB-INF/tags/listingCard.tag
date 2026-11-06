<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="listing" required="true" type="ar.edu.itba.paw.model.Listing" %>
<%@ attribute name="listingUrl" required="true" %>
<%@ attribute name="noImageLabel" required="false" %>
<%@ attribute name="cardClassname" required="false" %>
<%@ attribute name="linkClassname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:set var="listingCardVariant" value="${not empty variant ? variant : 'default'}"/>
<c:set var="imageUrl" value=""/>
<c:if test="${not empty listing.imageIds}">
    <c:url value="/image/${listing.imageIds[0]}" var="imageUrl"/>
</c:if>
<c:set var="subLabel" value=""/>
<c:if test="${not empty listing.model and not empty listing.model.subcategory}">
    <spring:message code="subcategory.${listing.model.subcategory.name}" var="subLabel"/>
</c:if>

<c:set var="variantClassnames" value="${
  listingCardVariant eq 'compact'
    ? 'p-2!'
    : ''
}"/>

<a href="${listingUrl}" class="block hover:-translate-y-0.5 transition ${linkClassname}">
    <paw:card classname="h-full relative ${variantClassnames} ${cardClassname}">
        <div class="w-full aspect-square rounded-lg overflow-hidden border border-black/10 mb-1 bg-neutral-200 relative">
          <c:choose>
            <c:when test="${not empty imageUrl}">
              <img src="${imageUrl}" alt="<c:out value='${listing.title}'/>" class="w-full h-full object-cover"/>
            </c:when>
            <c:otherwise>
              <div class="w-full h-full grid place-items-center text-black/30 text-xs px-2 text-center">
                <c:out value="${noImageLabel}"/>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
        <c:choose>
            <c:when test="${listingCardVariant == 'compact'}">
                <paw:listingHotBadge listing="${listing}" classname="top-1! left-1!" />
                <h3 class="text-sm font-medium"><c:out value="${listing.title}"/></h3>
                <div class="flex items-center gap-2 flex-wrap mt-auto">
                    <p class="text-lg font-bold"><paw:price amount="${listing.price.amount}"/></p>
                    <c:if test="${listing.acceptsTrade}">
                        <span class="text-sm text-lime-600 bg-lime-50 border border-lime-200 rounded-full w-5 h-5 flex items-center justify-center">
                            <paw:icon name="arrow-right-left" />
                        </span>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <paw:listingHotBadge listing="${listing}" />
                <h3 class="text-base font-semibold"><c:out value="${listing.title}"/></h3>
                <p class="text-sm text-black/60 mb-2"><c:out value="${subLabel}"/></p>
                <div class="flex items-center gap-2 flex-wrap mt-auto">
                    <p class="text-xl font-bold"><paw:price amount="${listing.price.amount}"/></p>
                    <c:if test="${listing.acceptsTrade}">
                        <span class="text-sm text-lime-600 bg-lime-50 border border-lime-200 rounded-full w-5 h-5 flex items-center justify-center">
                            <paw:icon name="arrow-right-left" />
                        </span>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </paw:card>
</a>
