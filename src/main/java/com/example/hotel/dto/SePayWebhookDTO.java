package com.example.hotel.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SePayWebhookDTO {
    @JsonProperty("id") // ID giao dịch của SePay
    private Long id;

    @JsonProperty("gateway") // Ví dụ: "MBBank"
    private String gateway;

    @JsonProperty("transactionDate") // Thời gian giao dịch
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    @JsonProperty("accountNumber") // Số tài khoản nhận
    private String accountNumber;

    @JsonProperty("content") // Nội dung chuyển khoản (QUAN TRỌNG)
    private String content;

    @JsonProperty("transferType") // Loại giao dịch (ví dụ: "in")
    private String transferType;

    // ===> SỬA QUAN TRỌNG Ở ĐÂY <===
    @JsonProperty("transferAmount") // Số tiền giao dịch
    private BigDecimal amount; // Tên biến Java có thể giữ nguyên là 'amount' cho gọn

    @JsonProperty("referenceCode") // Mã tham chiếu ngân hàng (FT...)
    private String referenceCode;

    @JsonProperty("accumulated") // Số dư lũy kế (nếu có)
    private BigDecimal accumulated;

    @JsonProperty("description") // Mô tả đầy đủ
    private String description;

    // Các trường này trong JSON mẫu bạn gửi là null, có thể giữ hoặc bỏ
    @JsonProperty("subAccount")
    private String subAccount;
    @JsonProperty("code")
    private String code;
}