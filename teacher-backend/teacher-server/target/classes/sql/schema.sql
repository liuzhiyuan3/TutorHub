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
DROP TABLE IF EXISTS subject;

CREATE TABLE `user` (
  id VARCHAR(32) PRIMARY KEY,
  user_account VARCHAR(50) NOT NULL,
  user_password VARCHAR(100) NOT NULL,
  user_name VARCHAR(50) NOT NULL,
  user_portrait VARCHAR(255) NULL,
  user_gender TINYINT DEFAULT 0,
  user_email VARCHAR(100) NULL,
  user_phone VARCHAR(20) NOT NULL,
  user_type TINYINT NOT NULL DEFAULT 0,
  user_status TINYINT NOT NULL DEFAULT 1,
  user_delete_status TINYINT NOT NULL DEFAULT 0,
  last_login_time DATETIME NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_user_account (user_account),
  UNIQUE KEY uk_user_phone (user_phone),
  KEY idx_user_type (user_type),
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
  teacher_school VARCHAR(100) NULL,
  teacher_major VARCHAR(100) NULL,
  teacher_education VARCHAR(50) NULL,
  teacher_self_description TEXT NULL,
  teacher_tutoring_method TINYINT NOT NULL DEFAULT 0,
  teacher_experience TEXT NULL,
  teacher_success_count INT NOT NULL DEFAULT 0,
  teacher_view_count INT NOT NULL DEFAULT 0,
  teacher_audit_status TINYINT NOT NULL DEFAULT 0,
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

CREATE TABLE subject (
  id VARCHAR(32) PRIMARY KEY,
  subject_name VARCHAR(50) NOT NULL,
  subject_code VARCHAR(50) NOT NULL,
  subject_category VARCHAR(50) NOT NULL,
  subject_description VARCHAR(255) NULL,
  subject_sort INT NOT NULL DEFAULT 0,
  subject_status TINYINT NOT NULL DEFAULT 1,
  subject_delete_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  UNIQUE KEY uk_subject_code (subject_code),
  KEY idx_subject_category (subject_category)
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
  requirement_tutoring_method TINYINT NOT NULL DEFAULT 0,
  requirement_frequency VARCHAR(50) NULL,
  requirement_salary DECIMAL(10,2) NOT NULL,
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
