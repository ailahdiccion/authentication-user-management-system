package controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.*;
import javax.servlet.http.*;

public class CaptchaServlet extends HttpServlet {

    private String reCaptchaSecretKey;

    @Override
    public void init() throws ServletException {
        reCaptchaSecretKey = getServletContext().getInitParameter("reCaptchaSecretKey");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        Integer attempts = (Integer) session.getAttribute("captchaAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        String token = request.getParameter("g-recaptcha-response");

        if (token == null || token.isEmpty()) {
            session.setAttribute("captchaError", "Please complete CAPTCHA verification.");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        boolean isValid = verifyCaptcha(token);

        if (!isValid) {
            attempts++;
            session.setAttribute("captchaAttempts", attempts);

            if (attempts >= 3) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/ErrorPages/error_5.jsp");
                return;
            }

            session.setAttribute("captchaError", "CAPTCHA verification failed. Please try again.");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        session.removeAttribute("captchaAttempts");

        request.setAttribute("captchaValid", true);
        request.getRequestDispatcher("/loginServlet").forward(request, response);
    }

    private boolean verifyCaptcha(String responseToken) throws IOException {

        String url = "https://www.google.com/recaptcha/api/siteverify";
        String params = "secret=" + reCaptchaSecretKey + "&response=" + responseToken;

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(params.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        in.close();

        return result.toString().contains("\"success\": true");
    }
}


