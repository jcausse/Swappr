<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="confirmText" required="true" %>
<%@ attribute name="cancelText" required="true" %>
<%@ attribute name="formAction" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<dialog id="${id}" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 rounded-xl border border-black/10 p-4 max-w-sm w-[90vw] backdrop:bg-black/40">
    <h3 class="text-lg font-semibold mb-2"><c:out value="${title}"/></h3>

    <paw:divider />

    <div class="text-sm text-black/60 mt-2 mb-6">
        <jsp:doBody/>
    </div>

    <div class="flex justify-end gap-2">
        <paw:button variant="ghost" role="secondary" text="${cancelText}" onclick="document.getElementById('${id}').close()" />
        <form action="${formAction}" method="POST">
            <paw:button type="submit" role="danger" text="${confirmText}" />
        </form>
    </div>
</dialog>
