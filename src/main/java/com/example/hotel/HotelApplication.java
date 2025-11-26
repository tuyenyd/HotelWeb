package com.example.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication
@EnableCaching
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

}
