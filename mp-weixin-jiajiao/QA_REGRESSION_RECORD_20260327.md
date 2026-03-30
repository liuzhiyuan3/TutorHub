# 小程序定位链路端到端回归记录（第4轮）

- 回归版本：`a368fd5`（工作区含未提交改动）
- 回归时间：`2026-03-27 21:35:50`
- 回归范围：定位链路 + 核心流程关键检查
- 回归方式：代码路径核对 + 构建验证 + 清单打钩留档

## 清单执行结果

### 2. 鉴权与登录
- [x] token 失效后，出现重新登录引导，不死循环。
  证据：`mp-weixin-jiajiao/utils/request.js` 401 分支会清理登录态并触发登录引导。
- [ ] 游客/家长/教员实际登录跳转行为（需微信开发者工具人工回归）。

### 3. 发布需求（家长）
- [x] 必填与范围校验生效（标题/描述/年级/地址/薪资）。
  证据：`pages/requirement-create/index.js` `validate()`。
- [x] 上传中禁提交、提交中禁重复点击。
  证据：`pages/requirement-create/index.wxml` 按钮 `disabled` 绑定。
- [x] 定位失败有可执行引导（去设置/手动选点）。
  证据：`pages/requirement-create/index.js` `handleLocationFailure()`。
- [x] 定位状态可见（成功/失败状态行）。
  证据：`pages/requirement-create/index.wxml/.wxss` `location-status`。
- [ ] 真机发布需求后跳转并看到新记录（需人工验证）。

### 4. 需求广场与接单（教员）
- [x] 资料未通过/未完善时，接单拦截与引导存在。
  证据：`pages/requirements/index.js` `receiveByPayload()`。
- [ ] 真实账号接单全流程（需人工验证）。

### 5. 订单流转
- [x] 合并池阶段文案中文映射存在且正确。
  证据：后端 `OrderService.resolveParentStageText` 与小程序 `pages/orders/index.js` 文案映射。
- [ ] 教员推进/家长取消/轨迹页真实链路（需人工验证）。

### 6. 页面状态与体验
- [x] loading/empty/error/按钮状态代码路径存在。
  证据：需求页、订单页、发布页均有对应分支。
- [ ] 多机型可用性与触控体验（需人工验证）。

## 自动化验证

- [x] 后端编译：`mvn -pl teacher-server -am -DskipTests compile` 通过。
- [x] 后台构建：`npm run build` 通过。
- [x] 回归清单条目存在：`QA_REGRESSION_CHECKLIST.md` 已包含“定位失败可执行引导”。

## 第6轮补充（echarts 按需加载优化）

- [x] 图表改为动态加载，未进入看板页不加载图表库。
  证据：`jiajiao-admin-web/src/components/admin/ChartPanel.vue` 采用 `import('../../utils/echarts-lite')`。
- [x] 图表库按模块裁剪（line/bar/pie + 必需组件）。
  证据：`jiajiao-admin-web/src/utils/echarts-lite.js`。
- [x] 构建结果对比：`echarts` 包体从约 `1127.31 kB` 降至 `547.88 kB`（仍高于 500k 警戒线）。

## 问题汇总

- 阻断问题：0（基于代码与构建结果）
- 主要问题：1（缺少微信开发者工具的真机端到端实操结果）

## 结论

- 当前结论：**条件通过（Conditional Pass）**
- 放行条件：按清单补齐人工回归项（登录、发布、接单、订单流转）。

---

## 第9轮补充（界面简洁化三轮中的小程序发布页）

- [x] 发布页信息分组更清晰（基础信息/教学偏好/快捷薪资/教员要求）。
  证据：`pages/requirement-create/index.wxml` 新增 `group-title` 分组标题。
- [x] 定位模块提示更明确（状态 + 引导文案）。
  证据：`pages/requirement-create/index.wxml/.wxss` 的 `location-row`、`location-status`、`field-note`。
- [x] 表单视觉节奏更统一（边框、间距、轻量背景）。
  证据：`pages/requirement-create/index.wxss` 的 `van-field-lite`、`salary-wrap`、`sheet-tip`。

### 本轮清单打钩（与 UI 相关项）

### 3. 发布需求（家长）
- [x] 必填项校验仍生效（未改动校验逻辑，仅调整样式与分组）。
- [x] 定位失败可执行引导仍保留（未改动 `handleLocationFailure` 分支）。
- [x] 上传中禁提交状态仍保留（提交按钮 `disabled` 绑定未改动）。

### 6. 页面状态与体验
- [x] loading/empty/error/按钮状态相关节点未删除，交互仍可见。
- [x] 关键流程可操作区域更清晰，定位提示更易读。
