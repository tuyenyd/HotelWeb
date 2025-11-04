// src/main/java/com/example/hotel/security/MvcConfig.java
package com.example.hotel.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục "user-avatars"
        Path avatarUploadDir = Paths.get("./user-avatars");
        String avatarUploadPath = avatarUploadDir.toFile().getAbsolutePath();

        // Cấu hình: Nếu ai đó request /avatars/ten-file.jpg
        // Hãy tìm file đó trong thư mục user-avatars
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:/" + avatarUploadPath + "/");
    }
}