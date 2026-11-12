<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<spring:message code="notifications.preferences.title" var="titleMsg"/>
<spring:message code="notifications.preferences.subtitle" var="subtitleMsg"/>
<spring:message code="notifications.preferences.backToNotifications" var="backLabel"/>
<spring:message code="notifications.preferences.table.type" var="typeHeaderLabel"/>
<spring:message code="notifications.preferences.table.channel" var="channelHeaderLabel"/>
<spring:message code="notifications.preferences.submit" var="submitLabel"/>
<spring:message code="notifications.preferences.cancel" var="cancelLabel"/>

<c:url value="/notifications" var="notificationsUrl"/>
<c:url value="/notifications/preferences" var="preferencesAction"/>

<paw:layout titleKey="notifications.preferences.title">
    <div class="mb-8 flex flex-col sm:flex-row sm:items-center gap-4">
        <div>
            <h1 class="text-3xl font-bold"><c:out value="${titleMsg}"/></h1>
            <p class="text-black/60 mt-2"><c:out value="${subtitleMsg}"/></p>
        </div>

        <div class="sm:ml-auto">
            <paw:linkButton href="${notificationsUrl}" text="${backLabel}" size="sm" variant="outline"/>
        </div>
    </div>

    <c:if test="${param.saved eq 'true'}">
        <spring:message code="notifications.preferences.saved" var="savedMsg"/>
        <paw:banner text="${savedMsg}" role="success" icon="check-circle" classname="mb-6"/>
    </c:if>

    <form:form modelAttribute="preferencesForm" action="${preferencesAction}" method="post" cssClass="flex flex-col gap-6">
        <form:errors path="" element="div" cssClass="text-xs text-red-600"/>

        <paw:card>
            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="border-b border-black/10 text-xs font-semibold uppercase tracking-wide text-black/60">
                            <th class="pb-3 pr-4"><c:out value="${typeHeaderLabel}"/></th>
                            <th class="pb-3 pl-4 w-56"><c:out value="${channelHeaderLabel}"/></th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-black/10">
                        <c:forEach var="type" items="${configurableTypes}">
                            <spring:message code="notification.type.${type.name()}" var="typeLabel"/>
                            <spring:message code="notification.type.${type.name()}.description" var="typeDesc"/>
                            <tr>
                                <td class="py-3.5 pr-4 align-middle">
                                    <div class="font-medium text-sm text-black"><c:out value="${typeLabel}"/></div>
                                    <p class="text-xs text-black/60 mt-0.5"><c:out value="${typeDesc}"/></p>
                                </td>
                                <td class="py-3.5 pl-4 align-middle w-56">
                                    <paw:formSelect
                                        path="preferences[${type.name()}]"
                                        id="pref_${type.name()}"
                                        items="${optionsByType[type.name()]}"
                                        itemLabel="label"
                                        itemValue="value"
                                        translateLabels="true"
                                        variant="outline"
                                        classname="w-full sm:w-56"
                                    />
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </paw:card>

        <div class="flex flex-row items-center gap-2">
            <paw:button type="submit" text="${submitLabel}"/>
            <paw:linkButton href="${notificationsUrl}" text="${cancelLabel}" variant="outline" role="secondary"/>
        </div>
    </form:form>
</paw:layout>
