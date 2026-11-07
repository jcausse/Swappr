<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>
<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>
<c:set var="checkboxValue" value="${not empty value ? value : 'true'}"/>
<c:set var="labelClassname" value="${isDisabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}"/>

<label for="${id}" class="flex items-center gap-2 text-sm text-black/80 select-none ${labelClassname} ${inputClass}">
  <form:checkbox
    path="${path}"
    id="${id}"
    value="${checkboxValue}"
    disabled="${isDisabled}"
    cssClass="peer sr-only"
  />
  <span
    aria-hidden="true"
    class="flex h-4 w-4 shrink-0 items-center justify-center rounded-md border border-black/20 bg-white text-lime-600 transition
           peer-checked:border-lime-600 peer-checked:bg-lime-50
           peer-focus-visible:outline-2 peer-focus-visible:outline-offset-0 outline-lime-600/30 peer-focus-visible:border-lime-600
           peer-disabled:opacity-50 peer-disabled:bg-black/10 peer-checked:[&_.check-icon]:opacity-100"
  >
    <paw:icon name="check" classname="check-icon font-bold text-xl/none translate-x-0.5 -translate-y-0.25 opacity-0 drop-shadow-[1px_1px_white,-1px_-1px_white]" />
  </span>
  <c:if test="${not empty label}">
    <span><c:out value="${label}"/></span>
  </c:if>
  <jsp:doBody/>
</label>
