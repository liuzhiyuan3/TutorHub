-- Home page seed data
-- 用途：为小程序首页生成“每个区块都有数据”的演示数据
-- 执行示例：
-- mysql -h127.0.0.1 -P3306 -uroot -p teacher_service < teacher-backend/teacher-server/src/main/resources/sql/home_page_seed.sql

USE teacher_service;
SET NAMES utf8mb4;

START TRANSACTION;

-- 1) 基础分类与学科（热门科目）
INSERT INTO subject_category(id, category_name, category_code, category_sort, category_status, category_delete_status, create_time, update_time)
VALUES
('subject_category_001', '小学', 'PRIMARY', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), category_status = 1, category_delete_status = 0, update_time = NOW();

INSERT INTO subject(id, subject_name, subject_code, subject_category_id, subject_category, subject_sort, subject_status, subject_delete_status, create_time, update_time)
VALUES
('subject_001', '小学英语', 'SUB_PRIMARY_001', 'subject_category_001', '小学', 1, 1, 0, NOW(), NOW()),
('subject_002', '小学语文', 'SUB_PRIMARY_002', 'subject_category_001', '小学', 2, 1, 0, NOW(), NOW()),
('subject_003', '小学数学', 'SUB_PRIMARY_003', 'subject_category_001', '小学', 3, 1, 0, NOW(), NOW()),
('subject_004', '幼教', 'SUB_PRIMARY_004', 'subject_category_001', '小学', 4, 1, 0, NOW(), NOW()),
('subject_005', '小学奥数', 'SUB_PRIMARY_005', 'subject_category_001', '小学', 5, 1, 0, NOW(), NOW()),
('subject_006', '学前教育', 'SUB_PRIMARY_006', 'subject_category_001', '小学', 6, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), subject_status = 1, subject_delete_status = 0, update_time = NOW();

