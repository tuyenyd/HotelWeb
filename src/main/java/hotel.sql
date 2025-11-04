-- ===========================================
-- TẠO DATABASE & CHỌN DATABASE
-- ===========================================
CREATE DATABASE IF NOT EXISTS hotel;
USE hotel;

-- ===========================================
-- BẢNG LOẠI PHÒNG (room_types)
-- ===========================================
CREATE TABLE room_types (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            type_code VARCHAR(50) UNIQUE NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            base_price DECIMAL(10,2) NOT NULL,
                            capacity INT NOT NULL,
                            size INT,
                            amenities JSON,
                            points_earned INT NOT NULL DEFAULT 0 COMMENT 'Điểm cố định nhận được khi check-out',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ===========================================
-- BẢNG PHÒNG (rooms)
-- ===========================================
CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       room_number VARCHAR(10) UNIQUE NOT NULL,
                       room_type_id BIGINT NOT NULL,
                       floor INT NOT NULL,
                       status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'CLEANING', 'RESERVED') DEFAULT 'AVAILABLE',
                       price DECIMAL(10,2) NOT NULL,
                       amenities JSON,
                       description TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- ===========================================
-- BẢNG HẠNG THÀNH VIÊN (loyalty_tiers)
-- ===========================================
CREATE TABLE loyalty_tiers (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên hạng: Bronze, Silver, Gold, Platinum',
                               points_required INT NOT NULL DEFAULT 0 COMMENT 'Điểm tối thiểu để đạt hạng này',
                               description TEXT,
                               benefits_json JSON COMMENT 'Lưu danh sách quyền lợi (VD: ["Giảm giá 5%"])',
                               INDEX (points_required)
);

-- ===========================================
-- BẢNG KHÁCH HÀNG (customers)
-- ===========================================
CREATE TABLE customers (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           full_name VARCHAR(150) NOT NULL,
                           id_number VARCHAR(20) NOT NULL UNIQUE COMMENT 'CCCD/CMND',
                           email VARCHAR(255) UNIQUE NOT NULL,
                           date_of_birth DATE NOT NULL,
                           phone VARCHAR(20),
                           address VARCHAR(255),
                           current_points INT NOT NULL DEFAULT 0 COMMENT 'Tổng điểm tích lũy hiện tại',
                           loyalty_tier_id BIGINT NOT NULL DEFAULT 1 COMMENT 'ID của hạng thành viên (FK)',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (loyalty_tier_id) REFERENCES loyalty_tiers(id)
);

