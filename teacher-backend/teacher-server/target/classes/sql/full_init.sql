-- Full init for new database (self-contained).
-- Execute:
--   mysql -h127.0.0.1 -P3306 -uroot -p < teacher-backend/teacher-server/src/main/resources/sql/full_init.sql

CREATE DATABASE IF NOT EXISTS teacher_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE teacher_service;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS role_menu;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS advertising;
DROP TABLE IF EXISTS slide;
DROP TABLE IF EXISTS teacher_subject;
DROP TABLE IF EXISTS teacher_region;
DROP TABLE IF EXISTS teacher_success_record;
DROP TABLE IF EXISTS dispatch_record;
DROP TABLE IF EXISTS favorite_teacher;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS requirement;
DROP TABLE IF EXISTS dictionary_content;
DROP TABLE IF EXISTS dictionary;
DROP TABLE IF EXISTS teacher_audit;
DROP TABLE IF EXISTS teacher_info;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS region;
DROP TABLE IF EXISTS school;
DROP TABLE IF EXISTS subject_category;
DROP TABLE IF EXISTS subject;

CREATE TABLE `user` (
  id VARCHAR(32) PRIMARY KEY,
  user_account VARCHAR(50) NOT NULL,
  user_password VARCHAR(100) NOT NULL,
  user_wechat_openid VARCHAR(64) NULL,
  user_name VARCHAR(50) NOT NULL,
  user_portrait VARCHAR(255) NULL,
  user_gender TINYINT DEFAULT 0,
  user_email VARCHAR(100) NULL,
  user_phone VARCHAR(20) NOT NULL,
  user_location_address VARCHAR(255) NULL,
  user_location_longitude DECIMAL(10,6) NULL,
  user_location_latitude DECIMAL(10,6) NULL,
  user_region_code VARCHAR(64) NULL,
  user_region_name VARCHAR(64) NULL,
  user_region_province VARCHAR(64) NULL,
  user_region_city VARCHAR(64) NULL,
  user_region_district VARCHAR(64) NULL,
  user_region_source VARCHAR(16) NULL,
  user_region_sync_time DATETIME NULL,
  user_type TINYINT NOT NULL DEFAULT 0,
  user_status TINYINT NOT NULL DEFAULT 1,
  profile_completed TINYINT NOT NULL DEFAULT 0,
  nickname_source VARCHAR(16) NULL,
  avatar_source VARCHAR(16) NULL,
  last_profile_complete_time DATETIME NULL,
  user_delete_status TINYINT NOT NULL DEFAULT 0,
  last_login_time DATETIME NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_user_account (user_account),
  UNIQUE KEY uk_user_wechat_openid (user_wechat_openid),
  UNIQUE KEY uk_user_phone (user_phone),
  KEY idx_user_type (user_type),
  KEY idx_user_type_profile (user_type, profile_completed),
  KEY idx_user_region_code (user_region_code),
  KEY idx_user_status (user_status),
  KEY idx_user_delete_status (user_delete_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher_info (
  id VARCHAR(32) PRIMARY KEY,
  user_id VARCHAR(32) NOT NULL,
  teacher_identity VARCHAR(50) NOT NULL,
  teacher_photo VARCHAR(255) NULL,
  teacher_age INT NULL,
  teacher_teaching_years INT NOT NULL DEFAULT 0,
  teacher_hometown VARCHAR(100) NULL,
  teacher_home_address VARCHAR(255) NULL,
  teacher_work_address VARCHAR(255) NULL,
  teacher_work_longitude DECIMAL(10,6) NULL,
  teacher_work_latitude DECIMAL(10,6) NULL,
  teacher_school VARCHAR(100) NULL,
  teacher_major VARCHAR(100) NULL,
  teacher_education VARCHAR(50) NULL,
  teacher_self_description TEXT NULL,
  teacher_tutoring_method TINYINT NOT NULL DEFAULT 0,
  teacher_experience TEXT NULL,
  teacher_success_count INT NOT NULL DEFAULT 0,
  teacher_view_count INT NOT NULL DEFAULT 0,
  teacher_audit_status TINYINT NOT NULL DEFAULT 0,
  teacher_cert_no VARCHAR(64) NULL,
  teacher_cert_images TEXT NULL,
  teacher_profile_completed TINYINT NOT NULL DEFAULT 0,
  teacher_enable_status TINYINT NOT NULL DEFAULT 1,
  teacher_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_teacher_user_id (user_id),
  KEY idx_teacher_audit_status (teacher_audit_status),
  KEY idx_teacher_delete_status (teacher_delete_status),
  CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher_audit (
  id VARCHAR(32) PRIMARY KEY,
  teacher_id VARCHAR(32) NOT NULL,
  audit_status TINYINT NOT NULL DEFAULT 0,
  audit_reason VARCHAR(500) NULL,
  auditor_id VARCHAR(32) NULL,
  audit_time DATETIME NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_teacher_audit_teacher (teacher_id),
  CONSTRAINT fk_audit_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subject_category (
  id VARCHAR(32) PRIMARY KEY,
  category_name VARCHAR(64) NOT NULL,
  category_code VARCHAR(64) NOT NULL,
  category_sort INT NOT NULL DEFAULT 0,
  category_status TINYINT NOT NULL DEFAULT 1,
  category_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_subject_category_code (category_code),
  KEY idx_subject_category_sort (category_sort),
  KEY idx_subject_category_status (category_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subject (
  id VARCHAR(32) PRIMARY KEY,
  subject_name VARCHAR(50) NOT NULL,
  subject_code VARCHAR(50) NOT NULL,
  subject_category_id VARCHAR(32) NULL,
  subject_category VARCHAR(50) NOT NULL,
  subject_description VARCHAR(255) NULL,
  subject_sort INT NOT NULL DEFAULT 0,
  subject_status TINYINT NOT NULL DEFAULT 1,
  subject_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_subject_code (subject_code),
  KEY idx_subject_category (subject_category),
  KEY idx_subject_category_id (subject_category_id),
  CONSTRAINT fk_subject_category_id FOREIGN KEY (subject_category_id) REFERENCES subject_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE school (
  id VARCHAR(32) PRIMARY KEY,
  school_name VARCHAR(100) NOT NULL,
  school_code VARCHAR(50) NOT NULL,
  school_type TINYINT NOT NULL DEFAULT 0,
  school_province VARCHAR(50) NOT NULL,
  school_city VARCHAR(50) NOT NULL,
  school_district VARCHAR(50) NOT NULL,
  school_address VARCHAR(255) NOT NULL,
  school_longitude DECIMAL(10,6) NULL,
  school_latitude DECIMAL(10,6) NULL,
  school_status TINYINT NOT NULL DEFAULT 1,
  school_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_school_code (school_code),
  KEY idx_school_city (school_city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE region (
  id VARCHAR(32) PRIMARY KEY,
  region_name VARCHAR(50) NOT NULL,
  region_code VARCHAR(50) NOT NULL,
  region_city VARCHAR(50) NOT NULL,
  region_province VARCHAR(50) NOT NULL,
  region_sort INT NOT NULL DEFAULT 0,
  region_status TINYINT NOT NULL DEFAULT 1,
  region_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_region_code (region_code),
  KEY idx_region_city (region_city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE requirement (
  id VARCHAR(32) PRIMARY KEY,
  parent_id VARCHAR(32) NOT NULL,
  requirement_title VARCHAR(200) NOT NULL,
  requirement_description TEXT NOT NULL,
  subject_id VARCHAR(32) NOT NULL,
  requirement_grade VARCHAR(50) NOT NULL,
  region_id VARCHAR(32) NOT NULL,
  requirement_address VARCHAR(255) NOT NULL,
  requirement_longitude DECIMAL(10,6) NULL,
  requirement_latitude DECIMAL(10,6) NULL,
  requirement_tutoring_method TINYINT NOT NULL DEFAULT 0,
  requirement_frequency VARCHAR(50) NULL,
  requirement_salary DECIMAL(10,2) NOT NULL,
  student_gender VARCHAR(16) NULL,
  salary_text VARCHAR(100) NULL,
  cross_street VARCHAR(255) NULL,
  student_detail VARCHAR(1000) NULL,
  teacher_qualification VARCHAR(100) NULL,
  teacher_gender_preference VARCHAR(16) NULL,
  teacher_requirement_text VARCHAR(1000) NULL,
  requirement_other TEXT NULL,
  requirement_images TEXT NULL,
  requirement_status TINYINT NOT NULL DEFAULT 0,
  requirement_audit_status TINYINT NOT NULL DEFAULT 0,
  requirement_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_requirement_parent (parent_id),
  KEY idx_requirement_status (requirement_status),
  KEY idx_requirement_subject (subject_id),
  CONSTRAINT fk_requirement_parent FOREIGN KEY (parent_id) REFERENCES `user` (id),
  CONSTRAINT fk_requirement_subject FOREIGN KEY (subject_id) REFERENCES subject (id),
  CONSTRAINT fk_requirement_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order` (
  id VARCHAR(32) PRIMARY KEY,
  order_number VARCHAR(50) NOT NULL,
  requirement_id VARCHAR(32) NOT NULL,
  parent_id VARCHAR(32) NOT NULL,
  teacher_id VARCHAR(32) NOT NULL,
  order_status TINYINT NOT NULL DEFAULT 0,
  order_amount DECIMAL(10,2) NOT NULL,
  order_start_time DATETIME NULL,
  order_end_time DATETIME NULL,
  order_remark VARCHAR(500) NULL,
  order_audit_status TINYINT NOT NULL DEFAULT 0,
  order_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_order_number (order_number),
  KEY idx_order_requirement (requirement_id),
  KEY idx_order_parent (parent_id),
  KEY idx_order_teacher (teacher_id),
  KEY idx_order_status (order_status),
  CONSTRAINT fk_order_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id),
  CONSTRAINT fk_order_parent FOREIGN KEY (parent_id) REFERENCES `user` (id),
  CONSTRAINT fk_order_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE appointment (
  id VARCHAR(32) PRIMARY KEY,
  parent_id VARCHAR(32) NOT NULL,
  teacher_id VARCHAR(32) NOT NULL,
  appointment_subject VARCHAR(50) NOT NULL,
  appointment_grade VARCHAR(50) NOT NULL,
  appointment_address VARCHAR(255) NOT NULL,
  appointment_time DATETIME NOT NULL,
  appointment_remark VARCHAR(500) NULL,
  appointment_status TINYINT NOT NULL DEFAULT 0,
  appointment_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_appointment_parent (parent_id),
  KEY idx_appointment_teacher (teacher_id),
  CONSTRAINT fk_appointment_parent FOREIGN KEY (parent_id) REFERENCES `user` (id),
  CONSTRAINT fk_appointment_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorite_teacher (
  id VARCHAR(32) PRIMARY KEY,
  parent_id VARCHAR(32) NOT NULL,
  teacher_id VARCHAR(32) NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_parent_teacher (parent_id, teacher_id),
  CONSTRAINT fk_favorite_parent FOREIGN KEY (parent_id) REFERENCES `user` (id),
  CONSTRAINT fk_favorite_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dispatch_record (
  id VARCHAR(32) PRIMARY KEY,
  order_id VARCHAR(32) NOT NULL,
  requirement_id VARCHAR(32) NOT NULL,
  parent_id VARCHAR(32) NOT NULL,
  teacher_id VARCHAR(32) NOT NULL,
  dispatch_time DATETIME NOT NULL,
  dispatch_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_dispatch_order (order_id),
  KEY idx_dispatch_teacher (teacher_id),
  CONSTRAINT fk_dispatch_order FOREIGN KEY (order_id) REFERENCES `order` (id),
  CONSTRAINT fk_dispatch_requirement FOREIGN KEY (requirement_id) REFERENCES requirement (id),
  CONSTRAINT fk_dispatch_parent FOREIGN KEY (parent_id) REFERENCES `user` (id),
  CONSTRAINT fk_dispatch_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher_subject (
  id VARCHAR(32) PRIMARY KEY,
  teacher_id VARCHAR(32) NOT NULL,
  subject_id VARCHAR(32) NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_teacher_subject (teacher_id, subject_id),
  CONSTRAINT fk_teacher_subject_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id),
  CONSTRAINT fk_teacher_subject_subject FOREIGN KEY (subject_id) REFERENCES subject (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher_region (
  id VARCHAR(32) PRIMARY KEY,
  teacher_id VARCHAR(32) NOT NULL,
  region_id VARCHAR(32) NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_teacher_region (teacher_id, region_id),
  CONSTRAINT fk_teacher_region_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id),
  CONSTRAINT fk_teacher_region_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher_success_record (
  id VARCHAR(32) PRIMARY KEY,
  teacher_id VARCHAR(32) NOT NULL,
  order_id VARCHAR(32) NOT NULL,
  success_grade VARCHAR(50) NOT NULL,
  success_order_date DATETIME NOT NULL,
  success_description VARCHAR(500) NULL,
  success_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_success_teacher (teacher_id),
  CONSTRAINT fk_success_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role (
  id VARCHAR(32) PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL,
  role_code VARCHAR(50) NOT NULL,
  role_description VARCHAR(255) NULL,
  role_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin (
  id VARCHAR(32) PRIMARY KEY,
  admin_account VARCHAR(50) NOT NULL,
  admin_password VARCHAR(100) NOT NULL,
  admin_name VARCHAR(50) NOT NULL,
  admin_portrait VARCHAR(255) NULL,
  admin_gender TINYINT DEFAULT 0,
  admin_email VARCHAR(100) NULL,
  admin_phone VARCHAR(20) NULL,
  role_id VARCHAR(32) NOT NULL,
  admin_enable_status TINYINT NOT NULL DEFAULT 1,
  admin_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_admin_account (admin_account),
  CONSTRAINT fk_admin_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu (
  id VARCHAR(32) PRIMARY KEY,
  menu_name VARCHAR(50) NOT NULL,
  menu_parent VARCHAR(32) NULL,
  menu_priority INT NOT NULL DEFAULT 0,
  menu_link VARCHAR(255) NULL,
  menu_icon VARCHAR(100) NULL,
  menu_type TINYINT NOT NULL DEFAULT 0,
  menu_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_menu_parent (menu_parent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_menu (
  id VARCHAR(32) PRIMARY KEY,
  role_id VARCHAR(32) NOT NULL,
  menu_id VARCHAR(32) NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES role (id),
  CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dictionary (
  id VARCHAR(32) PRIMARY KEY,
  dictionary_name VARCHAR(50) NOT NULL,
  dictionary_code VARCHAR(50) NOT NULL,
  dictionary_description VARCHAR(255) NULL,
  dictionary_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_dictionary_code (dictionary_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dictionary_content (
  id VARCHAR(32) PRIMARY KEY,
  dictionary_content_text VARCHAR(100) NOT NULL,
  dictionary_content_value VARCHAR(50) NOT NULL,
  dictionary_id VARCHAR(32) NOT NULL,
  dictionary_content_sort INT NOT NULL DEFAULT 0,
  dictionary_content_status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  KEY idx_dictionary_id (dictionary_id),
  CONSTRAINT fk_dictionary_content_dictionary FOREIGN KEY (dictionary_id) REFERENCES dictionary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE slide (
  id VARCHAR(32) PRIMARY KEY,
  slide_picture VARCHAR(255) NOT NULL,
  slide_link VARCHAR(255) NULL,
  slide_note VARCHAR(255) NULL,
  slide_priority INT NOT NULL DEFAULT 0,
  slide_status TINYINT NOT NULL DEFAULT 1,
  slide_module TINYINT NOT NULL DEFAULT 0,
  slide_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE advertising (
  id VARCHAR(32) PRIMARY KEY,
  advertising_source VARCHAR(100) NOT NULL,
  advertising_title VARCHAR(200) NOT NULL,
  advertising_link VARCHAR(255) NOT NULL,
  advertising_picture VARCHAR(255) NOT NULL,
  advertising_status TINYINT NOT NULL DEFAULT 1,
  advertising_expire_time DATETIME NULL,
  advertising_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO role(id, role_name, role_code, role_description, role_delete_status, create_time, update_time)
VALUES ('role_admin_001', '超级管理员', 'SUPER_ADMIN', '系统默认管理员角色', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), update_time = NOW();

INSERT INTO admin(id, admin_account, admin_password, admin_name, role_id, admin_enable_status, admin_delete_status, create_time, update_time)
VALUES ('admin_001', 'admin', '123456', '平台管理员', 'role_admin_001', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE admin_name = VALUES(admin_name), update_time = NOW();

INSERT INTO subject_category(id, category_name, category_code, category_sort, category_status, category_delete_status, create_time, update_time)
VALUES
('subject_category_001', '小学', 'PRIMARY', 1, 1, 0, NOW(), NOW()),
('subject_category_002', '初中', 'JUNIOR', 2, 1, 0, NOW(), NOW()),
('subject_category_003', '高中', 'SENIOR', 3, 1, 0, NOW(), NOW()),
('subject_category_004', '大学', 'COLLEGE', 4, 1, 0, NOW(), NOW()),
('subject_category_005', '语言', 'LANGUAGE', 5, 1, 0, NOW(), NOW()),
('subject_category_006', '音乐', 'MUSIC', 6, 1, 0, NOW(), NOW()),
('subject_category_007', '其他', 'OTHER', 7, 1, 0, NOW(), NOW()),
('subject_category_008', '计算机', 'COMPUTER', 8, 1, 0, NOW(), NOW()),
('subject_category_009', '职业技能', 'PRO_SKILL', 9, 1, 0, NOW(), NOW()),
('subject_category_010', '资格考试', 'CERT', 10, 1, 0, NOW(), NOW()),
('subject_category_011', '家庭服务', 'HOME_SERVICE', 11, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), update_time = NOW();

INSERT INTO subject(id, subject_name, subject_code, subject_category_id, subject_category, subject_sort, subject_status, subject_delete_status, create_time, update_time)
VALUES
('subject_001', '小学英语', 'SUB_PRIMARY_001', 'subject_category_001', '小学', 1, 1, 0, NOW(), NOW()),
('subject_002', '小学语文', 'SUB_PRIMARY_002', 'subject_category_001', '小学', 2, 1, 0, NOW(), NOW()),
('subject_003', '小学数学', 'SUB_PRIMARY_003', 'subject_category_001', '小学', 3, 1, 0, NOW(), NOW()),
('subject_004', '幼教', 'SUB_PRIMARY_004', 'subject_category_001', '小学', 4, 1, 0, NOW(), NOW()),
('subject_005', '小学奥数', 'SUB_PRIMARY_005', 'subject_category_001', '小学', 5, 1, 0, NOW(), NOW()),
('subject_006', '学前教育', 'SUB_PRIMARY_006', 'subject_category_001', '小学', 6, 1, 0, NOW(), NOW()),
('subject_007', '小学陪读', 'SUB_PRIMARY_007', 'subject_category_001', '小学', 7, 1, 0, NOW(), NOW()),
('subject_008', '家庭教育', 'SUB_PRIMARY_008', 'subject_category_001', '小学', 8, 1, 0, NOW(), NOW()),
('subject_009', '陪玩陪读', 'SUB_PRIMARY_009', 'subject_category_001', '小学', 9, 1, 0, NOW(), NOW()),
('subject_010', '初一初二物理', 'SUB_JUNIOR_001', 'subject_category_002', '初中', 10, 1, 0, NOW(), NOW()),
('subject_011', '初一初二英语', 'SUB_JUNIOR_002', 'subject_category_002', '初中', 11, 1, 0, NOW(), NOW()),
('subject_012', '初一初二数学', 'SUB_JUNIOR_003', 'subject_category_002', '初中', 12, 1, 0, NOW(), NOW()),
('subject_013', '初三英语', 'SUB_JUNIOR_004', 'subject_category_002', '初中', 13, 1, 0, NOW(), NOW()),
('subject_014', '初三数学', 'SUB_JUNIOR_005', 'subject_category_002', '初中', 14, 1, 0, NOW(), NOW()),
('subject_015', '初三化学', 'SUB_JUNIOR_006', 'subject_category_002', '初中', 15, 1, 0, NOW(), NOW()),
('subject_016', '初三物理', 'SUB_JUNIOR_007', 'subject_category_002', '初中', 16, 1, 0, NOW(), NOW()),
('subject_017', '初一初二语文', 'SUB_JUNIOR_008', 'subject_category_002', '初中', 17, 1, 0, NOW(), NOW()),
('subject_018', '初三语文', 'SUB_JUNIOR_009', 'subject_category_002', '初中', 18, 1, 0, NOW(), NOW()),
('subject_019', '初中历史', 'SUB_JUNIOR_010', 'subject_category_002', '初中', 19, 1, 0, NOW(), NOW()),
('subject_020', '初中地理', 'SUB_JUNIOR_011', 'subject_category_002', '初中', 20, 1, 0, NOW(), NOW()),
('subject_021', '初中奥数', 'SUB_JUNIOR_012', 'subject_category_002', '初中', 21, 1, 0, NOW(), NOW()),
('subject_022', '预备班', 'SUB_JUNIOR_013', 'subject_category_002', '初中', 22, 1, 0, NOW(), NOW()),
('subject_023', '家庭教育', 'SUB_JUNIOR_014', 'subject_category_002', '初中', 23, 1, 0, NOW(), NOW()),
('subject_024', '初中科学', 'SUB_JUNIOR_015', 'subject_category_002', '初中', 24, 1, 0, NOW(), NOW()),
('subject_025', '初中政治', 'SUB_JUNIOR_016', 'subject_category_002', '初中', 25, 1, 0, NOW(), NOW()),
('subject_026', '初中道法', 'SUB_JUNIOR_017', 'subject_category_002', '初中', 26, 1, 0, NOW(), NOW()),
('subject_027', '初中生物', 'SUB_JUNIOR_018', 'subject_category_002', '初中', 27, 1, 0, NOW(), NOW()),
('subject_028', '初一初二化学', 'SUB_JUNIOR_019', 'subject_category_002', '初中', 28, 1, 0, NOW(), NOW()),
('subject_029', '高一高二英语', 'SUB_SENIOR_001', 'subject_category_003', '高中', 29, 1, 0, NOW(), NOW()),
('subject_030', '高三英语', 'SUB_SENIOR_002', 'subject_category_003', '高中', 30, 1, 0, NOW(), NOW()),
('subject_031', '高一高二数学', 'SUB_SENIOR_003', 'subject_category_003', '高中', 31, 1, 0, NOW(), NOW()),
('subject_032', '高一高二化学', 'SUB_SENIOR_004', 'subject_category_003', '高中', 32, 1, 0, NOW(), NOW()),
('subject_033', '高一高二语文', 'SUB_SENIOR_005', 'subject_category_003', '高中', 33, 1, 0, NOW(), NOW()),
('subject_034', '高三数学', 'SUB_SENIOR_006', 'subject_category_003', '高中', 34, 1, 0, NOW(), NOW()),
('subject_035', '高三化学', 'SUB_SENIOR_007', 'subject_category_003', '高中', 35, 1, 0, NOW(), NOW()),
('subject_036', '高一高二物理', 'SUB_SENIOR_008', 'subject_category_003', '高中', 36, 1, 0, NOW(), NOW()),
('subject_037', '高三物理', 'SUB_SENIOR_009', 'subject_category_003', '高中', 37, 1, 0, NOW(), NOW()),
('subject_038', '高三语文', 'SUB_SENIOR_010', 'subject_category_003', '高中', 38, 1, 0, NOW(), NOW()),
('subject_039', '高中历史', 'SUB_SENIOR_011', 'subject_category_003', '高中', 39, 1, 0, NOW(), NOW()),
('subject_040', '高中地理', 'SUB_SENIOR_012', 'subject_category_003', '高中', 40, 1, 0, NOW(), NOW()),
('subject_041', '高中政治', 'SUB_SENIOR_013', 'subject_category_003', '高中', 41, 1, 0, NOW(), NOW()),
('subject_042', 'SAT', 'SUB_SENIOR_014', 'subject_category_003', '高中', 42, 1, 0, NOW(), NOW()),
('subject_043', '高中生物', 'SUB_SENIOR_015', 'subject_category_003', '高中', 43, 1, 0, NOW(), NOW()),
('subject_044', '信息科技', 'SUB_SENIOR_016', 'subject_category_003', '高中', 44, 1, 0, NOW(), NOW()),
('subject_045', '雅思', 'SUB_COLLEGE_001', 'subject_category_004', '大学', 45, 1, 0, NOW(), NOW()),
('subject_046', '高等数学', 'SUB_COLLEGE_002', 'subject_category_004', '大学', 46, 1, 0, NOW(), NOW()),
('subject_047', '托福', 'SUB_COLLEGE_003', 'subject_category_004', '大学', 47, 1, 0, NOW(), NOW()),
('subject_048', '英语四级', 'SUB_COLLEGE_004', 'subject_category_004', '大学', 48, 1, 0, NOW(), NOW()),
('subject_049', '微观经济学', 'SUB_COLLEGE_005', 'subject_category_004', '大学', 49, 1, 0, NOW(), NOW()),
('subject_050', '英语六级', 'SUB_COLLEGE_006', 'subject_category_004', '大学', 50, 1, 0, NOW(), NOW()),
('subject_051', '英语专业四级', 'SUB_COLLEGE_007', 'subject_category_004', '大学', 51, 1, 0, NOW(), NOW()),
('subject_052', '英语专业八级', 'SUB_COLLEGE_008', 'subject_category_004', '大学', 52, 1, 0, NOW(), NOW()),
('subject_053', '考研英语', 'SUB_COLLEGE_009', 'subject_category_004', '大学', 53, 1, 0, NOW(), NOW()),
('subject_054', '考研数学', 'SUB_COLLEGE_010', 'subject_category_004', '大学', 54, 1, 0, NOW(), NOW()),
('subject_055', '考研政治', 'SUB_COLLEGE_011', 'subject_category_004', '大学', 55, 1, 0, NOW(), NOW()),
('subject_056', '考研专业课', 'SUB_COLLEGE_012', 'subject_category_004', '大学', 56, 1, 0, NOW(), NOW()),
('subject_057', '在职研究生论文指导', 'SUB_COLLEGE_013', 'subject_category_004', '大学', 57, 1, 0, NOW(), NOW()),
('subject_058', 'MBA', 'SUB_COLLEGE_014', 'subject_category_004', '大学', 58, 1, 0, NOW(), NOW()),
('subject_059', '考博英语', 'SUB_COLLEGE_015', 'subject_category_004', '大学', 59, 1, 0, NOW(), NOW()),
('subject_060', '考博专业课', 'SUB_COLLEGE_016', 'subject_category_004', '大学', 60, 1, 0, NOW(), NOW()),
('subject_061', '专升本', 'SUB_COLLEGE_017', 'subject_category_004', '大学', 61, 1, 0, NOW(), NOW()),
('subject_062', '自学考试', 'SUB_COLLEGE_018', 'subject_category_004', '大学', 62, 1, 0, NOW(), NOW()),
('subject_063', '考博面试辅导', 'SUB_COLLEGE_019', 'subject_category_004', '大学', 63, 1, 0, NOW(), NOW()),
('subject_064', '国学', 'SUB_COLLEGE_020', 'subject_category_004', '大学', 64, 1, 0, NOW(), NOW()),
('subject_065', '成人高考', 'SUB_COLLEGE_021', 'subject_category_004', '大学', 65, 1, 0, NOW(), NOW()),
('subject_066', '上海话', 'SUB_LANGUAGE_001', 'subject_category_005', '语言', 66, 1, 0, NOW(), NOW()),
('subject_067', '日语', 'SUB_LANGUAGE_002', 'subject_category_005', '语言', 67, 1, 0, NOW(), NOW()),
('subject_068', '英语口语', 'SUB_LANGUAGE_003', 'subject_category_005', '语言', 68, 1, 0, NOW(), NOW()),
('subject_069', '新概念英语', 'SUB_LANGUAGE_004', 'subject_category_005', '语言', 69, 1, 0, NOW(), NOW()),
('subject_070', '牛津英语', 'SUB_LANGUAGE_005', 'subject_category_005', '语言', 70, 1, 0, NOW(), NOW()),
('subject_071', '西班牙语', 'SUB_LANGUAGE_006', 'subject_category_005', '语言', 71, 1, 0, NOW(), NOW()),
('subject_072', '德语', 'SUB_LANGUAGE_007', 'subject_category_005', '语言', 72, 1, 0, NOW(), NOW()),
('subject_073', '商务英语', 'SUB_LANGUAGE_008', 'subject_category_005', '语言', 73, 1, 0, NOW(), NOW()),
('subject_074', '法语', 'SUB_LANGUAGE_009', 'subject_category_005', '语言', 74, 1, 0, NOW(), NOW()),
('subject_075', '韩语', 'SUB_LANGUAGE_010', 'subject_category_005', '语言', 75, 1, 0, NOW(), NOW()),
('subject_076', '意大利语', 'SUB_LANGUAGE_011', 'subject_category_005', '语言', 76, 1, 0, NOW(), NOW()),
('subject_077', '对外汉语', 'SUB_LANGUAGE_012', 'subject_category_005', '语言', 77, 1, 0, NOW(), NOW()),
('subject_078', '阿拉伯语', 'SUB_LANGUAGE_013', 'subject_category_005', '语言', 78, 1, 0, NOW(), NOW()),
('subject_079', '俄语', 'SUB_LANGUAGE_014', 'subject_category_005', '语言', 79, 1, 0, NOW(), NOW()),
('subject_080', '葡萄牙语', 'SUB_LANGUAGE_015', 'subject_category_005', '语言', 80, 1, 0, NOW(), NOW()),
('subject_081', '托福sbs英语', 'SUB_LANGUAGE_016', 'subject_category_005', '语言', 81, 1, 0, NOW(), NOW()),
('subject_082', '剑桥英语', 'SUB_LANGUAGE_017', 'subject_category_005', '语言', 82, 1, 0, NOW(), NOW()),
('subject_083', '小提琴', 'SUB_MUSIC_001', 'subject_category_006', '音乐', 83, 1, 0, NOW(), NOW()),
('subject_084', '钢琴', 'SUB_MUSIC_002', 'subject_category_006', '音乐', 84, 1, 0, NOW(), NOW()),
('subject_085', '电子琴', 'SUB_MUSIC_003', 'subject_category_006', '音乐', 85, 1, 0, NOW(), NOW()),
('subject_086', '长笛', 'SUB_MUSIC_004', 'subject_category_006', '音乐', 86, 1, 0, NOW(), NOW()),
('subject_087', '琵琶', 'SUB_MUSIC_005', 'subject_category_006', '音乐', 87, 1, 0, NOW(), NOW()),
('subject_088', '手风琴', 'SUB_MUSIC_006', 'subject_category_006', '音乐', 88, 1, 0, NOW(), NOW()),
('subject_089', '古筝', 'SUB_MUSIC_007', 'subject_category_006', '音乐', 89, 1, 0, NOW(), NOW()),
('subject_090', '视唱练耳', 'SUB_MUSIC_008', 'subject_category_006', '音乐', 90, 1, 0, NOW(), NOW()),
('subject_091', '声乐', 'SUB_MUSIC_009', 'subject_category_006', '音乐', 91, 1, 0, NOW(), NOW()),
('subject_092', '单簧管', 'SUB_MUSIC_010', 'subject_category_006', '音乐', 92, 1, 0, NOW(), NOW()),
('subject_093', '大号', 'SUB_MUSIC_011', 'subject_category_006', '音乐', 93, 1, 0, NOW(), NOW()),
('subject_094', '萨克斯', 'SUB_MUSIC_012', 'subject_category_006', '音乐', 94, 1, 0, NOW(), NOW()),
('subject_095', '吉他', 'SUB_MUSIC_013', 'subject_category_006', '音乐', 95, 1, 0, NOW(), NOW()),
('subject_096', '小号', 'SUB_MUSIC_014', 'subject_category_006', '音乐', 96, 1, 0, NOW(), NOW()),
('subject_097', '古琴', 'SUB_MUSIC_015', 'subject_category_006', '音乐', 97, 1, 0, NOW(), NOW()),
('subject_098', '二胡', 'SUB_MUSIC_016', 'subject_category_006', '音乐', 98, 1, 0, NOW(), NOW()),
('subject_099', '大提琴', 'SUB_MUSIC_017', 'subject_category_006', '音乐', 99, 1, 0, NOW(), NOW()),
('subject_100', '打击乐', 'SUB_MUSIC_018', 'subject_category_006', '音乐', 100, 1, 0, NOW(), NOW()),
('subject_101', '圆号', 'SUB_MUSIC_019', 'subject_category_006', '音乐', 101, 1, 0, NOW(), NOW()),
('subject_102', '中提琴', 'SUB_MUSIC_020', 'subject_category_006', '音乐', 102, 1, 0, NOW(), NOW()),
('subject_103', '竹笛', 'SUB_MUSIC_021', 'subject_category_006', '音乐', 103, 1, 0, NOW(), NOW()),
('subject_104', '架子鼓', 'SUB_MUSIC_022', 'subject_category_006', '音乐', 104, 1, 0, NOW(), NOW()),
('subject_105', '舞蹈', 'SUB_MUSIC_023', 'subject_category_006', '音乐', 105, 1, 0, NOW(), NOW()),
('subject_106', '扬琴', 'SUB_MUSIC_024', 'subject_category_006', '音乐', 106, 1, 0, NOW(), NOW()),
('subject_107', '长号', 'SUB_MUSIC_025', 'subject_category_006', '音乐', 107, 1, 0, NOW(), NOW()),
('subject_108', '巴松', 'SUB_MUSIC_026', 'subject_category_006', '音乐', 108, 1, 0, NOW(), NOW()),
('subject_109', '羽毛球', 'SUB_OTHER_001', 'subject_category_007', '其他', 109, 1, 0, NOW(), NOW()),
('subject_110', '跆拳道', 'SUB_OTHER_002', 'subject_category_007', '其他', 110, 1, 0, NOW(), NOW()),
('subject_111', '游泳', 'SUB_OTHER_003', 'subject_category_007', '其他', 111, 1, 0, NOW(), NOW()),
('subject_112', '乒乓球', 'SUB_OTHER_004', 'subject_category_007', '其他', 112, 1, 0, NOW(), NOW()),
('subject_113', '美术', 'SUB_OTHER_005', 'subject_category_007', '其他', 113, 1, 0, NOW(), NOW()),
('subject_114', '网球', 'SUB_OTHER_006', 'subject_category_007', '其他', 114, 1, 0, NOW(), NOW()),
('subject_115', '书法', 'SUB_OTHER_007', 'subject_category_007', '其他', 115, 1, 0, NOW(), NOW()),
('subject_116', '卡通画', 'SUB_OTHER_008', 'subject_category_007', '其他', 116, 1, 0, NOW(), NOW()),
('subject_117', '中国象棋', 'SUB_OTHER_009', 'subject_category_007', '其他', 117, 1, 0, NOW(), NOW()),
('subject_118', '国际象棋', 'SUB_OTHER_010', 'subject_category_007', '其他', 118, 1, 0, NOW(), NOW()),
('subject_119', '中国画', 'SUB_OTHER_011', 'subject_category_007', '其他', 119, 1, 0, NOW(), NOW()),
('subject_120', '围棋', 'SUB_OTHER_012', 'subject_category_007', '其他', 120, 1, 0, NOW(), NOW()),
('subject_121', '素描', 'SUB_OTHER_013', 'subject_category_007', '其他', 121, 1, 0, NOW(), NOW()),
('subject_122', '油画', 'SUB_OTHER_014', 'subject_category_007', '其他', 122, 1, 0, NOW(), NOW()),
('subject_123', '篮球', 'SUB_OTHER_015', 'subject_category_007', '其他', 123, 1, 0, NOW(), NOW()),
('subject_124', '水彩', 'SUB_OTHER_016', 'subject_category_007', '其他', 124, 1, 0, NOW(), NOW()),
('subject_125', '空手道', 'SUB_OTHER_017', 'subject_category_007', '其他', 125, 1, 0, NOW(), NOW()),
('subject_126', '编程', 'SUB_OTHER_018', 'subject_category_007', '其他', 126, 1, 0, NOW(), NOW()),
('subject_127', '漫画', 'SUB_OTHER_019', 'subject_category_007', '其他', 127, 1, 0, NOW(), NOW()),
('subject_128', '计算机', 'SUB_OTHER_020', 'subject_category_007', '其他', 128, 1, 0, NOW(), NOW()),
('subject_129', '潜能开发', 'SUB_OTHER_021', 'subject_category_007', '其他', 129, 1, 0, NOW(), NOW()),
('subject_130', '机器人编程', 'SUB_OTHER_022', 'subject_category_007', '其他', 130, 1, 0, NOW(), NOW()),
('subject_131', '学习习惯', 'SUB_OTHER_023', 'subject_category_007', '其他', 131, 1, 0, NOW(), NOW()),
('subject_132', '口才', 'SUB_OTHER_024', 'subject_category_007', '其他', 132, 1, 0, NOW(), NOW()),
('subject_133', '记忆力', 'SUB_OTHER_025', 'subject_category_007', '其他', 133, 1, 0, NOW(), NOW()),
('subject_134', '注意力', 'SUB_OTHER_026', 'subject_category_007', '其他', 134, 1, 0, NOW(), NOW()),
('subject_135', '心理辅导', 'SUB_OTHER_027', 'subject_category_007', '其他', 135, 1, 0, NOW(), NOW()),
('subject_136', 'SAT体育', 'SUB_OTHER_028', 'subject_category_007', '其他', 136, 1, 0, NOW(), NOW()),
('subject_137', '其他', 'SUB_OTHER_029', 'subject_category_007', '其他', 137, 1, 0, NOW(), NOW()),
('subject_138', '足球', 'SUB_OTHER_030', 'subject_category_007', '其他', 138, 1, 0, NOW(), NOW()),
('subject_139', '计算机应用能力初级', 'SUB_COMPUTER_001', 'subject_category_008', '计算机', 139, 1, 0, NOW(), NOW()),
('subject_140', '计算机能力应用中级', 'SUB_COMPUTER_002', 'subject_category_008', '计算机', 140, 1, 0, NOW(), NOW()),
('subject_141', '计算机基本操作', 'SUB_COMPUTER_003', 'subject_category_008', '计算机', 141, 1, 0, NOW(), NOW()),
('subject_142', '大数据', 'SUB_COMPUTER_004', 'subject_category_008', '计算机', 142, 1, 0, NOW(), NOW()),
('subject_143', 'MySQL', 'SUB_COMPUTER_005', 'subject_category_008', '计算机', 143, 1, 0, NOW(), NOW()),
('subject_144', 'Python', 'SUB_COMPUTER_006', 'subject_category_008', '计算机', 144, 1, 0, NOW(), NOW()),
('subject_145', '系统运维', 'SUB_COMPUTER_007', 'subject_category_008', '计算机', 145, 1, 0, NOW(), NOW()),
('subject_146', '系统架构', 'SUB_COMPUTER_008', 'subject_category_008', '计算机', 146, 1, 0, NOW(), NOW()),
('subject_147', '网页设计', 'SUB_COMPUTER_009', 'subject_category_008', '计算机', 147, 1, 0, NOW(), NOW()),
('subject_148', '直播主播', 'SUB_COMPUTER_010', 'subject_category_008', '计算机', 148, 1, 0, NOW(), NOW()),
('subject_149', '网站开发', 'SUB_COMPUTER_011', 'subject_category_008', '计算机', 149, 1, 0, NOW(), NOW()),
('subject_150', '短视频剪辑', 'SUB_COMPUTER_012', 'subject_category_008', '计算机', 150, 1, 0, NOW(), NOW()),
('subject_151', '营销文案', 'SUB_COMPUTER_013', 'subject_category_008', '计算机', 151, 1, 0, NOW(), NOW()),
('subject_152', '短视频运营', 'SUB_COMPUTER_014', 'subject_category_008', '计算机', 152, 1, 0, NOW(), NOW()),
('subject_153', '视频制作', 'SUB_COMPUTER_015', 'subject_category_008', '计算机', 153, 1, 0, NOW(), NOW()),
('subject_154', '人力资源培训', 'SUB_PRO_SKILL_001', 'subject_category_009', '职业技能', 154, 1, 0, NOW(), NOW()),
('subject_155', '经济师', 'SUB_PRO_SKILL_002', 'subject_category_009', '职业技能', 155, 1, 0, NOW(), NOW()),
('subject_156', '电子商务', 'SUB_PRO_SKILL_003', 'subject_category_009', '职业技能', 156, 1, 0, NOW(), NOW()),
('subject_157', '注册会计师', 'SUB_PRO_SKILL_004', 'subject_category_009', '职业技能', 157, 1, 0, NOW(), NOW()),
('subject_158', '会计从业资格', 'SUB_PRO_SKILL_005', 'subject_category_009', '职业技能', 158, 1, 0, NOW(), NOW()),
('subject_159', '造价工程师', 'SUB_CERT_001', 'subject_category_010', '资格考试', 159, 1, 0, NOW(), NOW()),
('subject_160', '人力资源师', 'SUB_CERT_002', 'subject_category_010', '资格考试', 160, 1, 0, NOW(), NOW()),
('subject_161', '一级建造师', 'SUB_CERT_003', 'subject_category_010', '资格考试', 161, 1, 0, NOW(), NOW()),
('subject_162', '二级建造师', 'SUB_CERT_004', 'subject_category_010', '资格考试', 162, 1, 0, NOW(), NOW()),
('subject_163', '执业医师', 'SUB_CERT_005', 'subject_category_010', '资格考试', 163, 1, 0, NOW(), NOW()),
('subject_164', '心理咨询师', 'SUB_CERT_006', 'subject_category_010', '资格考试', 164, 1, 0, NOW(), NOW()),
('subject_165', '健康护理员', 'SUB_CERT_007', 'subject_category_010', '资格考试', 165, 1, 0, NOW(), NOW()),
('subject_166', '护士资格证', 'SUB_CERT_008', 'subject_category_010', '资格考试', 166, 1, 0, NOW(), NOW()),
('subject_167', '教师资格证', 'SUB_CERT_009', 'subject_category_010', '资格考试', 167, 1, 0, NOW(), NOW()),
('subject_168', '月嫂', 'SUB_HOME_SERVICE_001', 'subject_category_011', '家庭服务', 168, 1, 0, NOW(), NOW()),
('subject_169', '保姆', 'SUB_HOME_SERVICE_002', 'subject_category_011', '家庭服务', 169, 1, 0, NOW(), NOW()),
('subject_170', '保洁', 'SUB_HOME_SERVICE_003', 'subject_category_011', '家庭服务', 170, 1, 0, NOW(), NOW()),
('subject_171', '育儿嫂', 'SUB_HOME_SERVICE_004', 'subject_category_011', '家庭服务', 171, 1, 0, NOW(), NOW()),
('subject_172', '育婴师', 'SUB_HOME_SERVICE_005', 'subject_category_011', '家庭服务', 172, 1, 0, NOW(), NOW()),
('subject_173', '陪护', 'SUB_HOME_SERVICE_006', 'subject_category_011', '家庭服务', 173, 1, 0, NOW(), NOW()),
('subject_174', '钟点工', 'SUB_HOME_SERVICE_007', 'subject_category_011', '家庭服务', 174, 1, 0, NOW(), NOW()),
('subject_175', '营养师', 'SUB_HOME_SERVICE_008', 'subject_category_011', '家庭服务', 175, 1, 0, NOW(), NOW()),
('subject_176', '搬家', 'SUB_HOME_SERVICE_009', 'subject_category_011', '家庭服务', 176, 1, 0, NOW(), NOW()),
('subject_177', '催乳师', 'SUB_HOME_SERVICE_010', 'subject_category_011', '家庭服务', 177, 1, 0, NOW(), NOW()),
('subject_178', '婴幼儿营养', 'SUB_HOME_SERVICE_011', 'subject_category_011', '家庭服务', 178, 1, 0, NOW(), NOW()),
('subject_179', '家电维修', 'SUB_HOME_SERVICE_012', 'subject_category_011', '家庭服务', 179, 1, 0, NOW(), NOW()),
('subject_180', '房屋维修', 'SUB_HOME_SERVICE_013', 'subject_category_011', '家庭服务', 180, 1, 0, NOW(), NOW()),
('subject_181', '家具维修', 'SUB_HOME_SERVICE_014', 'subject_category_011', '家庭服务', 181, 1, 0, NOW(), NOW()),
('subject_182', '电脑维修', 'SUB_HOME_SERVICE_015', 'subject_category_011', '家庭服务', 182, 1, 0, NOW(), NOW()),
('subject_183', '手机维修', 'SUB_HOME_SERVICE_016', 'subject_category_011', '家庭服务', 183, 1, 0, NOW(), NOW()),
('subject_184', '开锁换锁修锁', 'SUB_HOME_SERVICE_017', 'subject_category_011', '家庭服务', 184, 1, 0, NOW(), NOW()),
('subject_185', '管道疏通', 'SUB_HOME_SERVICE_018', 'subject_category_011', '家庭服务', 185, 1, 0, NOW(), NOW()),
('subject_186', '空调清洗', 'SUB_HOME_SERVICE_019', 'subject_category_011', '家庭服务', 186, 1, 0, NOW(), NOW()),
('subject_187', '油烟机清洗', 'SUB_HOME_SERVICE_020', 'subject_category_011', '家庭服务', 187, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), update_time = NOW();

INSERT INTO region(id, region_name, region_code, region_city, region_province, region_sort, region_status, region_delete_status, create_time, update_time)
VALUES
('region_001', '海淀区', 'REG_HAIDIAN', '北京', '北京', 1, 1, 0, NOW(), NOW()),
('region_002', '朝阳区', 'REG_CHAOYANG', '北京', '北京', 2, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE region_name = VALUES(region_name), update_time = NOW();

INSERT INTO school(id, school_name, school_code, school_type, school_province, school_city, school_district, school_address, school_status, school_delete_status, create_time, update_time)
VALUES
('school_001', '北京大学', 'SCH_PKU', 0, '北京', '北京', '海淀区', '北京市海淀区颐和园路5号', 1, 0, NOW(), NOW()),
('school_002', '清华大学', 'SCH_THU', 0, '北京', '北京', '海淀区', '北京市海淀区清华园1号', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE school_name = VALUES(school_name), update_time = NOW();
