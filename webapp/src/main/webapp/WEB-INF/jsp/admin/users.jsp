<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<spring:message code="admin.users.title" var="usersTitle"/>
<spring:message code="admin.users.subtitle" var="usersSubtitle"/>

<c:url value="/admin/users" var="usersBaseUrl"/>
<c:url value="/admin/users/suspend" var="suspendUserUrl"/>
<c:url value="/admin/users/unsuspend" var="unsuspendUserUrl"/>
<c:url value="/admin/users/grant-admin" var="grantAdminUrl"/>

<admin:layout titleKey="admin.users.title" title="${usersTitle}" subtitle="${usersSubtitle}">
    <div class="flex flex-col gap-6">
        <%-- Search User by Username --%>
        <paw:card>
            <form method="get" action="${usersBaseUrl}" class="flex flex-col sm:flex-row gap-3 items-end">
                <div class="flex-1 w-full">
                    <spring:message code="admin.users.search.label" var="searchLabel"/>
                    <spring:message code="admin.users.search.placeholder" var="searchPlaceholder"/>
                    <div class="flex flex-col gap-1">
                        <label for="admin-user-search" class="text-xs text-black/70 font-medium">
                            <c:out value="${searchLabel}"/>
                        </label>
                        <input id="admin-user-search" name="username" type="text" value="<c:out value='${searchedUsername}'/>" placeholder="${searchPlaceholder}"
                               class="w-full px-3 py-2 rounded-lg text-sm border border-black/20 focus-visible:border-lime-600 focus-visible:outline-2"/>
                    </div>
                </div>

                <spring:message code="admin.users.search.submit" var="submitSearch"/>
                <paw:button text="${submitSearch}" type="submit" variant="default" role="default" icon="search" classname="w-full sm:w-auto"/>
            </form>
        </paw:card>

        <%-- Search Result --%>
        <c:if test="${userNotFound}">
            <spring:message code="admin.users.error.notFound" var="userNotFoundMsg"/>
            <paw:banner text="${userNotFoundMsg}" role="danger" icon="alert-circle" />
        </c:if>

        <c:if test="${not empty targetUser}">
            <c:url value="/profile/${targetUser.id}" var="targetUserProfileUrl"/>
            <spring:message code="admin.users.profileCard.title" var="profileCardTitle"/>
            <paw:card title="${profileCardTitle}">
                <div class="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between mt-2">
                    <div class="flex gap-4 items-center">
                        <paw:userAvatar user="${targetUser}" size="lg"/>
                        <div class="flex flex-col">
                            <h3 class="font-bold text-lg"><c:out value="${targetUser.displayName}"/></h3>
                            <p class="text-sm text-black/60 font-mono">@<c:out value="${targetUser.username}"/></p>
                            <p class="text-xs text-black/50 mt-0.5"><c:out value="${targetUser.email}"/></p>
                            <div class="flex items-center gap-1.5 mt-2">
                                <c:forEach var="role" items="${targetUserRoles}">
                                    <c:choose>
                                        <c:when test="${role.name() eq 'ADMIN'}">
                                            <paw:badge text="${role.name()}" classname="text-purple-700 bg-purple-100" size="sm"/>
                                        </c:when>
                                        <c:otherwise>
                                            <paw:badge text="${role.name()}" classname="text-neutral-700 bg-neutral-100" size="sm"/>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                <c:if test="${targetUser.suspended}">
                                    <spring:message code="admin.users.badge.suspended" var="suspendedBadgeText"/>
                                    <paw:badge text="${suspendedBadgeText}" classname="text-red-700 bg-red-100" size="sm"/>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="flex flex-col sm:flex-row gap-2 items-stretch sm:items-center self-end sm:self-center">
                        <spring:message code="admin.users.viewProfile" var="viewProfileText"/>
                        <paw:linkButton href="${targetUserProfileUrl}" text="${viewProfileText}" variant="outline" size="sm" icon="external-link"/>

                        <c:if test="${not isTargetUserAdmin}">
                            <form:form method="post" action="${grantAdminUrl}" modelAttribute="grantRoleForm">
                                <input type="hidden" name="username" value="<c:out value='${targetUser.username}'/>"/>
                                <spring:message code="admin.users.grantAdmin.btn" var="grantAdminBtnText"/>
                                <paw:button text="${grantAdminBtnText}" type="submit" variant="outline" role="default" size="sm" icon="shield"/>
                            </form:form>
                        </c:if>

                        <c:choose>
                            <c:when test="${targetUser.suspended}">
                                <form:form method="post" action="${unsuspendUserUrl}" modelAttribute="suspendUserForm">
                                    <input type="hidden" name="username" value="<c:out value='${targetUser.username}'/>"/>
                                    <spring:message code="admin.users.unsuspend.btn" var="unsuspendBtnText"/>
                                    <paw:button text="${unsuspendBtnText}" type="submit" variant="outline" role="default" size="sm" icon="user-check"/>
                                </form:form>
                            </c:when>
                            <c:otherwise>
                                <form:form method="post" action="${suspendUserUrl}" modelAttribute="suspendUserForm">
                                    <input type="hidden" name="username" value="<c:out value='${targetUser.username}'/>"/>
                                    <spring:message code="admin.users.suspend.btn" var="suspendBtnText"/>
                                    <paw:button text="${suspendBtnText}" type="submit" variant="outline" role="danger" size="sm" icon="user-x"/>
                                </form:form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </paw:card>
        </c:if>


        <%-- Direct Action Cards --%>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <%-- User Suspend Form --%>
            <spring:message code="admin.users.suspend.title" var="suspendUserTitle"/>
            <spring:message code="admin.users.suspend.desc" var="suspendUserDesc"/>
            <paw:card title="${suspendUserTitle}" subtitle="${suspendUserDesc}">
                <form:form method="post" action="${suspendUserUrl}" modelAttribute="suspendUserForm" class="flex flex-col gap-4 mt-2">
                    <spring:message code="admin.users.usernameLabel" var="usernameLabel"/>
                    <spring:message code="admin.users.usernamePlaceholder" var="usernamePlaceholder"/>
                    <paw:formInput path="username" label="${usernameLabel}" placeholder="${usernamePlaceholder}" variant="outline"/>
                    <spring:message code="admin.users.suspend.submit" var="submitUserSuspend"/>
                    <paw:button text="${submitUserSuspend}" type="submit" variant="default" role="danger" icon="user-x" classname="self-end"/>
                </form:form>
            </paw:card>

            <%-- User Grant Admin Role Form --%>
            <spring:message code="admin.users.grantAdmin.title" var="grantAdminTitle"/>
            <spring:message code="admin.users.grantAdmin.desc" var="grantAdminDesc"/>
            <paw:card title="${grantAdminTitle}" subtitle="${grantAdminDesc}">
                <form:form method="post" action="${grantAdminUrl}" modelAttribute="grantRoleForm" class="flex flex-col gap-4 mt-2">
                    <spring:message code="admin.users.grantAdmin.usernameLabel" var="grantUsernameLabel"/>
                    <spring:message code="admin.users.usernamePlaceholder" var="grantUsernamePlaceholder"/>
                    <paw:formInput path="username" label="${grantUsernameLabel}" placeholder="${grantUsernamePlaceholder}" variant="outline"/>
                    <spring:message code="admin.users.grantAdmin.submit" var="submitGrantAdmin"/>
                    <paw:button text="${submitGrantAdmin}" type="submit" variant="default" role="default" icon="shield" classname="self-end"/>
                </form:form>
            </paw:card>
        </div>
    </div>
</admin:layout>
