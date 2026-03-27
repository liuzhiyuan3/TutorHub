# API_CONTRACTS.md

## 1. 文档目的

本文件定义前后端联调时的“稳定契约”，用于减少接口变更导致的回归问题。  
原则：先改文档再改代码；接口变更必须同步更新本文件。

## 2. 全局约定

### 2.1 Base URL

- 本地默认：`http://localhost:8080`
- 管理后台可通过 `VITE_API_BASE_URL` 覆盖
- 小程序从 `app.globalData.baseUrl` 读取，默认 `http://localhost:8080`

### 2.2 认证

- Header：`Authorization: Bearer <token>`
- 管理后台：
  - 请求拦截器自动注入 `admin_token`
  - `401` 时清空 token 并跳转登录页
- 小程序：
  - 支持 `required/optional` 鉴权模式
  - `401` 时根据模式触发重新授权或降级匿名

### 2.3 统一响应

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- 成功：`code = 0`
- 失败：`code != 0`，`message` 必须可读
- 前端只消费 `data`，错误通过 `message` 抛出

### 2.4 分页建议约定

如接口为分页接口，建议统一：

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "pageSize": 10
}
```

若历史接口结构不一致，需在接口注释中明确实际结构。

## 3. 管理端接口分组（按当前代码）

以下来自 `jiajiao-admin-web/src/api/index.js` 与后端 controller 结构，作为联调基线。

### 3.1 认证

- `POST /api/auth/admin/login`
- `GET /api/auth/admin/me`

### 3.2 用户管理

- `GET /api/admin/users/page`
- `GET /api/admin/users/{id}/profile`
- `PUT /api/admin/users/{id}/status`
- `DELETE /api/admin/users/{id}`

### 3.3 教师审核

- `GET /api/admin/teachers/page`
- `GET /api/admin/teachers/{id}/profile`
- `PUT /api/admin/teachers/{id}/audit`

### 3.4 需求管理

- `GET /api/admin/requirements/page`
- `PUT /api/admin/requirements/{id}/audit`

### 3.5 订单管理

- `GET /api/admin/orders/page`
- `PUT /api/admin/orders/{id}/status`
- `PUT /api/admin/orders/{id}/audit`

### 3.6 内容管理

- 学科：
  - `GET /api/admin/content/subjects/page`
  - `POST /api/admin/content/subjects`
  - `DELETE /api/admin/content/subjects/{id}`
- 学科分类：
  - `GET /api/admin/content/subject-categories/page`
  - `POST /api/admin/content/subject-categories`
  - `DELETE /api/admin/content/subject-categories/{id}`
- 学校：
  - `GET /api/admin/content/schools/page`
  - `POST /api/admin/content/schools`
  - `DELETE /api/admin/content/schools/{id}`
- 区域：
  - `GET /api/admin/content/regions/page`
  - `POST /api/admin/content/regions`
  - `DELETE /api/admin/content/regions/{id}`

### 3.7 系统管理

- 角色：
  - `GET /api/admin/system/roles/page`
  - `POST /api/admin/system/roles`
  - `DELETE /api/admin/system/roles/{id}`
- 菜单：
  - `GET /api/admin/system/menus/page`
  - `POST /api/admin/system/menus`
  - `DELETE /api/admin/system/menus/{id}`
- 角色菜单：
  - `GET /api/admin/system/role-menus/page`
  - `POST /api/admin/system/role-menus`
  - `DELETE /api/admin/system/role-menus/{id}`
- 字典：
  - `GET /api/admin/system/dictionary/page`
  - `POST /api/admin/system/dictionary`
  - `DELETE /api/admin/system/dictionary/{id}`
- 字典项：
  - `GET /api/admin/system/dictionary-content/page`
  - `POST /api/admin/system/dictionary-content`
  - `DELETE /api/admin/system/dictionary-content/{id}`
- 轮播：
  - `GET /api/admin/system/slides/page`
  - `POST /api/admin/system/slides`
  - `DELETE /api/admin/system/slides/{id}`
- 广告：
  - `GET /api/admin/system/advertising/page`
  - `POST /api/admin/system/advertising`
  - `DELETE /api/admin/system/advertising/{id}`

### 3.8 文件上传

- `POST /api/file/upload?biz=<biz>`
- Content-Type：`multipart/form-data`
- 文件字段名：`file`

### 3.9 统计

- `GET /api/admin/stats/overview`
- `GET /api/admin/stats/business`
- `GET /api/admin/stats/trend`
- `GET /api/home/filters`

## 4. 小程序相关后端域（按 controller）

后端存在以下业务域 controller（用于补全契约）：

- `AuthController`
- `UserController`
- `TeacherController`
- `TeacherTeachingController`
- `RequirementController`
- `OrderController`
- `AppointmentController`
- `DispatchRecordController`
- `FavoriteTeacherController`
- `ContentController`
- `HomeController`
- `LocationController`
- `MockPayController`
- `FileController`

建议后续把这些域按“请求/响应示例”补全到本文件。

## 5. 变更流程（强制）

接口变更（新增/删除/字段修改）时，必须同步做以下动作：

1. 更新本文件对应接口定义
2. 更新调用方（admin-web / mini program）
3. 更新最小联调用例
4. 在提交说明中标记“契约变更”

## 6. 向后兼容策略

- 非必要不删除字段，只做“新增可选字段”
- 字段重命名优先采用双字段过渡（旧+新）
- 删除接口前至少经历一个版本的废弃提示
- 错误码语义保持稳定，不随意复用

## 7. 联调验收清单

- 鉴权通过与 401 行为符合预期
- 成功/失败响应结构符合统一约定
- 分页接口在空数据与越界页码时表现稳定
- 前端页面对 loading、empty、error 状态处理正常
- 日志中无明显后端异常堆栈

