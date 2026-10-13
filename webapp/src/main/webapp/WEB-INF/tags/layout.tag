<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="titleParams" required="false" %>
<%@ attribute name="variant" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>


<c:set var="layoutVariant" value="${not empty variant ? variant : 'default'}"/>
<c:set var="variantClassname" value="${
  layoutVariant eq 'wide' ? 'max-w-6xl'
  : layoutVariant eq 'narrow' ? 'max-w-3xl'
  : 'max-w-5xl'
}"/>

<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">

<paw:head titleKey="${titleKey}" titleParams="${titleParams}" />

<body class="min-h-screen bg-neutral-50">
    <paw:navbar/>

    <main class="${variantClassname} mx-auto px-6 pt-8 pb-16">
        <jsp:doBody />
    </main>
</body>

</html>
