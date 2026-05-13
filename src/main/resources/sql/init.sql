CREATE DATABASE IF NOT EXISTS venve DEFAULT CHARACTER SET utf8mb4;

USE venve;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    description TEXT,
    business_hours VARCHAR(50) DEFAULT '09:00-21:00',
    table_count INT DEFAULT 1,
    price_per_hour DECIMAL(10,2) DEFAULT 30.00,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_venue_date_time (venue_id, booking_date, start_time, end_time),
    INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(30) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
);

INSERT INTO venue (name, location, description, business_hours, table_count, price_per_hour, status) VALUES
('1号台', 'A区', '专业级乒乓球台，配备灯光与防滑地板', '09:00-21:00', 2, 30.00, 'AVAILABLE'),
('2号台', 'A区', '靠近休息区，适合新手练习', '09:00-21:00', 1, 25.00, 'AVAILABLE'),
('3号台', 'B区', '高级比赛台，含球拍租借与计分器', '10:00-22:00', 3, 50.00, 'AVAILABLE'),
('4号台', 'B区', '安静独立空间，适合私教课程', '09:00-21:00', 1, 40.00, 'AVAILABLE'),
('5号台', 'C区', '维护中，预计下周恢复', '09:00-21:00', 2, 35.00, 'MAINTENANCE');
