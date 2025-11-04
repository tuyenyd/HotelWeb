// src/main/java/com/example/hotel/entity/User.java
package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(length = 1000) // Cho phép mô tả dài
    private String about;

    private String company;

    private String job;

    private String country;

    private String address;

    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl; // Đường dẫn tới ảnh

    // Mạng xã hội (nullable)
    private String twitter;
    private String facebook;
    private String instagram;
    private String linkedin;

    // Constructors
    public User() {
    }

    public User(String username, String fullName, String email, String password, String role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        //this.isActive = true;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {

        this.isActive = active;
    }

}