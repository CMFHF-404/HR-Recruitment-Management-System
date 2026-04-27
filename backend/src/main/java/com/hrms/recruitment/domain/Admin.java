package com.hrms.recruitment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AdminRole role = AdminRole.HR;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Admin() {
    }

    public Admin(String username, String password, String name) {
        this(username, password, name, AdminRole.HR);
    }

    public Admin(String username, String password, String name, AdminRole role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AdminRole getRole() { return role == null ? AdminRole.HR : role; }
    public void setRole(AdminRole role) { this.role = role; }
    public boolean hasStoredRole() { return role != null; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
