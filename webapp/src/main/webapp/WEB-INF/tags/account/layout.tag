<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="actionHref" required="false" %>
<%@ attribute name="actionText" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="account" tagdir="/WEB-INF/tags/account" %>

<paw:layout variant="wide" titleKey="${titleKey}">
    <div class="mb-8 flex flex-row items-center">
        <div>
            <h1 class="text-3xl font-bold"><c:out value="${title}"/></h1>
            <p class="text-black/60 mt-2"><c:out value="${subtitle}"/></p>
        </div>
        <c:if test="${not empty actionHref and not empty actionText}">
            <c:url value="${actionHref}" var="href" />
            <paw:linkButton href="${href}" text="${actionText}" classname="ml-auto" />
        </c:if>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-10 gap-4">
        <div class="md:col-span-3">
            <account:sidebar user="${currentUser.get()}" />
        </div>

        <div class="md:col-span-7">
            <jsp:doBody />
        </div>
    </div>
</paw:layout>
