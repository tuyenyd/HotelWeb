package com.example.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Thêm (exclude = {SecurityAutoConfiguration.class}) để tắt bảo mật
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

}
