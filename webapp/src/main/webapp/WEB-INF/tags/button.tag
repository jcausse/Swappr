<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="false" %>
<%@ attribute name="icon" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="role" required="false" %>
<%@ attribute name="id" required="false" %>
<%@ attribute name="classname" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="on" required="false" type="java.lang.Boolean" %>
<%@ attribute name="onclick" required="false" %>
<%@ attribute name="ariaLabel" required="false" %>
<%@ attribute name="popovertarget" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="btnType" value="${not empty type ? type : 'button'}"/>
<c:set var="btnVariant" value="${not empty variant ? variant : 'default'}"/>
<c:set var="btnSize" value="${not empty size ? size : 'md'}"/>
<c:set var="btnRole" value="${not empty role ? role : 'default'}"/>
<c:set var="btnClass" value="${not empty classname ? classname : ''}"/>

<c:set var="isDisabled" value="${disabled ne null ? disabled : false}"/>
<c:set var="isOn" value="${on ne null ? on : false}"/>

<c:set var="variantClassnames" value="${
  btnVariant eq 'outline'
    ? 'border border-current/15 hover:bg-current/10 disabled:bg-current/10 active:bg-current/15 disabled:active:bg-current/10 data-[state=on]:bg-current/10'
    : btnVariant eq 'ghost'
    ? 'hover:bg-current/10 disabled:bg-current/10 active:bg-current/15 disabled:active:bg-current/10 data-[state=on]:bg-current/10'
    : '[background-position:-1px_-1px] [background-size:calc(100%+2px)_calc(100%+2px)] bg-gradient-to-b from-current/3 to-current/7 border-t border-b border-t-white/30 border-b-black/20
       hover:from-current/5 hover:to-current/10 active:border-t-black/15 active:border-b-white/30 active:translate-y-px'
}"/>

<c:set var="sizeClassnamesAll" value="${
  btnSize eq 'sm'
    ? 'p-1 text-xs/3'
    : btnSize eq 'lg'
    ? 'p-4 text-base/5'
    : 'p-2 text-sm/4'
}"/>

<c:set var="sizeClassnamesNotIcon" value="${
  btnSize eq 'sm'
    ? 'px-2 min-w-16'
    : btnSize eq 'lg'
    ? 'px-6 min-w-20'
    : 'px-4 min-w-20'
}"/>

<c:set var="sizeClassnamesIcon" value="${
  btnSize eq 'sm'
    ? 'min-w-5.5'
    : btnSize eq 'lg'
    ? 'min-w-13.5'
    : 'min-w-8.5'
}"/>

<c:set var="sizeClassnames" value="${sizeClassnamesAll}" />
<c:if test="${empty icon or not empty text}">
    <c:set var="sizeClassnames" value="${sizeClassnames} ${sizeClassnamesNotIcon}" />
</c:if>
<c:if test="${not empty icon and empty text}">
    <c:set var="sizeClassnames" value="${sizeClassnames} ${sizeClassnamesIcon}" />
</c:if>

<c:set var="roleClassnames" value="${
  btnRole eq 'danger'
    ? 'text-red-600'
    : btnRole eq 'success'
    ? 'text-green-600'
    : btnRole eq 'secondary'
    ? 'text-black/60'
    : 'text-lime-600'
}"/>

<button
    type="${btnType}"
    id="${id}"
    class="
        font-semibold rounded-lg
        flex flex-row flex-shrink-0 items-center justify-center gap-2
        cursor-pointer transition duration-150
        focus-visible:outline outline-offset-0 outline-lime-600
        focus-visible:shadow-[0_0_0_3px] shadow-lime-600/30
        disabled:text-black/40 disabled:cursor-default
        ${variantClassnames}
        ${sizeClassnames}
        ${roleClassnames}
        ${btnClass}
    "
    <c:if test="${btnType eq 'submit'}">data-submit-guard</c:if>
    <c:if test="${isDisabled}">disabled</c:if>
    <c:if test="${isOn}">data-state="on"</c:if>
    <c:if test="${not empty onclick}">onclick="${onclick}"</c:if>
    <c:if test="${not empty ariaLabel}">aria-label="${ariaLabel}"</c:if>
    <c:if test="${not empty popovertarget}">popovertarget="${popovertarget}"</c:if>
>
    <c:if test="${not empty icon}">
        <paw:icon name="${icon}" />
    </c:if>
    <c:if test="${not empty text}">
        <c:out value="${text}"/>
    </c:if>
    <jsp:doBody/>
</button>

<c:if test="${btnType eq 'submit'}">
    <script>
        if (!window.__submitGuardInstalled) {
            window.__submitGuardInstalled = true;
            document.addEventListener('submit', function (e) {
                var form = e.target;
                var button = form.querySelector('button[type="submit"][data-submit-guard]');
                if (button) {
                    // Disable after the current submit is dispatched, so the form still sends.
                    setTimeout(function () { button.disabled = true; }, 0);
                }
            }, true);
        }
    </script>
</c:if>
