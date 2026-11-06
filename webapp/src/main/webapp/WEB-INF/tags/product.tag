<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model" required="true" type="ar.edu.itba.paw.model.Model" %>
<%@ attribute name="year" required="true" type="java.lang.Integer" %>
<%@ attribute name="size" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<c:set var="fontSize" value="${not empty size ? size : 'md'}"/>
<c:set var="sizeClassnamesName" value="${fontSize eq 'sm' ? 'text-sm' : 'text-base'}"/>
<c:set var="sizeClassnamesCategory" value="${fontSize eq 'sm' ? 'text-xs' : 'text-sm'}"/>

<div class="flex flex-col">
    <div class="${sizeClassnamesName} truncate"><c:out value="${model.brand.name}"/> <c:out value="${model.name}"/> (<c:out value="${year}"/>)</div>
    <div class="${sizeClassnamesCategory} text-black/60 truncate">
        <spring:message code="category.${model.subcategory.category.name}"/> / <spring:message code="subcategory.${model.subcategory.name}"/>
    </div>
</div>
