package controller;

import java.io.*;
import java.sql.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

public class LoginServlet extends HttpServlet {

    private Connection conn;
    private String secretKey;
    private String cipherAlgorithm;

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private FileHandler fileHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        secretKey = getServletContext().getInitParameter("secretKey");
        cipherAlgorithm = getServletContext().getInitParameter("cipherAlgorithm");

        try {
            Class.forName(config.getInitParameter("jdbcClassName"));

            conn = DriverManager.getConnection(
                config.getInitParameter("jdbcDriverURL") + "://"
                + config.getInitParameter("dbHostName") + ":"
                + config.getInitParameter("dbPort") + "/"
                + config.getInitParameter("databaseName"),
                config.getInitParameter("dbUserName"),
                config.getInitParameter("dbPassword")
            );

        } catch (Exception e) {
            conn = null;
        }

        try {
            String appPath = getServletContext().getRealPath("");
            String logDirPath = appPath + File.separator + "log";

            File logDir = new File(logDirPath);
            if (!logDir.exists()) logDir.mkdirs();

            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String logFile = logDirPath + File.separator + "Log_" + date + ".log";

            fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String decryptPassword(String encryptedPassword) {
        try {
            byte[] keyBytes = secretKey.getBytes("UTF-8");
            byte[] validKey = new byte[16];
            System.arraycopy(keyBytes, 0, validKey, 0, Math.min(keyBytes.length, 16));

            SecretKeySpec key = new SecretKeySpec(validKey, cipherAlgorithm);
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(encryptedPassword);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted, "UTF-8");

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info("Start of login attempt");

        Boolean captchaPassed = (Boolean) request.getAttribute("captchaValid");

        if (captchaPassed == null || !captchaPassed) {
            LOGGER.warning("CAPTCHA validation FAILED");
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error_5.jsp");
            return;
        }

        LOGGER.info("CAPTCHA validation SUCCESS");

        String email = request.getParameter("email");
        String pass = request.getParameter("password");

        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT Email, Password, UserRole FROM USERS WHERE Email = ?"
            );

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String decrypted = decryptPassword(rs.getString("Password"));

                if (decrypted != null && decrypted.equals(pass)) {

                    HttpSession oldSession = request.getSession(false);
                    if (oldSession != null) oldSession.invalidate();

                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("email", email);
                    newSession.setAttribute("role", rs.getString("UserRole"));
                    newSession.setMaxInactiveInterval(5 * 60);

                    LOGGER.info("Successful login");

                    response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
                    return;

                } else {
                    LOGGER.warning("Password validation FAILED");
                    response.sendRedirect(request.getContextPath() + "/ErrorPages/error_2.jsp");
                    return;
                }

            } else {
                LOGGER.warning("User NOT found");
                response.sendRedirect(request.getContextPath() + "/ErrorPages/error_3.jsp");
                return;
            }

        } catch (SQLException e) {
            LOGGER.severe("SQL ERROR");
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error.jsp");
        }

        LOGGER.info("End of login process");
    }
}


