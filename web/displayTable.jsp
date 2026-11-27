<%@page import="java.util.List"%>
<%@page import="controller.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/displayTableStyles.css">

<%
    String role = (String) session.getAttribute("role");
    boolean isAdmin = role != null && role.equalsIgnoreCase("ADMIN");
    List<User> userList = (List<User>) session.getAttribute("userList");
%>

<div class="results-container">
    <table class="db-table">
        <thead>
            <tr>
                <th>Username</th>
                <th>Password</th>
                <th>User Role</th>
                <th>Actions</th>
            </tr>
        </thead>

        <tbody>
            <%
                if (userList != null && !userList.isEmpty()) {
                    for (User user : userList) {
                        String email = user.getEmail();
                        String password = user.getPassword();
                        String userRole = user.getUserRole();
                        String roleLabel = (userRole != null && userRole.equalsIgnoreCase("ADMIN")) ? "Admin" : "Guest";
            %>
            <tr>
                <td><%= email%></td>
                <td><%= password%></td>
                <td><%= roleLabel%></td>

                <td>
                    <% if (isAdmin) {%>
                    <div class="actions-container">
                        <button type="button"
                                class="edit-btn"
                                data-email="<%= email%>"
                                data-pass="<%= password%>"
                                data-role="<%= userRole%>"
                                onclick="openUpdateFromBtn(this)">
                            Update
                        </button>

                        <button type="button"
                                class="delete-btn"
                                data-email="<%= email%>"
                                onclick="openDeleteFromBtn(this)">
                            Delete
                        </button>
                    </div>
                    <% } else { %>
                    <span class="muted">View Only</span>
                    <% } %>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="4" class="no-record">No records available.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</div>


