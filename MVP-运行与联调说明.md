# 家教平台 MVP 运行与联调说明

## 1. 环境准备
- MySQL 8.x（默认库名：`teacher_service`）
- Java 17+（必须是 JDK，不是 JRE）
- Node.js 18+
- 微信开发者工具（原生小程序）

## 2. 数据库初始化
1. 执行建表脚本：`teacher-student/src/main/resources/sql/schema.sql`
2. 执行种子脚本：`teacher-student/src/main/resources/sql/seed.sql`
3. 可选查询示例：`teacher-student/src/main/resources/sql/queries.sql`

## 3. 后端启动（Spring Boot + MyBatis-Plus）
1. 检查配置文件：`teacher-student/src/main/resources/application.yml`
2. 启动命令（在 `teacher-student` 目录）：
   - `mvn spring-boot:run`
   - 或 `./mvnw.cmd spring-boot:run`
3. 默认地址：`http://localhost:8080`

### 3.1 核心接口清单
- 鉴权
  - `POST /api/auth/admin/login`
  - `POST /api/auth/user/login`
  - `POST /api/auth/register`
- 用户与教员
  - `GET /api/user/me`
  - `PUT /api/user/me`
  - `POST /api/teacher/profile`
  - `GET /api/teacher/profile/me`
  - `GET /api/teacher/page`
- 需求与接单
  - `POST /api/requirement`
  - `GET /api/requirement/page`
  - `GET /api/requirement/my/page`
  - `POST /api/order/receive/{requirementId}`
- 订单
  - `GET /api/order/my/page`
  - `PUT /api/order/{id}/status`
- 管理端
  - `GET /api/admin/users/page`
  - `GET /api/admin/teachers/page`
  - `PUT /api/admin/teachers/{id}/audit`
  - `GET /api/admin/requirements/page`
  - `GET /api/admin/orders/page`
  - `PUT /api/admin/orders/{id}/status`
  - `GET /api/admin/content/subjects/page`
  - `POST /api/admin/content/subjects`
  - `GET /api/admin/content/schools/page`
  - `POST /api/admin/content/schools`
  - `GET /api/admin/content/regions/page`
  - `POST /api/admin/content/regions`

## 4. 管理端启动（Vue3 + Vite）
1. 进入目录：`jiajiao-admin-web`
2. 安装依赖：`npm install`
3. 开发启动：`npm run dev`
4. 生产构建：`npm run build`
5. 登录账号（seed）：`admin / 123456`

## 5. 原生微信小程序启动
1. 微信开发者工具导入目录：`jiajiao-mini`
2. 使用 `project.config.json` 默认配置打开
3. 修改 `app.js` 里的后端地址（若不是本机 `localhost`）
4. 首次使用可在登录页点击“没有账号？先注册”，自动创建演示用户

## 6. 联调主流程（验收）
1. 家长登录 -> 发布需求
2. 教员登录 -> 完善教员资料 -> 管理端审核通过
3. 教员在“接单列表”接单 -> 生成订单
4. 家长/教员在“我的订单”查看并更新订单状态
5. 管理端查看用户、审核、需求、订单、学科/学校/区域数据

## 7. 本地验证结果与注意事项
- 管理端 `npm run build` 已通过。
- 后端使用 `mvnw test` 编译时提示当前环境为 JRE（无 Java 编译器），需安装 JDK 并配置 `JAVA_HOME`。
- 你机器的全局 Maven `settings.xml` 有标签拼写错误（`mirrorof` 结束标签写成了 `mirror0f`），建议修复后再用系统 Maven。
