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
    class="flex h-4 w-4 shrink-0 items-center justify-center rounded-[4px] border border-black/20 bg-white text-white transition
           peer-checked:border-lime-600 peer-checked:bg-lime-600
           peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-lime-600
           peer-disabled:opacity-50 peer-checked:[&_.icon-check]:opacity-100"
  >
    <paw:icon name="check" classname="text-[10px]/none opacity-0" />
  </span>
  <c:if test="${not empty label}">
    <span><c:out value="${label}"/></span>
  </c:if>
  <jsp:doBody/>
</label>
