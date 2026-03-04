# API 兼容映射（旧 `teacher-student` -> 新 `teacher-backend`）

本次重写保持了原有主路径风格，旧前端联调可最小改动接入。

## 1. 鉴权接口（兼容）

- `POST /api/auth/user/login` -> 保持不变
- `POST /api/auth/admin/login` -> 保持不变
- `POST /api/auth/register` -> 保持不变

## 2. 应用端核心（兼容）

- `GET /api/user/me` -> 保持不变
- `PUT /api/user/me` -> 保持不变
- `POST /api/teacher/profile` -> 保持不变
- `GET /api/teacher/profile/me` -> 保持不变
- `GET /api/teacher/page` -> 保持不变
- `POST /api/requirement` -> 保持不变
- `GET /api/requirement/page` -> 保持不变
- `GET /api/requirement/my/page` -> 保持不变
- `GET /api/requirement/{id}` -> 保持不变
- `POST /api/order/receive/{requirementId}` -> 保持不变
- `GET /api/order/my/page` -> 保持不变
- `PUT /api/order/{id}/status` -> 保持不变
- `GET /api/content/subjects` -> 保持不变
- `GET /api/content/schools` -> 保持不变
- `GET /api/content/regions` -> 保持不变

## 3. 管理端核心（兼容）

- `GET /api/admin/users/page` -> 保持不变
- `GET /api/admin/teachers/page` -> 保持不变
- `PUT /api/admin/teachers/{id}/audit` -> 保持不变（请求体改为 `AuditRequest`，含 `auditStatus/reason`）
- `GET /api/admin/requirements/page` -> 保持不变
- `GET /api/admin/orders/page` -> 保持不变
- `PUT /api/admin/orders/{id}/status` -> 保持不变
- `GET /api/admin/content/subjects/page` -> 保持不变
- `POST /api/admin/content/subjects` -> 保持不变
- `GET /api/admin/content/schools/page` -> 保持不变
- `POST /api/admin/content/schools` -> 保持不变
- `GET /api/admin/content/regions/page` -> 保持不变
- `POST /api/admin/content/regions` -> 保持不变

## 4. 新增扩展接口（全量重写补充）

- 预约：`/api/appointment/**`
- 备选老师：`/api/favorite/**`
- 派单记录：`/api/dispatch/**`
- 教学信息关联：`/api/teacher/teaching/**`
- 系统管理：`/api/admin/system/**`（角色、菜单、字典、轮播图、广告）
- 统计：`/api/admin/stats/overview`
- 管理端业务扩展：`/api/admin/business/**`

## 5. 迁移建议

- 现有小程序和管理端可先按“兼容接口”直接联调
- 再逐步接入新增扩展接口实现全量业务
