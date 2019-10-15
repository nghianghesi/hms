<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="node-wrapper">
	<span>${node.getHubid()} / ${node.getName()} / ${node.getIsActive()}</span>
	<c:forEach var="subnode" items="${node.getSubHubs()}">
	    <c:set var="node" value="${subnode}" scope="request"/>
	    <jsp:include page="node.jsp"/>
	</c:forEach>
</div>