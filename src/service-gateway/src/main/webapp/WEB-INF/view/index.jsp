<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html lang="en">
<head>
	<style type="text/css">
		.node-wrapper{
			padding-left: 10px;
		}
	</style>
</head>
<body>
    <div>
        <div>
            <h1>Hub Manager</h1>
			<c:set var="node" value="${node}" scope="request"/>
    		<jsp:include page="node.jsp"/>
        </div>
    </div>
</body>
</html>