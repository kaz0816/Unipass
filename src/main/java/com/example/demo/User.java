package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ログインIDとして使う
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // ハッシュ化したパスワードを保存

    @Column(nullable = false)
    private String role;      // "ROLE_STUDENT" など

    private String university; // 任意：大学名

    public User() {
    }

    public User(String email, String password, String role, String university) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.university = university;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getUniversity() { return university; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setUniversity(String university) { this.university = university; }
}
