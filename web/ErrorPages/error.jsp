<%
    request.setAttribute("errTitle", "SQL Error");
    request.setAttribute("errMessage", "SQL Error detected. Please check the server logs.");
%>
<%@include file="error_template.jspf" %>