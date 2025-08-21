package model;

public class LoginResponse {
    private int userId;
    private String email;
    private String token;

    public LoginResponse(int userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    // Getters & Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
