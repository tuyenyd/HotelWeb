// src/main/java/com/example/hotel/controller/UserController.java
package com.example.hotel.controller;

import com.example.hotel.dto.PasswordChangeRequest;
import com.example.hotel.dto.ProfileUpdateRequest;
import com.example.hotel.entity.User;
import com.example.hotel.repository.UserRepository;
import com.example.hotel.security.UserDetailsImpl;
import com.example.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder; // Dùng để mã hóa mật khẩu

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Luôn mã hóa mật khẩu trước khi lưu
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User savedUser = userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = optionalUser.get();
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setRole(userDetails.getRole());
        existingUser.setIsActive(userDetails.getIsActive());

        // Chỉ cập nhật mật khẩu nếu nó được cung cấp (không rỗng)
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Không cho phép đổi username
        // existingUser.setUsername(userDetails.getUsername());

        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Lỗi: Người dùng chưa xác thực.");
        }
        try {
            User user = userService.getProfile(userDetails.getUsername());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật thông tin hồ sơ
     * Dùng @ModelAttribute để nhận cả DTO và file
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @ModelAttribute ProfileUpdateRequest updateRequest,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Lỗi: Người dùng chưa xác thực.");
        }

        try {
            User updatedUser = userService.updateProfile(userDetails.getUsername(), updateRequest, avatarFile);
            return ResponseEntity.ok(updatedUser); // Trả về user đã cập nhật
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật hồ sơ: " + e.getMessage());
        }
    }

    /**
     * API: Thay đổi mật khẩu
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PasswordChangeRequest passwordRequest) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Lỗi: Người dùng chưa xác thực.");
        }

        try {
            userService.changePassword(
                    userDetails.getUsername(),
                    passwordRequest.getCurrentPassword(),
                    passwordRequest.getNewPassword()
            );
            return ResponseEntity.ok("Đổi mật khẩu thành công!");
        } catch (RuntimeException e) {
            // Trả về 400 (Bad Request) nếu mật khẩu cũ sai
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
        }
    }
}