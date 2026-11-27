<%
    request.setAttribute("errTitle", "Authentication Failed");
    request.setAttribute("errMessage", "Too many failed CAPTCHA attempts. Please try logging in again.");
%>
<%@include file="error_template.jspf" %>
