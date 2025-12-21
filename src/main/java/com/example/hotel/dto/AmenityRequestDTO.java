package com.example.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class AmenityRequestDTO {

    @NotBlank(message = "Tên tiện ích không được để trống")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Loại tiện ích không được để trống")
    private String type;

    private String description;

    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String iconClass;

    private BigDecimal price;

    private Boolean isChargeable;

    // Constructors, Getters and Setters (Đã bỏ qua)
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
}