<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<c:url value="/admin" var="dashboardUrl"/>
<c:url value="/admin/listings" var="listingsUrl"/>
<c:url value="/admin/offers" var="offersUrl"/>
<c:url value="/admin/users" var="usersUrl"/>
<c:url value="/admin/catalog" var="catalogUrl"/>
<c:url value="/" var="homeUrl"/>

<spring:message code="admin.sidebar.title" var="sidebarTitle"/>
<spring:message code="admin.sidebar.overview" var="overviewTitle"/>
<spring:message code="admin.sidebar.listings" var="listingsTitle"/>
<spring:message code="admin.sidebar.offers" var="offersTitle"/>
<spring:message code="admin.sidebar.users" var="usersTitle"/>
<spring:message code="admin.sidebar.catalog" var="catalogTitle"/>
<spring:message code="admin.sidebar.backToSite" var="backToSiteTitle"/>

<paw:card title="${sidebarTitle}" classname="sticky top-24">
    <div class="flex flex-col gap-1 mt-4">
        <admin:link href="${dashboardUrl}" text="${overviewTitle}" icon="layout-dashboard" />
        <admin:link href="${listingsUrl}" text="${listingsTitle}" icon="store" />
        <admin:link href="${offersUrl}" text="${offersTitle}" icon="handshake" />
        <admin:link href="${usersUrl}" text="${usersTitle}" icon="users" />
        <admin:link href="${catalogUrl}" text="${catalogTitle}" icon="tags" />

        <paw:divider />

        <admin:link href="${homeUrl}" text="${backToSiteTitle}" icon="arrow-left" />
    </div>
</paw:card>
