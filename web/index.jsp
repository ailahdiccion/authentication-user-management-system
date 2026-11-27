<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    if (session != null && session.getAttribute("email") != null) {
        response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
        return;
    }

    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/indexStyles.css">
    <title>Login</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>

<header>
    <jsp:include page="<%= (String)application.getAttribute("header") %>" />
</header>

<main>
    <div class="login-container">

        <h2>Welcome Back!</h2>
        <p class="subtitle">Please enter your details to sign in.</p>

        <%
            String captchaError = (String) session.getAttribute("captchaError");
            if (captchaError != null) {
        %>
            <div class="status-message" style="background:#fee2e2; color:#b91c1c;">
                <%= captchaError %>
            </div>
        <%
                session.removeAttribute("captchaError");
            }
        %>

        <form action="${pageContext.request.contextPath}/CaptchaServlet" method="POST">

            <div class="form-group">
                <label>Username</label>
                <input type="text" name="email" required>
            </div>

            <div class="form-group">
                <label>Password</label>
                <input type="password" name="password" required>
            </div>

            <div class="captcha-wrapper">
                <div class="g-recaptcha" data-sitekey="REPLACE_CAPTCHA_KEY_LOCALLY"></div>
            </div>

            <button type="submit">Sign In</button>

        </form>
    </div>
</main>

<footer>
    <jsp:include page="<%= (String)application.getAttribute("footer") %>" />
</footer>

</body>
</html>


