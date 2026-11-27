package controller;

public class User {
    private final String email;
    private final String password;
    private final String userrole;

    public User(String email, String password, String userrole) {
        this.email = email;
        this.password = password;
        this.userrole = userrole;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserRole() {
        return userrole;
    }
}