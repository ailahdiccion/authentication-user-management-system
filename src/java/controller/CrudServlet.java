package controller;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.*;

public class CrudServlet extends HttpServlet {

    private Connection conn;
    private String secretKey;
    private String cipherAlgorithm;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        secretKey = getServletContext().getInitParameter("secretKey");
        cipherAlgorithm = getServletContext().getInitParameter("cipherAlgorithm");

        try {
            Class.forName(config.getInitParameter("jdbcClassName"));

            String username = config.getInitParameter("dbUserName");
            String password = config.getInitParameter("dbPassword");

            String url = config.getInitParameter("jdbcDriverURL")
                    + "://"
                    + config.getInitParameter("dbHostName")
                    + ":"
                    + config.getInitParameter("dbPort")
                    + "/"
                    + config.getInitParameter("databaseName");

            conn = DriverManager.getConnection(url, username, password);
            System.out.println("CrudServlet DB Connected: " + url);

        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }
    }

    private String encryptPassword(String plainText) {
        try {
            byte[] keyBytes = secretKey.getBytes("UTF-8");
            byte[] validKey = new byte[16];
            System.arraycopy(keyBytes, 0, validKey, 0, Math.min(keyBytes.length, 16));
            
            SecretKeySpec key = new SecretKeySpec(validKey, cipherAlgorithm);
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encryptedValue = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
        System.out.println(encryptPassword("admin"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error_session.jsp");
            return;
        }

        if (conn == null) {
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        boolean isAdmin = role != null && role.equalsIgnoreCase("ADMIN");

        if (!isAdmin && action != null) {
            if (action.equalsIgnoreCase("create")
                    || action.equalsIgnoreCase("update")
                    || action.equalsIgnoreCase("delete")) {
                response.sendRedirect(request.getContextPath() + "/ErrorPages/error_session.jsp");
                return;
            }
        }

        try {
            if ("create".equalsIgnoreCase(action)) {

                String email = request.getParameter("email");
                String plainPass = request.getParameter("password");
                String newRole = request.getParameter("userrole");
                
                String encryptedPass = encryptPassword(plainPass);

                String sql = "INSERT INTO USERS (Email, Password, UserRole) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, encryptedPass);
                ps.setString(3, newRole);

                int rows = ps.executeUpdate();
                ps.close();

                session.setAttribute("message",
                        rows == 1 ? "User created successfully!" : "Creation failed.");

                response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
                return;
            }

            if ("update".equalsIgnoreCase(action)) {

                String email = request.getParameter("updateEmail");
                String plainPass = request.getParameter("updatePass");
                String newRole = request.getParameter("updateUserRole");

                String encryptedPass = encryptPassword(plainPass);

                String sql = "UPDATE USERS SET Password = ?, UserRole = ? WHERE Email = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, encryptedPass);
                ps.setString(2, newRole);
                ps.setString(3, email);

                int rows = ps.executeUpdate();
                ps.close();

                String loggedEmail = (String) session.getAttribute("email");
                if (email != null && email.equalsIgnoreCase(loggedEmail)) {
                    session.setAttribute("role", newRole);
                }

                session.setAttribute("message",
                        rows == 1 ? "User updated successfully!" : "Update failed.");

                response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
                return;
            }

            if ("delete".equalsIgnoreCase(action)) {

                String email = request.getParameter("email");
                String loggedInEmail = (String) session.getAttribute("email");

                if (email != null && email.equalsIgnoreCase(loggedInEmail)) {
                    session.setAttribute("errorMessage", "You cannot delete your own admin account.");
                    response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
                    return;
                }

                String sql = "DELETE FROM USERS WHERE Email = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);

                int rows = ps.executeUpdate();
                ps.close();

                session.setAttribute("message",
                        rows == 1 ? "User deleted successfully!" : "Delete failed.");

                response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
                return;
            }

            if ("viewresults".equalsIgnoreCase(action)) {

                String loggedEmail = (String) session.getAttribute("email");
                List<User> userList = new ArrayList<>();

                PreparedStatement ps;
                String sql;

                if (isAdmin) {
                    sql = "SELECT Email, Password, UserRole FROM USERS ORDER BY Email";
                    ps = conn.prepareStatement(sql);
                } else {
                    sql = "SELECT Email, Password, UserRole FROM USERS WHERE Email = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, loggedEmail);
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    userList.add(new User(
                            rs.getString("Email"),
                            rs.getString("Password"), 
                            rs.getString("UserRole")
                    ));
                }

                rs.close();
                ps.close();

                session.setAttribute("userList", userList);
                request.getRequestDispatcher("success.jsp").forward(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error.jsp");
        }
    }
}

