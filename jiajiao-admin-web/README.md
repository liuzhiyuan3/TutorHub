# 家教管理端（jiajiao-admin-web）

## 本地启动

```bash
npm install
npm run dev
```

默认地址：`http://localhost:5173`  
后端地址：`http://localhost:8080`（在 `src/utils/request.js` 中配置）

## 登录

- 账号：`admin`
- 密码：`123456`

## 已完成页面

- 业务管理：控制台、用户管理、教员审核、需求管理、订单管理
- 内容管理：学科管理、学校管理、区域管理、轮播图管理、广告管理
- 系统管理：角色管理、菜单管理、角色菜单、字典管理（含字典项）
- 统计看板：总览、趋势、区域分布（支持时间范围筛选）

## 统一交互规范

- 统一布局：分组侧边栏 + 顶部标题区 + 内容卡片容器
- 统一组件：状态标签、确认弹窗、详情抽屉
- 统一反馈：请求失败提示、登录过期自动跳转登录页

## 视觉精修规范

- 视觉令牌：统一主色、功能色、阴影、圆角与字体层级（见 `src/style.css`）
- 页面结构：`标题区 + 说明文案 + 筛选面板 + 表格面板 + 操作反馈`
- 表格规范：表头浅色底、行 hover、高危操作按钮高亮、空态统一展示
- 反馈规范：列表加载骨架、请求失败重试、无数据空态提示
- 详情规范：详情统一抽屉展示，确认类操作统一弹窗展示

## Dashboard 图表化模块

- 总览卡片：用户/教员/需求/订单
- 趋势折线图：新增订单 + 新增需求（支持近 7/15/30 天）
- 状态分布饼图：订单状态分布
- 区域柱状图：区域需求分布
- 排行图：学科需求 Top8

## 弹窗字段填写规范

- 新增/编辑统一使用独立弹窗，不再使用行内输入
- 编码类字段（如学科编码、学校编码、角色编码、字典编码）仅允许：大写字母/数字/下划线
- 数字字段（排序、状态、类型、优先级、模块）仅允许数字输入，且最小值为 `0`
- 经纬度字段仅允许数字格式
- 内容管理字段对齐数据库实体：
  - 学科：`subjectName/subjectCode/subjectCategory/subjectDescription/subjectSort/subjectStatus`
  - 学校：`schoolName/schoolCode/schoolType/schoolProvince/schoolCity/schoolDistrict/schoolAddress/schoolLongitude/schoolLatitude/schoolStatus`
  - 区域：`regionName/regionCode/regionCity/regionProvince/regionSort/regionStatus`
- 系统管理字段对齐数据库实体：
  - 角色：`roleName/roleCode/roleDescription`
  - 菜单：`menuName/menuParent/menuPriority/menuLink/menuIcon/menuType`
  - 字典：`dictionaryName/dictionaryCode/dictionaryDescription`
  - 字典项：`dictionaryId/dictionaryContentText/dictionaryContentValue/dictionaryContentSort/dictionaryContentStatus`
  - 轮播图：`slidePicture/slideLink/slideNote/slidePriority/slideStatus/slideModule`
  - 广告：`advertisingSource/advertisingTitle/advertisingLink/advertisingPicture/advertisingStatus/advertisingExpireTime`

## 联调验收清单

- [x] 用户状态启停、删除可成功回刷列表
- [x] 教员审核支持通过/拒绝，拒绝可填写原因
- [x] 需求支持状态筛选、审核流转、详情查看
- [x] 订单支持状态迁移、审核操作、详情查看
- [ ] 学科/学校/区域支持新增、编辑、删除
- [ ] 角色/菜单/角色菜单支持基础配置闭环
- [ ] 字典支持字典与字典项联动维护
- [ ] 轮播图/广告支持新增、编辑、删除、状态展示
- [x] 控制台支持总览卡片 + 五类图表（折线/饼图/柱状/排行）
- [x] 统计看板可切换近 7/15/30 天并刷新数据
- [x] `npm run build` 打包通过
