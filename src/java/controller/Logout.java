package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class Logout extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}
