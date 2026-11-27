<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    if (session == null || session.getAttribute("email") == null) {
        response.sendRedirect(request.getContextPath() + "/ErrorPages/error_session.jsp");
        return;
    }

    String role = (String) session.getAttribute("role");
    boolean isAdmin = role != null && role.equalsIgnoreCase("ADMIN");

    if (session.getAttribute("userList") == null) {
        response.sendRedirect(request.getContextPath() + "/CrudServlet?action=viewresults");
        return;
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Dashboard</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/successStyles.css">
    </head>
    <body>

        <header>
            <jsp:include page="<%= (String) application.getAttribute("header")%>" />
        </header>

        <main>

            <div class="user-container">
                <h1>Welcome</h1>
                <p class="credentials">Username: ${sessionScope.email}</p>
                <p class="credentials">Role: ${sessionScope.role}</p>

                <form action="${pageContext.request.contextPath}/ReportServlet" method="GET">
                    <button type="submit" class="downloadbtn">Download Report</button>
                </form>

                <form action="${pageContext.request.contextPath}/logout" method="POST">
                    <input type="submit" value="Logout" class="logoutbtn">
                </form>
            </div>

            <%
                String message = (String) session.getAttribute("message");
                if (message != null) {
            %>
            <div class="toast"><%= message%></div>
            <%
                    session.removeAttribute("message");
                }
            %>

            <%
                String errorMessage = (String) session.getAttribute("errorMessage");
                if (errorMessage != null) {
            %>
            <div class="toast error-toast"><%= errorMessage%></div>
            <%
                    session.removeAttribute("errorMessage");
                }
            %>

            <% if (isAdmin) { %>
            <div class="create-card">
                <form class="create-form"
                      action="${pageContext.request.contextPath}/CrudServlet"
                      method="POST">

                    <input type="hidden" name="action" value="create">

                    <div class="form-row">
                        <label>Username</label>
                        <input id="createEmail"
                               type="email"
                               name="email"
                               placeholder="Enter username"
                               required>
                    </div>

                    <div class="form-row">
                        <label>Password</label>
                        <input id="createPassword"
                               type="text"
                               name="password"
                               placeholder="Enter password"
                               required>
                    </div>

                    <div class="form-row">
                        <label>User Role</label>
                        <select id="createRole" name="userrole" required>
                            <option value="">Select role</option>
                            <option value="ADMIN">Admin</option>
                            <option value="GUEST">Guest</option>
                        </select>
                    </div>

                    <button id="createBtn"
                            type="submit"
                            class="create-btn disabled-btn"
                            disabled>
                        + Add User
                    </button>

                </form>
            </div>
            <% }%>

            <div class="db-container">
                <jsp:include page="displayTable.jsp" />
            </div>

        </main>

        <footer>
            <jsp:include page="<%= (String) application.getAttribute("footer")%>" />
        </footer>

        <% if (isAdmin) { %>
        <div id="updateModal" class="modal-backdrop" style="display:none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Update User</h2>
                    <button type="button" class="x-btn" onclick="closeModal('updateModal')">×</button>
                </div>

                <form action="${pageContext.request.contextPath}/CrudServlet" method="POST">
                    <input type="hidden" name="action" value="update">

                    <div class="modal-body">
                        <label>Username (Not editable)</label>
                        <input id="updEmail" class="input greyed-out"
                               type="text"
                               name="updateEmail"
                               readonly>

                        <label>New Password</label>
                        <input id="updPass"
                               class="input"
                               type="text"
                               name="updatePass"
                               required>

                        <label>User Role</label>
                        <select id="updRole"
                                class="input"
                                name="updateUserRole"
                                required>
                            <option value="ADMIN">Admin</option>
                            <option value="GUEST">Guest</option>
                        </select>
                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="btn cancel-btn"
                                onclick="closeModal('updateModal')">
                            Cancel
                        </button>

                        <button type="submit"
                                class="btn save-btn">
                            Save
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div id="deleteModal" class="modal-backdrop" style="display:none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Confirm Delete</h2>
                    <button type="button" class="x-btn" onclick="closeModal('deleteModal')">×</button>
                </div>

                <form action="${pageContext.request.contextPath}/CrudServlet" method="POST">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" id="delEmail" name="email">

                    <div class="modal-body">
                        <p class="confirm-text">
                            Are you sure you want to delete this user?<br><br>
                            <b id="delEmailText"></b>
                        </p>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn cancel-btn" onclick="closeModal('deleteModal')">
                            Cancel
                        </button>
                        <button type="submit" class="btn danger-btn">
                            Delete
                        </button>
                    </div>
                </form>
            </div>
        </div>
        <% }%>

        <script>
            function openModal(id) {
                document.getElementById(id).style.display = "flex";
            }

            function closeModal(id) {
                document.getElementById(id).style.display = "none";
            }

            function openUpdateFromBtn(btn) {
                document.getElementById("updEmail").value = btn.dataset.email;
                document.getElementById("updPass").value = btn.dataset.pass;
                document.getElementById("updRole").value = btn.dataset.role.toUpperCase();
                openModal("updateModal");
            }

            function openDeleteFromBtn(btn) {
                var email = btn.dataset.email;
                document.getElementById("delEmail").value = email;
                document.getElementById("delEmailText").textContent = email;
                openModal("deleteModal");
            }

            var createEmail = document.getElementById("createEmail");
            var createPassword = document.getElementById("createPassword");
            var createRole = document.getElementById("createRole");
            var createBtn = document.getElementById("createBtn");

            function validateCreateForm() {
                if (createEmail == null || createPassword == null || createRole == null || createBtn == null) {
                    return;
                }

                var emailValue = createEmail.value.trim();
                var passwordValue = createPassword.value.trim();
                var roleValue = createRole.value;

                var emailValid = emailValue.indexOf("@") > 0;
                var passwordValid = passwordValue.length >= 4;
                var roleValid = roleValue !== "";

                if (emailValid && passwordValid && roleValid) {
                    createBtn.disabled = false;
                    createBtn.classList.remove("disabled-btn");
                } else {
                    createBtn.disabled = true;
                    createBtn.classList.add("disabled-btn");
                }
            }

            if (createEmail != null) {
                createEmail.addEventListener("input", validateCreateForm);
                createPassword.addEventListener("input", validateCreateForm);
                createRole.addEventListener("change", validateCreateForm);
                validateCreateForm();
            }
        </script>

    </body>
</html>