-- ===========================================
-- BẢNG ĐẶT PHÒNG (bookings)
-- ===========================================
CREATE TABLE bookings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          booking_code VARCHAR(20) NOT NULL UNIQUE COMMENT 'Mã đặt phòng, ví dụ: BK-2457',
                          customer_id BIGINT NOT NULL,
                          room_id BIGINT NOT NULL,
                          customer_full_name VARCHAR(100) NOT NULL,
                          customer_phone VARCHAR(20) NOT NULL,
                          check_in_date DATE NOT NULL,
                          check_out_date DATE NOT NULL,
                          price_per_night DECIMAL(10, 2) NOT NULL,
                          total_price DECIMAL(12, 2) NOT NULL,
                          status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                          created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          so_nguoi_lon INT NOT NULL DEFAULT 1,
                          so_tre_em INT NOT NULL DEFAULT 0,
                          FOREIGN KEY (customer_id) REFERENCES customers(id),
                          FOREIGN KEY (room_id) REFERENCES rooms(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- ===========================================
-- BẢNG LỊCH SỬ TÍCH ĐIỂM (loyalty_point_transactions)
-- ===========================================
CREATE TABLE loyalty_point_transactions (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            customer_id BIGINT NOT NULL,
                                            booking_id BIGINT NULL COMMENT 'Liên kết với đặt phòng (nếu có)',
                                            points_earned INT NOT NULL COMMENT 'Điểm nhận được (có thể âm nếu đổi quà)',
                                            description VARCHAR(255),
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (customer_id) REFERENCES customers(id),
                                            FOREIGN KEY (booking_id) REFERENCES bookings(id),
                                            INDEX (customer_id)
);

-- ===========================================
-- BẢNG THANH TOÁN (payments)
-- ===========================================
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          booking_id BIGINT NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          payment_method ENUM('CASH', 'CREDIT_CARD', 'BANK_TRANSFER', 'ONLINE') DEFAULT 'CASH',
                          payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
                          payment_date TIMESTAMP NULL,
                          transaction_id VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- ===========================================
-- BẢNG NGƯỜI DÙNG HỆ THỐNG (users)
-- ===========================================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(200) NOT NULL,
                       role ENUM('ADMIN', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPING') DEFAULT 'RECEPTIONIST',
                       is_active BOOLEAN DEFAULT TRUE,
                       last_login TIMESTAMP NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ===========================================
-- DỮ LIỆU MẪU
-- ===========================================

-- 1. Dữ liệu các hạng thành viên
INSERT INTO loyalty_tiers (id, name, points_required, description, benefits_json) VALUES
                                                                                      (1, 'Bronze', 0, 'Thành viên mới', '["Tích điểm 1x", "WiFi miễn phí"]'),
                                                                                      (2, 'Silver', 500, 'Thành viên thân thiết', '["Giảm giá 5% F&B", "Check-in sớm"]'),
                                                                                      (3, 'Gold', 1500, 'Thành viên VIP', '["Giảm giá 10% F&B", "Nâng hạng phòng miễn phí (nếu có)", "Bữa sáng miễn phí"]'),
                                                                                      (4, 'Platinum', 3000, 'Thành viên cao cấp', '["Giảm giá 15% F&B", "Nâng hạng phòng miễn phí (luôn có)", "Phòng chờ VIP"]');

-- 2. Loại phòng
INSERT INTO room_types (type_code, name, description, base_price, capacity, size, amenities, points_earned) VALUES
                                                                                                                ('standard', 'Phòng Standard', 'Phòng tiêu chuẩn 1 giường lớn', 850000, 2, 25, '["wifi", "ac", "tv", "hairdryer"]', 100),
                                                                                                                ('twin', 'Phòng Twin', 'Phòng tiêu chuẩn 2 giường đơn', 900000, 2, 28, '["wifi", "ac", "tv", "hairdryer"]', 100),
                                                                                                                ('deluxe', 'Phòng Deluxe', 'Phòng cao cấp view đẹp', 1200000, 3, 35, '["wifi", "ac", "tv", "minibar", "safe", "hairdryer"]', 150),
                                                                                                                ('family', 'Phòng Family', 'Phòng gia đình 2 giường lớn', 1800000, 5, 50, '["wifi", "ac", "tv", "minibar", "safe"]', 200),
                                                                                                                ('suite', 'Phòng Suite', 'Phòng hạng sang có phòng khách', 2500000, 4, 60, '["wifi", "ac", "tv", "minibar", "safe", "jacuzzi"]', 300);

-- 3. Phòng
INSERT INTO rooms (room_number, room_type_id, floor, status, price, amenities, description) VALUES
                                                                                                ('101', 1, 1, 'AVAILABLE', 850000, '["wifi", "ac", "tv"]', 'Standard view thành phố'),
                                                                                                ('102', 1, 1, 'AVAILABLE', 850000, '["wifi", "ac", "tv"]', 'Standard view thành phố'),
                                                                                                ('103', 1, 1, 'AVAILABLE', 850000, '["wifi", "ac", "tv"]', 'Standard view sân vườn'),
                                                                                                ('104', 2, 1, 'AVAILABLE', 900000, '["wifi", "ac", "tv"]', 'Twin view sân vườn'),
                                                                                                ('105', 2, 1, 'AVAILABLE', 900000, '["wifi", "ac", "tv"]', 'Twin view thành phố'),
                                                                                                ('201', 3, 2, 'AVAILABLE', 1200000, '["wifi", "ac", "tv", "minibar"]', 'Deluxe view biển'),
                                                                                                ('202', 3, 2, 'AVAILABLE', 1200000, '["wifi", "ac", "tv", "minibar"]', 'Deluxe view biển'),
                                                                                                ('203', 3, 2, 'MAINTENANCE', 1200000, '["wifi", "ac", "tv", "minibar"]', 'Deluxe view biển - đang bảo trì'),
                                                                                                ('204', 3, 2, 'AVAILABLE', 1200000, '["wifi", "ac", "tv", "minibar"]', 'Deluxe view biển'),
                                                                                                ('301', 4, 3, 'AVAILABLE', 1800000, '["wifi", "ac", "tv", "minibar"]', 'Family view hồ bơi'),
                                                                                                ('302', 4, 3, 'AVAILABLE', 1800000, '["wifi", "ac", "tv", "minibar"]', 'Family view hồ bơi'),
                                                                                                ('401', 5, 4, 'AVAILABLE', 2500000, '["wifi", "ac", "tv", "minibar", "jacuzzi"]', 'Suite Tổng thống'),
                                                                                                ('402', 5, 4, 'AVAILABLE', 2500000, '["wifi", "ac", "tv", "minibar", "jacuzzi"]', 'Suite Hoàng gia');

-- 4. Khách hàng (Đã cập nhật tên)
INSERT INTO customers (full_name, id_number, email, date_of_birth, phone, address) VALUES
                                                                                       ('Nguyễn Văn Anh', '011111111', 'nguyenvananh@email.com', '1985-06-15', '0912345678', '123 Đường ABC, Quận 1, TP.HCM'),
                                                                                       ('Trần Thị Bình', '022222222', 'tranthibinh@email.com', '1990-02-20', '0923456789', '456 Đường XYZ, Quận 2, TP.HN'),
                                                                                       ('Lê Văn Cường', '033333333', 'levancuong@email.com', '1995-11-30', '0932345678', '789 Đường LMN, Quận 3, TP.DN'),
                                                                                       ('Phạm Thị Dung', '044444444', 'phamthidung@email.com', '1988-08-10', '0942345678', '101 Đường QRS, Cần Thơ'),
                                                                                       ('Hoàng Văn Hùng', '055555555', 'hoangvanhung@email.com', '2000-01-05', '0952345678', '202 Đường TUV, Hải Phòng'),
                                                                                       ('Ngô Thị Lan', '066666666', 'ngothilan@email.com', '1992-07-25', '0962345678', '303 Đường Hùng Vương, TP. Hà Nội'),
                                                                                       ('Phan Văn Dũng', '077777777', 'phanvandung@email.com', '1983-03-12', '0972345678', '404 Đường Lê Lợi, TP. Đà Nẵng'),
                                                                                       ('Đặng Thị Nga', '088888888', 'dangthinga@email.com', '1998-12-01', '0982345678', '505 Đường GHK, Bình Dương'),
                                                                                       ('Vũ Văn Giang', '099999999', 'vuvangiang@email.com', '1991-04-14', '0911111111', '111 Đường JKL, Vũng Tàu'),
                                                                                       ('Hồ Thị Hà', '101010101', 'hothiha@email.com', '1993-09-09', '0922222222', '222 Đường MNO, Nha Trang'),
                                                                                       ('Võ Minh Tuấn', '121212121', 'vominh.tuan@email.com', '1989-05-20', '0933333333', '333 Đường Nguyễn Huệ, TP.HCM'),
                                                                                       ('Đoàn Thanh Mai', '131313131', 'doanthanhmai@email.com', '1994-10-27', '0944444444', '444 Đường Trần Hưng Đạo, TP. Cần Thơ'),
                                                                                       ('Trần Văn Kiên', '141414141', 'tranvankien@email.com', '1979-11-11', '0955555555', '555 Đường PQR, Đà Lạt'),
                                                                                       ('Lý Thị Lệ', '151515151', 'lythile@email.com', '1982-08-08', '0966666666', '666 Đường STU, Huế'),
                                                                                       ('Bùi Văn Minh', '161616161', 'buivanminh@email.com', '2001-07-07', '0977777777', '777 Đường VWX, Quy Nhơn');

-- 5. Người dùng
INSERT INTO users (username, email, password, full_name, role) VALUES
                                                                   ('admin', 'admin@hotel.com', 'xyz123', 'Quản Trị Viên', 'ADMIN'),
                                                                   ('manager', 'manager@hotel.com', 'xyz456', 'Lê Minh', 'MANAGER'),
                                                                   ('reception', 'reception@hotel.com', 'xyz789', 'Ngọc Trinh', 'RECEPTIONIST');

-- 6. Đặt phòng (Đã cập nhật tên khách hàng)
INSERT INTO bookings (
    booking_code, customer_id, room_id, customer_full_name, customer_phone,
    check_in_date, check_out_date, so_nguoi_lon, so_tre_em,
    price_per_night, total_price, status, created_date
) VALUES
-- THÁNG 1 (Tết Âm lịch - Cao điểm)
('BK-24001', 1, 1, 'Nguyễn Văn Anh', '0912345678', '2025-01-20', '2025-01-22', 2, 0, 850000, 1700000, 'CHECKED_OUT', '2025-01-10 10:00:00'),
('BK-24002', 2, 6, 'Trần Thị Bình', '0923456789', '2025-01-22', '2025-01-25', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-01-15 11:00:00'),
('BK-24003', 3, 11, 'Lê Văn Cường', '0932345678', '2025-01-23', '2025-01-25', 4, 0, 1800000, 3600000, 'CHECKED_OUT', '2025-01-18 09:00:00'),
('BK-24004', 4, 12, 'Phạm Thị Dung', '0942345678', '2025-01-24', '2025-01-28', 2, 0, 2500000, 10000000, 'CHECKED_OUT', '2025-01-20 14:00:00'),
('BK-24005', 5, 4, 'Hoàng Văn Hùng', '0952345678', '2025-01-25', '2025-01-27', 2, 0, 900000, 1800000, 'CHECKED_OUT', '2025-01-22 16:00:00'),
-- THÁNG 2
('BK-24006', 6, 7, 'Ngô Thị Lan', '0962345678', '2025-02-10', '2025-02-14', 2, 0, 1200000, 4800000, 'CHECKED_OUT', '2025-01-15 08:00:00'),
('BK-24007', 7, 10, 'Phan Văn Dũng', '0972345678', '2025-02-11', '2025-02-15', 4, 0, 1800000, 7200000, 'CHECKED_OUT', '2025-01-20 17:00:00'),
('BK-24008', 8, 13, 'Đặng Thị Nga', '0982345678', '2025-02-12', '2025-02-16', 3, 0, 2500000, 10000000, 'CHECKED_OUT', '2025-01-25 12:00:00'),
('BK-24009', 9, 2, 'Vũ Văn Giang', '0911111111', '2025-02-13', '2025-02-15', 2, 0, 850000, 1700000, 'CHECKED_OUT', '2025-02-01 10:00:00'),
('BK-24010', 10, 5, 'Hồ Thị Hà', '0922222222', '2025-02-14', '2025-02-17', 2, 0, 900000, 2700000, 'CHECKED_OUT', '2025-02-05 11:00:00'),
-- THÁNG 3
('BK-24011', 11, 1, 'Võ Minh Tuấn', '0933333333', '2025-03-05', '2025-03-07', 2, 0, 850000, 1700000, 'CHECKED_OUT', '2025-03-01 09:00:00'),
('BK-24012', 12, 6, 'Đoàn Thanh Mai', '0944444444', '2025-03-10', '2025-03-13', 3, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-03-05 14:00:00'),
('BK-24013', 13, 4, 'Trần Văn Kiên', '0955555555', '2025-03-15', '2025-03-17', 2, 0, 900000, 1800000, 'CANCELLED', '2025-03-10 16:00:00'),
-- THÁNG 4
('BK-24014', 14, 10, 'Lý Thị Lệ', '0966666666', '2025-04-01', '2025-04-05', 4, 0, 1800000, 7200000, 'CHECKED_OUT', '2025-03-20 08:00:00'),
('BK-24015', 15, 2, 'Bùi Văn Minh', '0977777777', '2025-04-10', '2025-04-12', 1, 0, 850000, 1700000, 'CANCELLED', '2025-04-05 17:00:00'),
('BK-24016', 1, 12, 'Nguyễn Văn Anh', '0912345678', '2025-04-28', '2025-04-30', 2, 0, 2500000, 5000000, 'CHECKED_OUT', '2025-04-15 12:00:00'),
('BK-24017', 2, 7, 'Trần Thị Bình', '0923456789', '2025-04-29', '2025-05-02', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-04-10 10:00:00'),
-- THÁNG 5
('BK-24018', 3, 11, 'Lê Văn Cường', '0932345678', '2025-04-30', '2025-05-03', 5, 0, 1800000, 5400000, 'CHECKED_OUT', '2025-04-11 11:00:00'),
('BK-24019', 4, 9, 'Phạm Thị Dung', '0942345678', '2025-05-01', '2025-05-03', 2, 0, 1200000, 2400000, 'CHECKED_OUT', '2025-04-12 09:00:00'),
('BK-24020', 5, 1, 'Hoàng Văn Hùng', '0952345678', '2025-05-15', '2025-05-18', 1, 0, 850000, 2550000, 'CHECKED_OUT', '2025-05-10 14:00:00'),
-- THÁNG 6
('BK-24021', 6, 13, 'Ngô Thị Lan', '0962345678', '2025-06-10', '2025-06-15', 2, 0, 2500000, 12500000, 'CHECKED_OUT', '2025-05-15 16:00:00'),
('BK-24022', 7, 10, 'Phan Văn Dũng', '0972345678', '2025-06-12', '2025-06-17', 5, 0, 1800000, 9000000, 'CHECKED_OUT', '2025-05-20 08:00:00'),
('BK-24023', 8, 6, 'Đặng Thị Nga', '0982345678', '2025-06-15', '2025-06-18', 3, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-06-01 17:00:00'),
('BK-24024', 9, 7, 'Vũ Văn Giang', '0911111111', '2025-06-20', '2025-06-23', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-06-10 12:00:00'),
-- THÁNG 7
('BK-24025', 10, 1, 'Hồ Thị Hà', '0922222222', '2025-07-01', '2025-07-05', 2, 0, 850000, 3400000, 'CHECKED_OUT', '2025-06-15 10:00:00'),
('BK-24026', 11, 4, 'Võ Minh Tuấn', '0933333333', '2025-07-02', '2025-07-07', 2, 0, 900000, 4500000, 'CHECKED_OUT', '2025-06-18 11:00:00'),
('BK-24027', 12, 11, 'Đoàn Thanh Mai', '0944444444', '2025-07-05', '2025-07-10', 4, 0, 1800000, 9000000, 'CHECKED_OUT', '2025-06-20 09:00:00'),
('BK-24028', 13, 12, 'Trần Văn Kiên', '0955555555', '2025-07-08', '2025-07-12', 3, 0, 2500000, 10000000, 'CHECKED_OUT', '2025-06-25 14:00:00'),
('BK-24029', 14, 6, 'Lý Thị Lệ', '0966666666', '2025-07-10', '2025-07-13', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-07-01 16:00:00'),
('BK-24030', 15, 7, 'Bùi Văn Minh', '0977777777', '2025-07-15', '2025-07-20', 2, 0, 1200000, 6000000, 'CHECKED_OUT', '2025-07-05 08:00:00'),
('BK-24031', 1, 10, 'Nguyễn Văn Anh', '0912345678', '2025-07-20', '2025-07-25', 4, 0, 1800000, 9000000, 'CHECKED_OUT', '2025-07-10 17:00:00'),
-- THÁNG 8
('BK-24032', 2, 13, 'Trần Thị Bình', '0923456789', '2025-08-01', '2025-08-05', 4, 0, 2500000, 10000000, 'CHECKED_OUT', '2025-07-15 12:00:00'),
('BK-24033', 3, 9, 'Lê Văn Cường', '0932345678', '2025-08-02', '2025-08-06', 2, 0, 1200000, 4800000, 'CHECKED_OUT', '2025-07-20 10:00:00'),
('BK-24034', 4, 1, 'Phạm Thị Dung', '0942345678', '2025-08-05', '2025-08-08', 2, 0, 850000, 2550000, 'CHECKED_OUT', '2025-07-25 11:00:00'),
('BK-24035', 5, 5, 'Hoàng Văn Hùng', '0952345678', '2025-08-10', '2025-08-15', 2, 0, 900000, 4500000, 'CHECKED_OUT', '2025-08-01 09:00:00'),
('BK-24036', 6, 11, 'Ngô Thị Lan', '0962345678', '2025-08-12', '2025-08-18', 5, 0, 1800000, 10800000, 'CHECKED_OUT', '2025-08-05 14:00:00'),
('BK-24037', 7, 12, 'Phan Văn Dũng', '0972345678', '2025-08-20', '2025-08-25', 2, 0, 2500000, 12500000, 'CHECKED_OUT', '2025-08-10 16:00:00'),
('BK-24038', 8, 2, 'Đặng Thị Nga', '0982345678', '2025-08-25', '2025-08-28', 1, 0, 850000, 2550000, 'CHECKED_OUT', '2025-08-20 08:00:00'),
-- THÁNG 9
('BK-24039', 9, 6, 'Vũ Văn Giang', '0911111111', '2025-09-05', '2025-09-08', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-08-25 17:00:00'),
('BK-24040', 10, 1, 'Hồ Thị Hà', '0922222222', '2025-09-10', '2025-09-12', 1, 0, 850000, 1700000, 'CHECKED_OUT', '2025-09-01 12:00:00'),
('BK-24041', 11, 4, 'Võ Minh Tuấn', '0933333333', '2025-09-15', '2025-09-18', 2, 0, 900000, 2700000, 'CANCELLED', '2025-09-10 10:00:00'),
-- THÁNG 10
('BK-24042', 12, 10, 'Đoàn Thanh Mai', '0944444444', '2025-10-05', '2025-10-10', 3, 0, 1800000, 9000000, 'CHECKED_OUT', '2025-09-20 11:00:00'),
('BK-24043', 13, 7, 'Trần Văn Kiên', '0955555555', '2025-10-15', '2025-10-17', 2, 0, 1200000, 2400000, 'CHECKED_OUT', '2025-10-01 09:00:00'),
('BK-24044', 14, 2, 'Lý Thị Lệ', '0966666666', '2025-10-20', '2025-10-22', 1, 0, 850000, 1700000, 'CANCELLED', '2025-10-15 14:00:00'),
-- THÁNG 11
('BK-24045', 15, 12, 'Bùi Văn Minh', '0977777777', '2025-11-05', '2025-11-08', 2, 0, 2500000, 7500000, 'CHECKED_OUT', '2025-10-20 16:00:00'),
('BK-24046', 1, 5, 'Nguyễn Văn Anh', '0912345678', '2025-11-15', '2025-11-17', 2, 0, 900000, 1800000, 'CHECKED_OUT', '2025-11-01 08:00:00'),
('BK-24047', 2, 9, 'Trần Thị Bình', '0923456789', '2025-11-20', '2025-11-23', 2, 0, 1200000, 3600000, 'CHECKED_OUT', '2025-11-10 17:00:00'),
-- THÁNG 12
('BK-24048', 3, 13, 'Lê Văn Cường', '0932345678', '2025-12-20', '2025-12-23', 3, 0, 2500000, 7500000, 'CONFIRMED', '2025-11-20 12:00:00'),
('BK-24049', 4, 11, 'Phạm Thị Dung', '0942345678', '2025-12-22', '2025-12-26', 5, 0, 1800000, 7200000, 'CONFIRMED', '2025-11-25 10:00:00'),
('BK-24050', 6, 12, 'Ngô Thị Lan', '0962345678', '2025-12-24', '2025-12-27', 2, 0, 2500000, 7500000, 'CONFIRMED', '2025-12-01 11:00:00'),
('BK-24051', 7, 10, 'Phan Văn Dũng', '0972345678', '2025-12-28', '2026-01-02', 4, 0, 1800000, 9000000, 'CONFIRMED', '2025-01-05 09:00:00'),
('BK-24052', 8, 7, 'Đặng Thị Nga', '0982345678', '2025-12-29', '2026-01-01', 2, 0, 1200000, 2400000, 'PENDING', '2025-12-20 14:00:00'),
('BK-24053', 9, 1, 'Vũ Văn Giang', '0911111111', '2025-12-30', '2026-01-02', 2, 0, 850000, 2550000, 'CONFIRMED', '2025-12-15 16:00:00');

-- 7. Thanh toán
INSERT INTO payments (booking_id, amount, payment_method, payment_status, payment_date, transaction_id) VALUES
                                                                                                            (1, 1700000, 'CASH', 'COMPLETED', '2025-01-22 12:00:00', 'TR-001'),
                                                                                                            (2, 3600000, 'CREDIT_CARD', 'COMPLETED', '2025-01-25 11:00:00', 'TR-002'),
                                                                                                            (3, 3600000, 'BANK_TRANSFER', 'COMPLETED', '2025-01-25 09:00:00', 'TR-003'),
                                                                                                            (4, 10000000, 'ONLINE', 'COMPLETED', '2025-01-20 14:05:00', 'TR-004'),
                                                                                                            (5, 1800000, 'CASH', 'COMPLETED', '2025-01-27 10:00:00', 'TR-005'),
                                                                                                            (6, 4800000, 'CREDIT_CARD', 'COMPLETED', '2025-01-15 08:05:00', 'TR-006'),
                                                                                                            (7, 7200000, 'ONLINE', 'COMPLETED', '2025-01-20 17:05:00', 'TR-007'),
                                                                                                            (8, 10000000, 'CREDIT_CARD', 'COMPLETED', '2025-01-25 12:05:00', 'TR-008'),
                                                                                                            (9, 1700000, 'CASH', 'COMPLETED', '2025-02-15 11:00:00', 'TR-009'),
                                                                                                            (10, 2700000, 'BANK_TRANSFER', 'COMPLETED', '2025-02-17 10:00:00', 'TR-010'),
                                                                                                            (11, 1700000, 'ONLINE', 'COMPLETED', '2025-03-01 09:05:00', 'TR-011'),
                                                                                                            (12, 3600000, 'CREDIT_CARD', 'COMPLETED', '2025-03-05 14:05:00', 'TR-012'),
                                                                                                            (14, 7200000, 'BANK_TRANSFER', 'COMPLETED', '2025-03-20 08:05:00', 'TR-014'),
                                                                                                            (16, 5000000, 'CREDIT_CARD', 'COMPLETED', '2025-04-15 12:05:00', 'TR-016'),
                                                                                                            (17, 3600000, 'ONLINE', 'COMPLETED', '2025-04-10 10:05:00', 'TR-017'),
                                                                                                            (18, 5400000, 'CASH', 'COMPLETED', '2025-05-03 12:00:00', 'TR-018'),
                                                                                                            (19, 2400000, 'CREDIT_CARD', 'COMPLETED', '2025-05-03 10:00:00', 'TR-019'),
                                                                                                            (20, 2550000, 'BANK_TRANSFER', 'COMPLETED', '2025-05-18 09:00:00', 'TR-020');



