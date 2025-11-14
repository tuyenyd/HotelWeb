package com.example.hotel.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {
    private String name;
    private String email;
    private String username;
    private String password;
    private String fullName;

}
