<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<spring:message code="notifications.title" var="titleMsg"/>
<spring:message code="notifications.subtitle" var="subtitleMsg"/>
<spring:message code="notifications.action.markAllRead" var="markAllReadLabel"/>
<spring:message code="notifications.filter.type.label" var="typeFilterLabel"/>
<spring:message code="notifications.filter.type.all" var="allTypesLabel"/>
<spring:message code="notifications.filter.clear" var="clearFiltersLabel"/>

<c:url value="/notifications" var="filterAction"/>
<c:url value="/notifications/read-all" var="markAllReadUrl"/>

<paw:layout titleKey="notifications.title">
    <div class="mb-8 flex flex-col sm:flex-row sm:items-center gap-4">
        <div>
            <div class="flex flex-row items-center gap-3">
                <h1 class="text-3xl font-bold"><c:out value="${titleMsg}"/></h1>
                <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
                    <paw:badge text="${recentUnreadNotificationsPage.totalCount}" classname="text-red-600 min-w-6! px-2!" size="md"/>
                </c:if>
            </div>
            <p class="text-black/60 mt-2"><c:out value="${subtitleMsg}"/></p>
        </div>

        <c:if test="${recentUnreadNotificationsPage.totalCount > 0}">
            <form action="${markAllReadUrl}" method="post" class="sm:ml-auto">
                <paw:button type="submit" variant="outline" icon="check-check" text="${markAllReadLabel}"/>
            </form>
        </c:if>
    </div>

    <form:form modelAttribute="filterForm" action="${filterAction}" method="get" id="filterForm" cssClass="flex flex-col gap-4 mb-6">
        <paw:card classname="p-2!">
            <paw:formButtonToggle
                path="unreadOnly"
                items="${readStatusOptions}"
                selectedOption="${filterForm.unreadOnly ? 'true' : 'false'}"
                translateLabels="true"
            />
        </paw:card>

        <paw:card>
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                <paw:formSelect
                    path="type"
                    label="${typeFilterLabel}"
                    placeholder="${allTypesLabel}"
                    items="${typeOptions}"
                    itemLabel="label"
                    itemValue="value"
                    translateLabels="true"
                    classname="w-full sm:w-72"
                />

                <c:if test="${filterForm.unreadOnly or not empty filterForm.type}">
                    <paw:linkButton
                        href="${filterAction}"
                        text="${clearFiltersLabel}"
                        variant="ghost"
                        role="secondary"
                        icon="x"
                    />
                </c:if>
            </div>
        </paw:card>
    </form:form>

    <c:choose>
        <c:when test="${empty notifications}">
            <paw:card>
                <div class="py-12 px-4 flex flex-col items-center gap-3 text-center">
                    <paw:icon name="bell-off" classname="text-3xl text-black/30"/>
                    <c:choose>
                        <c:when test="${filterForm.unreadOnly or not empty filterForm.type}">
                            <spring:message code="notifications.empty" var="emptyFilteredMsg"/>
                            <p class="text-black/60 text-base"><c:out value="${emptyFilteredMsg}"/></p>
                            <paw:linkButton href="${filterAction}" text="${clearFiltersLabel}" variant="outline" icon="x"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="notifications.empty.all" var="emptyAllMsg"/>
                            <p class="text-black/60 text-base"><c:out value="${emptyAllMsg}"/></p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </paw:card>
        </c:when>
        <c:otherwise>
            <div class="flex flex-col gap-3">
                <c:forEach var="notification" items="${notifications}">
                    <paw:notificationCard notification="${notification}" compact="${false}"/>
                </c:forEach>
            </div>

            <paw:pagination page="${notificationPage}" baseUrl="/notifications"/>
        </c:otherwise>
    </c:choose>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const typeSelect = document.getElementById('type');
            if (typeSelect) {
                typeSelect.addEventListener('change', function() {
                    document.getElementById('filterForm').submit();
                });
            }
        });
    </script>
</paw:layout>
