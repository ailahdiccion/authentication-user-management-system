<%
    request.setAttribute("errTitle", "404 - Not Found");
    request.setAttribute("errMessage", "The page you requested does not exist.");
%>
<%@include file="error_template.jspf" %>