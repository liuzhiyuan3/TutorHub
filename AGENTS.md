# AGENTS.md

项目：Teacher（家教平台多端项目）  
目标：让任意 AI 编码代理在新对话中快速理解项目、稳定改动、避免误操作。

## 核心项目范围

- 以下 3 个目录是核心目录，结构默认不可变：
  - `mp-weixin-jiajiao`（微信小程序端）
  - `jiajiao-admin-web`（管理后台前端，Vue3 + Vite）
  - `teacher-backend`（Java 后端，Spring Boot 多模块）
- 未经用户明确同意，不允许删除或重命名以上 3 个目录。
- `jiajiao-mini` 已废弃，如再次出现可在确认后清理。
-注意，每次下载依赖和下载相关的操作是，询问我是否下载到那个盘上

## 架构速览

- 小程序端：
  - `mp-weixin-jiajiao/app.json`：全局页面注册与路由
  - `mp-weixin-jiajiao/pages/**`：页面
  - `mp-weixin-jiajiao/utils/**`：请求与工具
- 管理后台：
  - `jiajiao-admin-web/src/views/**`：页面
  - `jiajiao-admin-web/src/api/**`：API 封装
  - `jiajiao-admin-web/src/router/**`：路由
  - `jiajiao-admin-web/src/components/**`：复用组件
- 后端：
  - `teacher-backend/teacher-server`：controller/service/mapper + 启动模块
  - `teacher-backend/teacher-pojo`：DTO/VO/Entity
  - `teacher-backend/teacher-common`：通用响应、异常、鉴权

## 开发环境提示

- 先确认工具链：
  - Node.js + npm
  - Java 17
  - Maven 3.9+
  - Git（`git --version` 可用）
- 采用最小扫描策略：
  - 先读关键入口：`README.md`、`package.json`、`pom.xml`、`app.json`
  - 再读取与任务直接相关文件
- 搜索优先使用 `rg`：
  - `rg "关键词" 路径`
  - `rg --files 路径`

## 运行与构建命令

- 管理后台：
  - `cd jiajiao-admin-web`
  - `npm install`
  - `npm run dev`
  - `npm run build`
- 小程序端：
  - `cd mp-weixin-jiajiao`
  - `npm install`
  - 使用微信开发者工具打开并调试
- 后端：
  - `cd teacher-backend`
  - `mvn clean test`
  - `mvn -pl teacher-server spring-boot:run`

## 测试与验收要求

- 后端接口变更：
  - 至少执行一次 `mvn test`
  - 验证状态码与响应体结构
- 后台前端变更：
  - 执行 `npm run build`
  - 验证路由跳转与 loading/empty/error 状态
- 小程序变更：
  - 验证页面已在 `app.json` 注册
  - 在微信开发者工具验证核心流程
- 若无法执行测试，必须说明原因和未覆盖风险。

## UI 与前端设计规则

- 总体要求：
  - 禁止生成“通用 AI 模板感”界面。
  - 风格需与现有产品语气一致。
  - 优先保证信息层级和可读性。
- 设计令牌（Token）：
  - 优先复用或定义颜色、间距、圆角、阴影、字体令牌。
  - 避免无依据的硬编码样式值。
- 字体与排版：
  - 明确标题/副标题/正文/辅助信息层级。
  - 保持合理行高和对比度。
- 布局：
  - 统一间距节奏（如 4/8 体系或项目既有体系）。
  - 保证桌面端与移动端都可正常展示。
- 组件：
  - 优先复用现有组件；必要时再抽新组件。
  - 状态完整：默认、悬停、激活、禁用、加载、空态、错误态。
- 动效：
  - 仅使用轻量且有意义的动效。
  - 避免炫技型或干扰型动画。
- 可访问性：
  - 保证文本对比度和点击区域可用性。
  - 不仅用颜色表达关键信息。

## 接口与数据契约规则

- 后端字段新增/删除/改名时，必须联查：
  - `jiajiao-admin-web` 的调用点
  - `mp-weixin-jiajiao` 的调用点
- 保持响应包结构一致（按项目约定）。
- 分页/筛选接口至少验证以下边界：
  - 空结果
  - 超大页码
  - 非法参数

## 安全与配置规则

- 严禁提交真实密钥：
  - `WECHAT_APP_SECRET`
  - `OSS_ACCESS_KEY_SECRET`
  - `QQ_MAP_KEY`
- 后端配置优先使用环境变量注入。
- 前端与小程序中禁止硬编码 token/secret。

## 变更策略

- 优先修复根因，不做表面补丁。
- 改动保持小而聚焦，不夹带无关重构。
- 不覆盖用户已有且未要求回退的改动。
- 未经明确授权，禁止执行破坏性命令（如 `git reset --hard`、批量删除）。

## 文件删除策略

- 允许删除：
  - 明确废弃且无引用关系的文件/目录
  - 可安全清理的构建产物与临时文件
- 禁止删除（除非用户明确要求）：
  - 三大核心目录及业务关键源码
- 删除前流程：
  1. 先确认“无引用”
  2. 再执行删除
  3. 最后输出删除清单

## 交付与汇报规范

- 每次完成后必须包含：
  - 做了什么
  - 为什么这样改
  - 修改了哪些文件
  - 如何验证
  - 剩余风险
- 建议标题格式：
  - `[模块] 简短描述`
  - 例如：`[admin-web] 修复仪表盘统计为空时崩溃`

## 新对话启动机制（重点）

- 目标：让代理每次新开对话都能快速进入项目上下文。
- 新对话时，用户应提供：
  - 本次目标（一句话）
  - 目标模块（小程序/后台/后端）
  - 约束条件（如：不重构、接口兼容、上线时限）
- 代理首轮必须执行：
  1. 阅读本 `AGENTS.md`
  2. 阅读模块入口文件：
     - 小程序：`app.json`
     - 后台：`package.json`、`src/router/index.js`
     - 后端：根 `pom.xml`、`teacher-server/src/main/resources/application.yml`
  3. 在改动前输出“理解确认”：
     - 目标理解
     - 预计改动文件
     - 验证计划

## 每次新对话推荐提示词![](image.png)
```text
请先阅读仓库根目录AGENTS.md、PROJECT_CONTEXT.md、API_CONTRACTS.md、UI_GUIDELINES.md，，并严格按其中规则执行。
本次目标：<一句话目标>
目标模块：<mp-weixin-jiajiao / jiajiao-admin-web / teacher-backend>
约束条件：
完成后请给我：改动文件清单、验证步骤、风险点。
```

