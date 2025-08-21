package model;

import java.sql.Timestamp;

public class User {

    private int id;
    private String name;
    private String email;
    private String password;

    // New fields
    private String role;                      // e.g., "user" or "admin"
    private String mobile;
    private String bio;

    private boolean isVerified;              // email verified status
    private String emailVerificationToken;
    private Timestamp emailVerificationTokenExpires;

    private String resetToken;               // for forgot password
    private Timestamp resetTokenExpires;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor without ID (used during registration)
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "user"; // default role
    }

    // Full constructor with all fields (e.g., for DAO mapping)
    public User(int id, String name, String email, String password, String role, String mobile, String bio,
                boolean isVerified, String emailVerificationToken, Timestamp emailVerificationTokenExpires,
                String resetToken, Timestamp resetTokenExpires,
                Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.mobile = mobile;
        this.bio = bio;
        this.isVerified = isVerified;
        this.emailVerificationToken = emailVerificationToken;
        this.emailVerificationTokenExpires = emailVerificationTokenExpires;
        this.resetToken = resetToken;
        this.resetTokenExpires = resetTokenExpires;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === Getters and Setters ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public Timestamp getEmailVerificationTokenExpires() {
        return emailVerificationTokenExpires;
    }
    public void setEmailVerificationTokenExpires(Timestamp emailVerificationTokenExpires) {
        this.emailVerificationTokenExpires = emailVerificationTokenExpires;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Timestamp getResetTokenExpires() { return resetTokenExpires; }
    public void setResetTokenExpires(Timestamp resetTokenExpires) {
        this.resetTokenExpires = resetTokenExpires;
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
