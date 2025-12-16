package com.example.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AmenityResponseDTO {

    private Long id;
    private String name;
    // Dùng 'type' để khớp với Frontend Admin
    private String type;
    private String description;
    private String iconClass;
    private BigDecimal price;
    private Boolean isChargeable;
    private LocalDateTime createdAt;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Constructors, Getters and Setters (Đã bỏ qua)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getIsChargeable() { return isChargeable; }
    public void setIsChargeable(Boolean isChargeable) { this.isChargeable = isChargeable; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}