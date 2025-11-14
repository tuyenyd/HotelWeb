// src/main/java/com/example/hotel/service/UserService.java
package com.example.hotel.service;

import com.example.hotel.dto.ProfileUpdateRequest;
import com.example.hotel.entity.User;
import com.example.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Đường dẫn thư mục lưu ảnh. (Bạn có thể đổi sang thư mục khác)
    // Cần đảm bảo thư mục "user-avatars" này tồn tại
    private final Path rootAvatarLocation = Paths.get("user-avatars");

    public UserService() {
        try {
            // Tự động tạo thư mục nếu chưa có
            Files.createDirectories(rootAvatarLocation);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ avatar", e);
        }
    }

    /**
     * Lấy thông tin hồ sơ
     */
    public User getProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
    }

    /**
     * Cập nhật hồ sơ
     */
    @Transactional
    public User updateProfile(String username, ProfileUpdateRequest request, MultipartFile avatarFile) throws IOException {
        User user = getProfile(username);

        // Cập nhật các trường text
        user.setFullName(request.getFullName());
        user.setAbout(request.getAbout());
        user.setCompany(request.getCompany());
        user.setJob(request.getJob());
        user.setCountry(request.getCountry());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail()); // Cẩn thận khi cho đổi email (vì nó là unique)
        user.setTwitter(request.getTwitter());
        user.setFacebook(request.getFacebook());
        user.setInstagram(request.getInstagram());
        user.setLinkedin(request.getLinkedin());

        // Xử lý file ảnh nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    Files.deleteIfExists(rootAvatarLocation.resolve(user.getAvatarUrl()));
                } catch (IOException e) {
                    System.err.println("Không thể xóa avatar cũ: " + e.getMessage());
                }
            }

            // Tạo tên file mới duy nhất
            String filename = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
            Files.copy(avatarFile.getInputStream(), this.rootAvatarLocation.resolve(filename));

            // Lưu tên file vào DB.
            // Cần cấu hình Spring để phục vụ file từ thư mục này
            user.setAvatarUrl(filename);
        }

        return userRepository.save(user);
    }

    /**
     * Thay đổi mật khẩu
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getProfile(username);

        // 1. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        // 2. Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}