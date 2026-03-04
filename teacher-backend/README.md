# 家教后端（重写版）

本工程按三模块拆分：

- `teacher-common`：通用返回体、异常、分页、JWT、安全上下文
- `teacher-pojo`：实体、DTO、VO
- `teacher-server`：Controller、Service、Mapper、配置、SQL 脚本

技术栈：

- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- MySQL 8.x
- Java 17

## 1. IDEA 导入方式

1. 在 IDEA 中打开目录：`teacher-backend`
2. 等待 Maven 自动导入完成
3. 运行主类：`com.teacher.server.TeacherServerApplication`

## 2. 数据库初始化

数据库默认：`teacher_service`

执行顺序：

1. `teacher-server/src/main/resources/sql/schema.sql`
2. `teacher-server/src/main/resources/sql/seed.sql`

默认管理员账号（seed）：

- 账号：`admin`
- 密码：`123456`

## 3. 配置说明

配置文件：`teacher-server/src/main/resources/application.yml`

默认连接：

- url: `jdbc:mysql://localhost:3306/teacher_service...`
- username: `root`
- password: `root`

如本地不一致，请修改后再启动。

## 4. 启动命令

在 `teacher-backend` 目录执行：

```bash
mvn clean package -DskipTests
```

单独启动服务模块：

```bash
mvn -pl teacher-server spring-boot:run
```

## 5. 接口分组

- 鉴权：`/api/auth/**`
- 应用端：`/api/user/**`、`/api/teacher/**`、`/api/requirement/**`、`/api/order/**`
- 扩展业务：`/api/appointment/**`、`/api/favorite/**`、`/api/dispatch/**`
- 管理端：`/api/admin/**`

## 6. 安全策略

- JWT 从 `Authorization: Bearer <token>` 解析
- `JwtAuthenticationFilter` 负责注入登录上下文
- `AdminAuthInterceptor` 强制限制 `/api/admin/**` 只能管理员访问

## 7. 已完成范围

- 全量核心数据表模型与 Mapper
- 应用端核心链路（注册登录、用户中心、教员资料、需求、接单、订单）
- 管理端核心链路（用户、教员审核、需求、订单、内容管理）
- 系统管理扩展（角色、菜单、角色菜单、字典、轮播图、广告、统计）
