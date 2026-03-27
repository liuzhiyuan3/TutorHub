# PROJECT_CONTEXT.md

## 1. 项目定位

Teacher 是一个家教撮合平台，包含 C 端（家长/老师）和管理端，核心目标是把“需求发布 -> 匹配教师 -> 成交履约 -> 运营管理”流程打通。

## 2. 产品端与职责

- `mp-weixin-jiajiao`（微信小程序）
  - 面向家长与老师
  - 家长侧：浏览老师、发布需求、下单与跟进
  - 老师侧：完善资料、接单与履约
- `jiajiao-admin-web`（管理后台）
  - 面向运营/审核/管理员
  - 用户、教师、需求、订单、内容与系统配置管理
- `teacher-backend`（后端服务）
  - 提供统一 API、鉴权、业务流程与数据持久化

## 3. 当前技术栈

- 小程序：原生微信小程序 + `@vant/weapp`
- 后台前端：Vue 3 + Vite + Axios + Vue Router
- 后端：Spring Boot 3 + MyBatis Plus + JWT + MySQL

## 4. 业务主流程（统一心智模型）

1. 用户注册/登录（家长、老师、管理员）
2. 家长发布家教需求
3. 平台派单/老师接单
4. 订单状态流转（创建、进行中、完成/取消等）
5. 管理端审核与内容治理（教师审核、需求审核、字典/轮播/广告等）
6. 统计看板与运营分析

## 5. 核心业务对象（通用术语）

- 用户（User）：平台账号主体
- 教师（Teacher）：可提供授课服务的用户
- 需求（Requirement）：家长发布的家教需求
- 订单（Order）：家长与老师达成的服务单
- 派单记录（DispatchRecord）：需求与老师之间的匹配过程记录
- 收藏（FavoriteTeacher）：家长收藏老师关系
- 系统字典（Dictionary/DictionaryContent）：可配置选项
- 轮播/广告（Slide/Advertising）：运营位内容

## 6. 状态与权限（约定）

- 鉴权方式：`Authorization: Bearer <token>`
- 管理端 token 存储：`localStorage.admin_token`
- 常见角色：
  - 家长用户
  - 老师用户
  - 管理员（Admin）
- 任何新增接口都应明确：
  - 谁可访问
  - 未登录/越权时返回策略

## 7. 后端统一响应约定

后端使用统一响应结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code = 0` 表示成功
- `code != 0` 表示失败（前端按 message 展示）

## 8. 环境与配置约束

- 默认服务端口：`8080`
- 配置优先环境变量，不在仓库提交真实密钥
- 敏感字段示例：
  - `WECHAT_APP_SECRET`
  - `OSS_ACCESS_KEY_SECRET`
  - `QQ_MAP_KEY`

## 9. 开发协作原则

- 最小改动原则：只改与任务直接相关文件
- 契约优先原则：后端字段变更需同步检查前端
- 可验证原则：每次提交都要可复现验证
- 可回滚原则：避免大而杂的混合改动

## 10. 新对话快速上下文模板

每次开新对话，先提供以下信息：

```text
项目：Teacher
本次目标：<一句话>
目标模块：<mp-weixin-jiajiao / jiajiao-admin-web / teacher-backend>
限制条件：<是否允许重构、是否要求兼容旧接口、完成时限>
验收标准：<比如：某页面可用、某接口返回正确、构建通过>
```

