<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>
<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>
<c:set var="labelClassname" value="${isDisabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}"/>

<label for="${id}" class="flex items-center gap-2 text-sm text-black/80 select-none cursor-pointer min-h-7.5 ${labelClassname} ${inputClass}">
  <form:radiobutton
    path="${path}"
    id="${id}"
    value="${value}"
    disabled="${isDisabled}"
    cssClass="peer sr-only"
  />
  <span
    aria-hidden="true"
    class="flex h-4 w-4 shrink-0 items-center justify-center rounded-full border border-black/20 bg-white transition duration-150
           peer-checked:border-lime-600 peer-checked:bg-lime-50
           peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-lime-600
           peer-disabled:opacity-50 peer-disabled:bg-black/10 peer-checked:[&_.radio-dot]:scale-100"
  >
    <span class="radio-dot h-2 w-2 rounded-full bg-lime-600 scale-0 transition duration-150"></span>
  </span>
  <c:if test="${not empty label}">
    <span><c:out value="${label}"/></span>
  </c:if>
  <jsp:doBody/>
</label>
