<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="classname" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="inputClass" value="${not empty classname ? classname : ''}"/>
<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>

<div class="flex flex-col gap-1 ${inputClass}">
  <paw:checkbox path="${path}" id="${path}" label="${label}" disabled="${isDisabled}"/>
  <form:errors path="${path}" element="div" cssClass="text-xs text-red-600"/>
</div>
