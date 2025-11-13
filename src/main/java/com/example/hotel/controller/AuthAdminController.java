package com.example.hotel.controller;


import com.example.hotel.dto.JwtResponse;
import com.example.hotel.dto.LoginRequest;
import com.example.hotel.dto.RegisterRequest;
import com.example.hotel.entity.User;
import com.example.hotel.repository.UserRepository;
import com.example.hotel.security.jwt.JwtUtils;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthAdminController {

    @Autowired
    @Lazy
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    /*
    @Autowired
    private PasswordEncoder passwordEncoder;*/

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Lấy thông tin người dùng từ database
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy người dùng sau khi đăng nhập."));

        // Trả về JwtResponse mới đã chứa thông tin user
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getAvatarUrl()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã được sử dụng!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Lỗi: Email đã được sử dụng!");
        }

        // Tạo một đối tượng User mới với fullName
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword()),
                "ADMIN"
        );
        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký người dùng thành công!");
    }
}