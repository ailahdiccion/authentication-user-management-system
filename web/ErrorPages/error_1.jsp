<%
    request.setAttribute("errTitle", "Uh oh!");
    request.setAttribute("errMessage", "Email does not exist in the DB; password cannot be empty!");
%>
<%@include file="error_template.jspf" %>