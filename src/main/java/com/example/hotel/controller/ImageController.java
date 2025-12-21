package com.example.hotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/images")
public class ImageController {

    private static final String IMAGE_DIR =
            "src/main/resources/static/Hotel/HotelAdmin/assets/img";

    @GetMapping
    public List<String> getImages() {
        File folder = new File(IMAGE_DIR);

        if (!folder.exists() || !folder.isDirectory()) {
            return List.of();
        }

        return Arrays.stream(folder.listFiles())
                .filter(file ->
                        file.isFile() &&
                                (file.getName().endsWith(".jpg")
                                        || file.getName().endsWith(".png")
                                        || file.getName().endsWith(".jpeg")
                                        || file.getName().endsWith(".webp")))
                .map(file ->
                        "/Hotel/HotelAdmin/assets/img/" + file.getName()
                )
                .toList();
    }
}