-- 2) 服务区域 + 学校
INSERT INTO region(id, region_name, region_code, region_city, region_province, region_sort, region_status, region_delete_status, create_time, update_time)
VALUES
('region_001', '海淀区', 'REG_HAIDIAN', '北京', '北京', 1, 1, 0, NOW(), NOW()),
('region_002', '朝阳区', 'REG_CHAOYANG', '北京', '北京', 2, 1, 0, NOW(), NOW()),
('region_003', '丰台区', 'REG_FENGTAI', '北京', '北京', 3, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE region_name = VALUES(region_name), region_status = 1, region_delete_status = 0, update_time = NOW();

INSERT INTO school(id, school_name, school_code, school_type, school_province, school_city, school_district, school_address, school_status, school_delete_status, create_time, update_time)
VALUES
('school_001', '北京大学', 'SCH_PKU', 0, '北京', '北京', '海淀区', '北京市海淀区颐和园路5号', 1, 0, NOW(), NOW()),
('school_002', '清华大学', 'SCH_THU', 0, '北京', '北京', '海淀区', '北京市海淀区清华园1号', 1, 0, NOW(), NOW()),
('school_003', '北京师范大学', 'SCH_BNU', 0, '北京', '北京', '海淀区', '北京市海淀区新街口外大街19号', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE school_name = VALUES(school_name), school_status = 1, school_delete_status = 0, update_time = NOW();

-- 3) 用户（1个家长 + 3个教员）
INSERT INTO `user`(
  id, user_account, user_password, user_wechat_openid, user_name, user_portrait, user_gender, user_email, user_phone,
  user_location_address, user_location_longitude, user_location_latitude, user_region_code, user_region_name,
  user_region_province, user_region_city, user_region_district, user_region_source, user_region_sync_time,
  user_type, user_status, profile_completed, nickname_source, avatar_source, last_profile_complete_time,
  user_delete_status, last_login_time, create_time, update_time
)
VALUES
('user_parent_001', 'parent_demo', '123456', NULL, '家长演示用户', 'https://picsum.photos/seed/parent001/300/300', 0, 'parent@example.com', '13900001001',
 '北京市海淀区中关村', 116.316389, 39.983424, 'region_001', '海淀区', '北京', '北京', '海淀区', 'gps', NOW(),
 0, 1, 1, 'manual', 'manual', NOW(), 0, NOW(), NOW(), NOW()),
('user_teacher_001', 'teacher_demo_01', '123456', NULL, '王老师', 'https://picsum.photos/seed/teacher001/300/300', 0, 't1@example.com', '13900001011',
 '北京市海淀区学院路', 116.353000, 39.983000, 'region_001', '海淀区', '北京', '北京', '海淀区', 'gps', NOW(),
 1, 1, 1, 'manual', 'manual', NOW(), 0, NOW(), NOW(), NOW()),
('user_teacher_002', 'teacher_demo_02', '123456', NULL, '李老师', 'https://picsum.photos/seed/teacher002/300/300', 1, 't2@example.com', '13900001012',
 '北京市朝阳区望京', 116.470000, 39.990000, 'region_002', '朝阳区', '北京', '北京', '朝阳区', 'gps', NOW(),
 1, 1, 1, 'manual', 'manual', NOW(), 0, NOW(), NOW(), NOW()),
('user_teacher_003', 'teacher_demo_03', '123456', NULL, '张老师', 'https://picsum.photos/seed/teacher003/300/300', 0, 't3@example.com', '13900001013',
 '北京市丰台区科技园', 116.290000, 39.860000, 'region_003', '丰台区', '北京', '北京', '丰台区', 'gps', NOW(),
 1, 1, 1, 'manual', 'manual', NOW(), 0, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE
  user_name = VALUES(user_name),
  user_portrait = VALUES(user_portrait),
  user_phone = VALUES(user_phone),
  user_type = VALUES(user_type),
  user_status = 1,
  user_delete_status = 0,
  profile_completed = 1,
  update_time = NOW();

-- 4) 教员信息（首页推荐教师）
INSERT INTO teacher_info(
  id, user_id, teacher_identity, teacher_photo, teacher_age, teacher_teaching_years, teacher_hometown, teacher_home_address,
  teacher_work_address, teacher_work_longitude, teacher_work_latitude, teacher_school, teacher_major, teacher_education,
  teacher_self_description, teacher_tutoring_method, teacher_experience, teacher_success_count, teacher_view_count,
  teacher_audit_status, teacher_cert_no, teacher_cert_images, teacher_profile_completed, teacher_enable_status, teacher_delete_status,
  create_time, update_time
)
VALUES
('teacher_001', 'user_teacher_001', '在职教师', 'https://picsum.photos/seed/tinfo001/600/600', 32, 8, '河北保定', '北京市海淀区学院路',
 '北京市海淀区学院路', 116.353000, 39.983000, '北京大学', '英语教育', '本科',
 '擅长小学英语提分与阅读写作，带班经验丰富。', 2, '长期一线教学，善于分层辅导。', 26, 580,
 1, NULL, NULL, 1, 1, 0, NOW(), NOW()),
('teacher_002', 'user_teacher_002', '研究生在读', 'https://picsum.photos/seed/tinfo002/600/600', 25, 4, '山东青岛', '北京市朝阳区望京',
 '北京市朝阳区望京', 116.470000, 39.990000, '清华大学', '汉语言文学', '硕士',
 '语文写作与阅读理解专项辅导，耐心细致。', 2, '多次辅导中考语文冲刺。', 15, 420,
 1, NULL, NULL, 1, 1, 0, NOW(), NOW()),
('teacher_003', 'user_teacher_003', '专业家教', 'https://picsum.photos/seed/tinfo003/600/600', 29, 6, '河南郑州', '北京市丰台区科技园',
 '北京市丰台区科技园', 116.290000, 39.860000, '北京师范大学', '数学与应用数学', '本科',
 '擅长小学数学与奥数思维启蒙，课堂互动性强。', 2, '善于制定阶段学习计划。', 19, 500,
 1, NULL, NULL, 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  teacher_identity = VALUES(teacher_identity),
  teacher_photo = VALUES(teacher_photo),
  teacher_school = VALUES(teacher_school),
  teacher_major = VALUES(teacher_major),
  teacher_education = VALUES(teacher_education),
  teacher_self_description = VALUES(teacher_self_description),
  teacher_tutoring_method = VALUES(teacher_tutoring_method),
  teacher_success_count = VALUES(teacher_success_count),
  teacher_view_count = VALUES(teacher_view_count),
  teacher_audit_status = 1,
  teacher_profile_completed = 1,
  teacher_enable_status = 1,
  teacher_delete_status = 0,
  update_time = NOW();

-- 5) 教员-学科/区域关联
INSERT INTO teacher_subject(id, teacher_id, subject_id, create_time)
VALUES
('ts_001', 'teacher_001', 'subject_001', NOW()),
('ts_002', 'teacher_001', 'subject_003', NOW()),
('ts_003', 'teacher_002', 'subject_002', NOW()),
('ts_004', 'teacher_003', 'subject_003', NOW()),
('ts_005', 'teacher_003', 'subject_005', NOW())
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);

