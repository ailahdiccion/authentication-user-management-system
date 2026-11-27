<%
    request.setAttribute("errTitle", "Uh oh!");
    request.setAttribute("errMessage", "You have entered the wrong Email (Username) and Password!");
%>
<%@include file="error_template.jspf" %>