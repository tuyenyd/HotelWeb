package com.example.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Sử dụng @Controller, KHÔNG phải @RestController
public class ViewController {

    /**
     * Trang đăng nhập Admin
     * URL truy cập: http://localhost:8080/Hotel/HotelAdmin/login
     * File view thực tế: resources/templates/Hotel/HotelAdmin/pages-login.html
     */
    @GetMapping("/Hotel/HotelAdmin/login")
    public String adminLoginPage() {
        // Trả về đường dẫn logic tới file view (không cần đuôi .html nếu dùng Thymeleaf)
        // Spring Boot sẽ tự động tìm file trong thư mục 'templates'
        return "Hotel/HotelAdmin/pages-login";
    }

    /**
     * Trang chủ Admin (Dashboard)
     * URL truy cập: http://localhost:8080/Hotel/HotelAdmin/dashboard
     */
    @GetMapping("/Hotel/HotelAdmin/dashboard")
    public String adminDashboardPage() {
        // Trả về file view: resources/templates/Hotel/HotelAdmin/index.html
        return "Hotel/HotelAdmin/index";
    }

    /**
     * Trang danh sách phòng Admin
     * URL truy cập: http://localhost:8080/Hotel/HotelAdmin/rooms
     */
    @GetMapping("/Hotel/HotelAdmin/rooms")
    public String adminRoomsPage() {
        // Trả về file view: resources/templates/Hotel/HotelAdmin/danhsachphong.html
        return "Hotel/HotelAdmin/danhsachphong";
    }

    // --- Tương tự cho các trang khác ---

    // Ví dụ trang chủ khách hàng
    @GetMapping("/")
    public String homePage() {
        return "Hotel/HotelUser/index";
    }
}