INSERT INTO teacher_region(id, teacher_id, region_id, create_time)
VALUES
('tr_001', 'teacher_001', 'region_001', NOW()),
('tr_002', 'teacher_002', 'region_002', NOW()),
('tr_003', 'teacher_003', 'region_003', NOW())
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);

-- 6) 家教需求（首页最新需求）
INSERT INTO requirement(
  id, parent_id, requirement_title, requirement_description, subject_id, requirement_grade, region_id, requirement_address,
  requirement_longitude, requirement_latitude, requirement_tutoring_method, requirement_frequency, requirement_salary,
  student_gender, salary_text, cross_street, student_detail, teacher_qualification, teacher_gender_preference,
  teacher_requirement_text, requirement_other, requirement_images, requirement_status, requirement_audit_status,
  requirement_delete_status, create_time, update_time
)
VALUES
('req_001', 'user_parent_001', '小学三年级英语提升', '词汇和阅读能力提升，周末上课。', 'subject_001', '小学三年级', 'region_001', '北京市海淀区中关村附近',
 116.320000, 39.985000, 2, '每周2次', 180.00, '女', '180/小时', NULL, '孩子基础一般，需要系统辅导', '有小学教学经验', '不限',
 '沟通顺畅，耐心负责', 'fast', NULL, 0, 1, 0, NOW() - INTERVAL 1 DAY, NOW()),
('req_002', 'user_parent_001', '小学四年级语文阅读写作', '阅读理解与作文训练。', 'subject_002', '小学四年级', 'region_002', '北京市朝阳区望京附近',
 116.470000, 39.993000, 2, '每周1-2次', 200.00, '男', '200/小时', NULL, '希望老师能带阅读方法', '有语文辅导经验', '女',
 '课堂氛围轻松，注重反馈', 'urgent', NULL, 0, 1, 0, NOW() - INTERVAL 2 DAY, NOW()),
('req_003', 'user_parent_001', '小学奥数思维训练', '提升逻辑思维和解题速度。', 'subject_005', '小学五年级', 'region_003', '北京市丰台区科技园附近',
 116.292000, 39.862000, 2, '每周2次', 220.00, '男', '220/小时', NULL, '希望老师有奥数经验', '竞赛辅导经验优先', '不限',
 '需要阶段测评', 'normal', NULL, 0, 1, 0, NOW() - INTERVAL 3 DAY, NOW())
ON DUPLICATE KEY UPDATE
  requirement_title = VALUES(requirement_title),
  requirement_description = VALUES(requirement_description),
  requirement_salary = VALUES(requirement_salary),
  requirement_status = 0,
  requirement_audit_status = 1,
  requirement_delete_status = 0,
  update_time = NOW();

