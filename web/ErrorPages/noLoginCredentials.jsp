<%
    request.setAttribute("errTitle", "Missing Fields");
    request.setAttribute("errMessage", "Username (Email) and Password cannot be empty!");
%>
<%@include file="error_template.jspf" %>