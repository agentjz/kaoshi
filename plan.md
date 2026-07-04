# kaoshi 纯前端演示运行时计划

## 需求

- 构建一个 GitHub Pages 可托管的纯前端演示版。
- 演示版和正式版必须使用同一套 Vue 页面、组件、路由、类型和视觉样式，不能维护两套页面。
- 演示版默认不写 `localStorage`，刷新页面恢复初始演示数据。
- 演示版不访问真实后端、MySQL 或 Redis，但要能体验当前现行有效核心能力：登录、管理端导航、用户/角色/部门、题库、考试、在线作答、提交、成绩和阅卷核心链路。
- 正式版现有后端 API 行为、测试和构建不能被破坏。

## 当前事实

- 当前前端 API 函数集中在 `frontend/src/api/auth.ts`、`admin.ts` 和 `exam-business.ts`，页面通过这些函数访问后端。
- 当前 `apiClient` 使用 axios 请求 `/api/...`，请求拦截器从 `auth` store 读取 token。
- 当前 `auth` store 直接读取和写入 `localStorage` 的 `kaoshi.accessToken` 与 `kaoshi.currentUser`。
- 当前路由守卫只依赖 `auth` store 的 token、user 和 `loadCurrentUser()`。
- 当前前端已有 CET4 本地音频资源 `frontend/public/local-assets/cet4/2023-03/set-1/2023-03-cet4-listening.mp3`。
- 当前 `package.json` 只有正式 `build`，没有 demo 构建脚本。
- 当前工作区在本计划创建前是干净的。

## 生产级验收保护点

- 正式模式继续走真实 HTTP API，不改变接口契约。
- demo 模式所有数据在内存中维护，不写 `localStorage`，不请求 `/api`。
- 页面不分叉，不新增平行 demo 页面。
- adapter 边界清楚：页面不判断 demo/real，业务 API 模块统一委派。
- demo seed 是可维护的结构化数据，不把大量演示逻辑塞进页面。
- demo 能跑真实浏览器验收，至少覆盖登录、题库查看、在线考试、提交、成绩查看和管理员阅卷。
- 文件职责审查：超过 300 行必须确认职责是否单一；状态、规则、数据读写和渲染展示不能混在页面里。

## 目标

- 增加运行时模式判断：`real` 与 `demo`。
- 增加 auth storage adapter：real 使用 `localStorage`，demo 使用内存 storage。
- 增加 API runtime adapter：real 调 axios，demo 调内存服务。
- 增加 demo seed 与内存数据库，覆盖 admin、张三、部门、角色、权限、题库、CET4 试题、考试、attempt、成绩。
- 增加 demo 构建脚本和 GitHub Pages 部署配置。
- 增加 demo Playwright 验收。
- 同步 README/spec 中已经实现的 demo 事实。

## 不做范围

- 不把真实后端搬到 GitHub Pages。
- 不在 demo 默认使用 `localStorage`、IndexedDB 或远程存储。
- 不做多租户、分享链接持久化或云端保存。
- 不复制一套 demo 页面。
- 不承诺 demo 导入导出能完整解析 Excel/zip；demo 中上传/下载只需提供可操作的演示反馈和轻量文件结果。

## 设计

- 新增 `frontend/src/runtime/app-mode.ts`：集中读取 `VITE_APP_MODE`，默认 `real`。
- 新增 `frontend/src/runtime/session-storage.ts`：封装 token 和 current user 读写；real 使用 `localStorage`，demo 使用模块级内存。
- 新增 `frontend/src/api/adapters/`：
  - `auth-adapter.ts`、`admin-adapter.ts`、`exam-business-adapter.ts` 定义接口。
  - `real/` 复用 axios 调用。
  - `demo/` 使用内存 store 实现同一接口。
- 现有 `auth.ts`、`admin.ts`、`exam-business.ts` 保持导出函数名不变，只委派到当前 adapter。
- demo 内存数据拆分：
  - `demo-seed.ts` 只保存初始数据构造。
  - `demo-store.ts` 只管理内存状态、ID 生成和快照克隆。
  - `demo-auth.ts`、`demo-admin.ts`、`demo-exam-business.ts` 分别实现业务 API。
  - `demo-scoring.ts` 负责作答评分和阅卷状态计算。
- GitHub Pages 使用 GitHub Actions 构建 `frontend` 的 demo dist 并部署。

## 实施任务

- [x] 创建运行模式和 session storage adapter。
- [x] 抽出 real API adapter，保持正式 API 行为不变。
- [x] 实现 demo 内存 seed、store 和核心业务 API。
- [x] 接入现有 API 模块，确保页面无需感知 demo/real。
- [x] 增加 demo build、preview 和 Playwright 配置/脚本。
- [x] 增加 GitHub Pages workflow。
- [x] 同步 README/spec 当前事实。
- [x] 运行类型检查、单测、正式构建、demo 构建、正式浏览器验收和 demo 浏览器验收。
- [x] 收口更新本计划状态和验证结果。

## 验证计划

- `cd frontend; npm.cmd run typecheck`
- `cd frontend; npm.cmd run test:unit`
- `cd frontend; npm.cmd run build`
- `cd frontend; npm.cmd run build:demo`
- `python .\start_test.py`
- `python .\start_browser_test.py`
- 新增 demo 浏览器验收脚本后运行对应命令。

## 收口

- 已完成：同源前端 runtime adapter、demo 内存后端、demo session 内存存储、GitHub Pages 构建、demo 浏览器验收和正式模式回归。
- 职责审查：`demo-store.ts` 与 `exam-business-demo-adapter.ts` 超过 300 行，触发审查；当前变化原因分别是“demo 内存状态/seed 构造”和“exam-business API 的 demo adapter 实现”，不包含页面渲染逻辑。后续如果 demo 业务继续扩展，应优先拆成 question-bank、exam、result 三个 demo service。
- 验证通过：
  - `cd frontend; npm.cmd run typecheck`
  - `cd frontend; npm.cmd run build`
  - `cd frontend; npm.cmd run build:demo`
  - `cd frontend; npm.cmd run test:e2e:demo`
  - `python .\start_test.py`
  - `python .\start_browser_test.py`
- 剩余风险：demo adapter 是内存模拟后端，文件导入/导出提供可操作演示和轻量文本/JSON结果，不等价于真实后端的 Excel/zip 完整解析。
