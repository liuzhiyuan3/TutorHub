# API 契约与状态字典

## 1. 通用返回

- 成功：`{ "code": 0, "message": "success", "data": ... }`
- 失败：`{ "code": -1, "message": "错误信息", "data": null }`

## 2. 核心状态枚举

### 需求状态 `requirementStatus`
- `0` 待接单
- `1` 已接单
- `2` 已完成
- `3` 已取消

允许迁移：
- `0 -> 1/3`
- `1 -> 2/3`

### 订单状态 `orderStatus`
- `0` 待确认
- `1` 进行中
- `2` 已完成
- `3` 已取消

允许迁移：
- `0 -> 1/3`
- `1 -> 2/3`

### 审核状态 `auditStatus`
- `0` 待审核
- `1` 审核通过
- `2` 审核拒绝

### 预约状态 `appointmentStatus`
- `0` 待确认
- `1` 已确认
- `2` 已拒绝
- `3` 已取消

允许迁移：
- `0 -> 1/2/3`

## 3. 关键接口（新增/强化）

### 需求
- `PUT /api/requirement/{id}/cancel`：家长取消需求
- `PUT /api/admin/requirements/{id}/audit`：管理员审核需求

### 订单
- `PUT /api/admin/orders/{id}/audit`：管理员审核订单
- `PUT /api/order/{id}/status`：按状态机更新订单状态
- `PUT /api/admin/orders/{id}/status`：管理员更新订单状态（保留兼容）

### 预约/收藏
- `PUT /api/appointment/{id}/status?status=`：预约状态流转
- `DELETE /api/favorite/{teacherId}`：移除备选老师

### 系统管理 CRUD
- `DELETE /api/admin/system/roles/{id}`
- `DELETE /api/admin/system/menus/{id}`
- `DELETE /api/admin/system/role-menus/{id}`
- `DELETE /api/admin/system/dictionary/{id}`
- `DELETE /api/admin/system/dictionary-content/{id}`
- `DELETE /api/admin/system/slides/{id}`
- `DELETE /api/admin/system/advertising/{id}`

### 内容管理 CRUD
- `DELETE /api/admin/content/subjects/{id}`
- `DELETE /api/admin/content/schools/{id}`
- `DELETE /api/admin/content/regions/{id}`

### 首页聚合与检索
- `GET /api/home/overview`：首页聚合数据
- `GET /api/home/filters`：筛选元数据（学科/区域/学校）
- `GET /api/home/teachers/search`：教员组合检索（VO）
  - 参数：`subjectId, regionId, tutoringMethod, auditStatus, keyword, schoolKeyword, minTeachingYears, maxTeachingYears, userLat, userLng, maxDistanceKm, sortBy(hot/latest/success/distance), pageNo, pageSize`
- `GET /api/home/requirements/search`：需求组合检索（VO）
  - 参数：`subjectId, regionId, tutoringMethod, minSalary, maxSalary, gradeKeyword, keyword, sortBy(latest/salaryAsc/salaryDesc), pageNo, pageSize`

### 教员详情（公开端）
- `GET /api/teacher/{id}`：教员聚合详情（VO）
  - 返回：基础信息 + 教学信息 + 科目/区域 + 成功记录（最近20条）
  - 副作用：详情读取会累计 `teacher_view_count`

### C端运营内容
- `GET /api/content/slides?module=`：读取已启用轮播图（按优先级倒序）
- `GET /api/content/advertising?source=`：读取已启用且未过期广告
- `GET /api/content/subjects`：公开学科选项（轻量VO）
- `GET /api/content/regions`：公开区域选项（轻量VO）
- `GET /api/content/schools`：公开学校选项（轻量VO）

### 统计
- `GET /api/admin/stats/trend?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

## 4. 权限约定

- `/api/admin/**`：仅管理员（拦截器强制）
- 普通业务接口：登录用户按角色（家长/教员）做二次业务鉴权

## 5. 常见错误语义

- `无权操作该资源`
- `非法状态迁移`
- `数据不存在或已删除`
- `审核未通过，不可继续流转`
- `教龄区间非法`
- `薪资区间非法`
