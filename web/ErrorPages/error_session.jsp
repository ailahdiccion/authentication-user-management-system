<%
    request.setAttribute("errTitle", "Session Error");
    request.setAttribute("errMessage", "Your session is invalid or expired. Please login again.");
%>
<%@include file="error_template.jspf" %>