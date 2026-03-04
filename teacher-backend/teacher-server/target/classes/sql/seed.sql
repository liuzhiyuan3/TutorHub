USE teacher_service;

INSERT INTO role(id, role_name, role_code, role_description, role_delete_status, create_time, update_time)
VALUES ('role_admin_001', '超级管理员', 'SUPER_ADMIN', '系统默认管理员角色', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), update_time = NOW();

INSERT INTO admin(id, admin_account, admin_password, admin_name, role_id, admin_enable_status, admin_delete_status, create_time, update_time)
VALUES ('admin_001', 'admin', '123456', '平台管理员', 'role_admin_001', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE admin_name = VALUES(admin_name), update_time = NOW();

INSERT INTO subject(id, subject_name, subject_code, subject_category, subject_sort, subject_status, subject_delete_status, create_time, update_time)
VALUES
('subject_001', '小学数学', 'SUB_MATH_PRIMARY', '小学', 1, 1, 0, NOW(), NOW()),
('subject_002', '初中英语', 'SUB_ENGLISH_JUNIOR', '初中', 2, 1, 0, NOW(), NOW()),
('subject_003', '高中物理', 'SUB_PHYSICS_HIGH', '高中', 3, 1, 0, NOW(), NOW())
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