-- 7) 订单 + 派单记录（首页派单动态）
INSERT INTO `order`(
  id, order_number, requirement_id, parent_id, teacher_id, order_status, order_amount,
  order_start_time, order_end_time, order_remark, order_audit_status, order_delete_status, create_time, update_time
)
VALUES
('order_001', 'OD202603280001', 'req_001', 'user_parent_001', 'teacher_001', 1, 720.00,
 NOW() - INTERVAL 2 DAY, NOW() + INTERVAL 28 DAY, '每周两次，按月结算', 1, 0, NOW() - INTERVAL 2 DAY, NOW()),
('order_002', 'OD202603280002', 'req_002', 'user_parent_001', 'teacher_002', 0, 800.00,
 NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, '阅读写作专项', 1, 0, NOW() - INTERVAL 1 DAY, NOW())
ON DUPLICATE KEY UPDATE
  order_status = VALUES(order_status),
  order_amount = VALUES(order_amount),
  order_audit_status = 1,
  order_delete_status = 0,
  update_time = NOW();

INSERT INTO dispatch_record(
  id, order_id, requirement_id, parent_id, teacher_id, dispatch_time, dispatch_status, create_time, update_time
)
VALUES
('dispatch_001', 'order_001', 'req_001', 'user_parent_001', 'teacher_001', NOW() - INTERVAL 2 DAY, 1, NOW() - INTERVAL 2 DAY, NOW()),
('dispatch_002', 'order_002', 'req_002', 'user_parent_001', 'teacher_002', NOW() - INTERVAL 1 DAY, 1, NOW() - INTERVAL 1 DAY, NOW()),
('dispatch_003', 'order_001', 'req_003', 'user_parent_001', 'teacher_003', NOW() - INTERVAL 8 HOUR, 1, NOW() - INTERVAL 8 HOUR, NOW())
ON DUPLICATE KEY UPDATE
  dispatch_status = VALUES(dispatch_status),
  dispatch_time = VALUES(dispatch_time),
  update_time = NOW();

-- 8) 首页轮播
INSERT INTO slide(
  id, slide_picture, slide_link, slide_note, slide_priority, slide_status, slide_module, slide_delete_status, create_time, update_time
)
VALUES
('slide_home_001', 'https://picsum.photos/seed/slide001/1200/500', 'https://example.com/teacher/home-banner-1', '新用户首单优惠', 100, 1, 0, 0, NOW(), NOW()),
('slide_home_002', 'https://picsum.photos/seed/slide002/1200/500', 'https://example.com/teacher/home-banner-2', '热门老师限时预约', 90, 1, 0, 0, NOW(), NOW()),
('slide_home_003', 'https://picsum.photos/seed/slide003/1200/500', 'https://example.com/teacher/home-banner-3', '家长好评榜单', 80, 1, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  slide_picture = VALUES(slide_picture),
  slide_link = VALUES(slide_link),
  slide_note = VALUES(slide_note),
  slide_priority = VALUES(slide_priority),
  slide_status = 1,
  slide_module = 0,
  slide_delete_status = 0,
  update_time = NOW();

-- 9) 首页广告（source=mini）
INSERT INTO advertising(
  id, advertising_source, advertising_title, advertising_link, advertising_picture,
  advertising_status, advertising_expire_time, advertising_delete_status, create_time, update_time
)
VALUES
('ad_mini_001', 'mini', '春季提分计划', 'https://example.com/teacher/ad-1', 'https://picsum.photos/seed/ad001/1200/400',
 1, NOW() + INTERVAL 180 DAY, 0, NOW(), NOW()),
('ad_mini_002', 'mini', '精品老师推荐', 'https://example.com/teacher/ad-2', 'https://picsum.photos/seed/ad002/1200/400',
 1, NOW() + INTERVAL 180 DAY, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  advertising_title = VALUES(advertising_title),
  advertising_link = VALUES(advertising_link),
  advertising_picture = VALUES(advertising_picture),
  advertising_status = 1,
  advertising_expire_time = VALUES(advertising_expire_time),
  advertising_delete_status = 0,
  update_time = NOW();

COMMIT;

