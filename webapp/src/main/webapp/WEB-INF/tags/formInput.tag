<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="min" required="false" %>
<%@ attribute name="max" required="false" %>
<%@ attribute name="step" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="classname" required="false" %>
<%@ attribute name="inputClassname" required="false" %>
<%@ attribute name="multiple" required="false" type="java.lang.Boolean" %>
<%@ attribute name="accept" required="false" %>
<%@ attribute name="showPasswordToggle" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="inputVariant" value="${not empty variant ? variant : 'default'}"/>
<c:set var="inputType" value="${not empty type ? type : 'text'}"/>
<c:set var="hasPasswordToggle" value="${showPasswordToggle ne null ? showPasswordToggle : (inputType eq 'password')}"/>
<c:set var="inputMin" value="${not empty min ? min : ''}"/>
<c:set var="inputMax" value="${not empty max ? max : ''}"/>
<c:set var="inputStep" value="${not empty step ? step : ''}"/>
<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>
<c:set var="innerClass" value="${not empty inputClassname ? inputClassname : ''}"/>

<c:set var="inputMultiple" value="${multiple ne null ? multiple : false}"/>
<c:set var="inputAccept" value="${not empty accept ? accept : ''}"/>

<spring:bind path="${path}">
  <c:set var="hasBindError" value="${status.error}"/>
</spring:bind>

<c:set var="bindErrorClasses" value="${hasBindError ? (inputVariant eq 'outline' ? 'border-red-600! outline-red-600/30' : 'outline outline-red-600 shadow-red-600/30 from-red-600/5 to-red-600/2') : ''}"/>

<c:set var="variantClassnames" value="${
  inputVariant eq 'outline'
    ? 'border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2
       disabled:bg-black/5 disabled:text-black/40
       peer-[.errors]/errors:border-red-600! peer-[.errors]/errors:outline-red-600/30'
    : 'border-t border-b border-black/15 border-b-white/20 bg-gradient-to-b from-black/5 to-black/2 bg-border
       focus-visible:outline outline-lime-600 focus-visible:shadow-[0_0_0_3px] shadow-lime-600/30
       disabled:from-black/10 disabled:to-black/8 disabled:text-black/40
       peer-[.errors]/errors:outline peer-[.errors]/errors:outline-red-600 peer-[.errors]/errors:shadow-red-600/30
       peer-[.errors]/errors:from-red-600/5 peer-[.errors]/errors:to-red-600/2'
}"/>

<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>

<%-- Define the markup in reverse order so we can use errors to conditionally style the input --%>
<div class="flex flex-col-reverse gap-1 ${inputClass}">
  <form:errors path="${path}" element="div" cssClass="text-xs text-red-600 peer/errors errors"/>

  <c:choose>
    <c:when test="${inputType eq 'textarea'}">
      <form:textarea
        path="${path}"
        id="${path}"
        placeholder="${placeholder}"
        disabled="${isDisabled}"
        cssClass="px-2 py-1 rounded-lg text-sm outline-0 transition duration-150 outline-lime-600/30 placeholder:text-black/40 ${variantClassnames} ${bindErrorClasses} ${innerClass}"
      />
    </c:when>
    <c:when test="${hasPasswordToggle}">
      <div class="relative flex items-center w-full peer-[.errors]/errors:[&_input]:border-red-600! peer-[.errors]/errors:[&_input]:outline-red-600/30">
        <form:input
          path="${path}"
          id="${path}"
          type="password"
          placeholder="${placeholder}"
          disabled="${isDisabled}"
          min="${inputMin}"
          max="${inputMax}"
          step="${inputStep}"
          multiple="${inputMultiple}"
          accept="${inputAccept}"
          cssClass="w-full px-2 py-1 pr-9 rounded-lg text-sm outline-0 transition duration-150 outline-lime-600/30 placeholder:text-black/40 ${variantClassnames} ${bindErrorClasses} ${innerClass}"
        />
        <spring:message code="a11y.togglePassword" var="togglePasswordLabel"/>
        <button
          type="button"
          class="absolute right-2.5 top-1/2 -translate-y-1/2 text-black/40 hover:text-black/70 focus:outline-none cursor-pointer flex items-center justify-center p-1"
          onclick="var input = this.previousElementSibling; var icon = this.querySelector('i'); if (input.type === 'password') { input.type = 'text'; icon.className = 'icon-eye-off'; } else { input.type = 'password'; icon.className = 'icon-eye'; } input.focus();"
          tabindex="-1"
          aria-label="<c:out value='${togglePasswordLabel}'/>"
        >
          <paw:icon name="eye" classname="text-base" />
        </button>
      </div>
    </c:when>
    <c:otherwise>
      <form:input
        path="${path}"
        id="${path}"
        type="${inputType}"
        placeholder="${placeholder}"
        disabled="${isDisabled}"
        min="${inputMin}"
        max="${inputMax}"
        step="${inputStep}"
        multiple="${inputMultiple}"
        accept="${inputAccept}"
        cssClass="px-2 py-1 rounded-lg text-sm outline-0 transition duration-150 outline-lime-600/30 placeholder:text-black/40 ${variantClassnames} ${bindErrorClasses} ${innerClass}"
      />
    </c:otherwise>
  </c:choose>

  <c:if test="${not empty label}">
    <label for="${path}" class="text-xs text-black/70 font-medium">
      <c:out value="${label}"/>
    </label>
  </c:if>
</div>
