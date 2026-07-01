# kaoshi 浅色体验与浏览器验收 Plan

## 1. 需求文档

owner 已将 GitHub 远端仓库改名为“考试”。当前任务把项目内部英文标识统一为 `kaoshi`，面向用户的中文产品名统一为“考试”。同时重做管理端基础体验、角色权限配置和考试端作答状态：浅色、克制、用户可理解，角色权限配置使用复选框分组和明确已选状态，不把英文 code 当成主要界面；考试端必须有开始确认、倒计时、附件展示、提交确认、提交锁定和自动化浏览器验收。

## 2. 当前事实

- 当前仓库目录仍是 `C:\Users\Administrator\Desktop\gaokao`，目录名未改，但 Git remote 已允许改到新仓库。
- Java 包名、Docker、脚本、前端 storage key、日志前缀等已经开始迁移到 `kaoshi/KAOSHI/com.kaoshi`。
- 当前前端使用 Vue 3、TypeScript、Element Plus 全量安装。
- 当前 UI 曾存在手写深色侧栏和深色登录页，已开始改为浅色 token。
- 当前角色页用表格展示角色，用弹窗编辑角色；权限和菜单配置曾使用多选下拉，这不符合用户友好要求。
- `.codex/skills` 已改成 kaoshi 项目专用规则，明确权限配置使用复选框/分组，不使用大量下拉让用户猜。
- 已 research：`vue-pure-admin`、`vue-vben-admin`、`soybean-admin` 等成熟后台模板都采用主题 token、菜单布局组件化、权限配置分组或树形/复选交互。当前任务继续使用 Element Plus，不复制模板源码。
- 已 research：`yfexam` 的作答页包含倒计时、答题卡、上一题/下一题、未作答提交确认和超时提交；题目编辑页用表格维护选项、正确答案、图片附件和题型校验。当前任务吸收作答状态、提交确认和媒体渲染模式，不照搬其旧 Vue 2/Element UI 实现。
- 当前前端角色接口类型已经改成 `permissions: AdminPermission[]` 和 `menus: AdminMenu[]`，但后端 `AdminRoleResponse` 仍返回字符串列表，契约不一致。
- 当前考试端已经有开始前确认态、倒计时和提交确认雏形，但缺少离开风险提示、答题卡、未作答统计和完整浏览器级验收。
- 当前 `frontend/package.json` 已加入 Playwright。浏览器级测试必须单独从 `start_browser_test.py` 或 `npm.cmd run test:e2e` 触发，常规验证不再隐式弹出浏览器。
- 根目录已存在 `start_dev.py`、`stop_dev.py`、`start_test.py`、`start_browser_test.py`、`stop_test.py`，测试脚本边界已收敛为：启动前统一停止旧服务，常规测试不启动浏览器，浏览器测试使用真实后端、真实前端、真实 Docker 数据库和 Chromium。

## 3. 失败测试

- 运行时代码、文档、脚本或配置仍把原项目名作为当前项目名。
- Java 包名、主类、测试包名迁移后后端无法编译。
- Docker Compose、脚本或启动器仍使用旧容器名、数据库名或环境变量导致启动失败。
- 前端 localStorage key、标题、日志前缀、包名仍使用旧项目名。
- UI 仍以深色侧栏、深色登录页或随意手写配色为主。
- 角色权限配置仍主要依赖多选下拉或英文 code 列表。
- 角色管理接口返回字符串权限/菜单，导致前端无法稳定显示中文权限名和菜单标题。
- 页面通过解释文案替代清晰控件，或说明文字堆叠影响专业感。
- 表格、弹窗、权限配置在常规宽度下出现挤压、溢出或不可操作。
- 考试开始后没有答题卡、未作答统计或离开提示，用户无法判断进度和提交风险。
- Playwright 不能通过真实后端和真实数据库自动验证登录、导航、角色保存和考试提交。
- 常规测试入口隐式启动浏览器，导致 owner 无法区分代码验证和真实浏览器验收。
- E2E 启动脚本在 Playwright webServer 内部再次清理端口或进程，存在误杀当前测试链路的风险。
- 根目录测试入口和 VSCode 任务没有清晰区分“常规测试”“浏览器测试”“停止测试”。
- 完整验证不通过。

## 4. 目标

- 项目英文标识统一为 `kaoshi`。
- 中文产品名统一为“考试”。
- Java 包名为 `com.kaoshi`，主类为 `KaoshiApplication`。
- Docker 容器、卷、数据库、脚本函数、环境变量统一为 `kaoshi/KAOSHI`。
- 前端包名、标题、storage key、日志前缀统一为 `kaoshi/考试`。
- Git remote 指向 `agentjz/kaoshi`。
- 前端采用正式浅色后台主题：浅色侧栏、白色顶栏、浅灰内容背景、统一边框/圆角/阴影/状态色，去掉深色登录背景。
- 角色编辑使用分组复选框配置权限和菜单，用户通过中文名称即可完成配置；系统 code 只做辅助审计信息。
- 权限清单和菜单清单页面以中文名称、业务归属、状态和路径为主，避免英文 code 成为主视觉。
- 后端角色响应返回完整权限对象和菜单对象，与前端类型一致。
- 考试端作答页包含开始确认、倒计时、答题卡、题目附件、未作答提交确认、提交锁定、超时自动提交和离开风险提示。
- Playwright 浏览器测试覆盖真实登录、管理端核心导航、角色创建保存、考试开始作答、提交结果和真实数据库写入链路。
- 根目录提供新手友好的测试入口：`start_test.py` 运行常规验证，`start_browser_test.py` 运行真实浏览器验收，`stop_test.py` 停止测试相关服务。
- `start_browser_test.py` 在启动前先停止本项目旧服务，再启动真实测试栈、等待前端可访问、执行 Playwright、最后统一停止服务；`scripts/run-e2e-stack.ps1` 只负责重置 Docker 数据、启动后端和前端、等待就绪并维持服务，不再负责杀进程。

## 5. 不做范围

- 不更换 Vue、Element Plus 或后端技术栈。
- 不引入 `vue-pure-admin`、`vue-vben-admin` 或 `soybean-admin` 源码。
- 不实现暗黑主题切换。
- 不实现完整菜单树拖拽和动态路由后台配置。
- 不把 Playwright 测试写成替代后端业务测试；后端评分、提交锁定和权限仍由 Maven 测试覆盖。
- 不使用 API mock 伪造浏览器验收。
- 不自动 commit 或 push。

## 6. 设计

- 命名迁移按语义边界执行：Java 包与文件路径、Maven artifact、Spring 配置、Docker/脚本、前端应用标识、文档分别处理。
- 旧数据库名不作为运行时兼容保留；本地 Docker 迁移到 `kaoshi` 后重新初始化。
- 前端主题保留 Element Plus，使用 CSS token 管理背景、面板、边框、文本、主色、菜单、表格、阴影。
- 管理端侧栏改浅色；登录页改浅色产品型布局；页面减少说明短句，靠标题、表格列、按钮和表单 label 表达功能。
- 角色编辑弹窗采用分区：基础信息、管理权限、可见菜单。权限和菜单用 CheckboxGroup，可全选/清空，长名称可换行，code 使用小号灰色文本。
- 角色响应契约由后端直接返回权限对象和菜单对象；前端不再把 code 当作主要数据源，也不做旧字符串结构适配。
- 考试端先进入考试说明/确认态，点击开始后记录本次作答开始时间并显示倒计时；题目列表右侧提供答题卡，提交前统计未作答数；倒计时结束自动提交；提交按钮必须有确认、防重复提交和提交后锁定状态。
- 考试离开风险用路由离开守卫和浏览器刷新拦截覆盖；提交完成或超时自动提交后解除拦截。
- Playwright 测试使用真实 Docker MySQL/Redis、真实 Spring Boot 后端、真实 Vue 前端和真实浏览器操作核心链路；测试前重置本项目 Docker 数据卷，保证种子数据可重复，禁止用 API mock 作为 E2E 验收。
- 测试入口分层：常规验证覆盖 Python 脚本编译、后端测试、前端类型检查、前端单元测试、前端构建、Docker 配置和文档扫描；浏览器验收单独覆盖真实登录、角色保存、考试作答和提交结果。
- PowerShell 底层脚本保留为可组合构件；根目录 Python 脚本作为 owner 在 VSCode 中可点击运行的友好入口。
- 角色列表展示角色名、说明、权限数量、菜单数量和更新时间/操作，不在主列表铺满英文 code。

## 7. 实施任务

- [x] K401 更新计划并确认 research 结论进入当前任务。
- [x] K402 迁移 Java 包名、Maven artifact、Spring 配置和测试。
- [x] K403 迁移 Docker、脚本、启动器、VSCode task 和本地数据库名。
- [x] K404 迁移前端项目名、storage key、标题、日志前缀和可见文案。
- [x] K405 改造 Element Plus 浅色后台主题和登录页视觉。
- [x] K406 对齐后端角色响应契约，重做角色、权限、菜单页面的信息架构和权限配置控件。
- [x] K409 补齐考试端开始前确认、倒计时、答题卡、未作答提交确认、提交锁定和离开风险提示。
- [x] K410 引入真实 Playwright 浏览器级测试，覆盖登录、管理端导航、角色创建保存、考试作答提交核心链路，并接入统一验证。
- [x] K411 拆分测试启动/停止入口：常规测试、浏览器测试、停止测试各自清晰，浏览器测试使用真实服务且不使用 mock。
- [x] K407 同步 README、spec、AGENTS、skills、roadmap、plan 和 remote URL。
- [x] K408 运行扫描、后端测试、前端类型检查、单测、构建和完整验证。

## 8. 验证计划

```powershell
rg -n "gaokao|Gaokao|GAOKAO|高考|com\.gaokao|agentjz/gaokao" . --glob "!frontend/dist/**" --glob "!backend/target/**" --glob "!node_modules/**" --glob "!.git/**"
powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1
python .\start_browser_test.py
```

## 9. 收口

已完成。

完成事实：

- 项目命名、文档、脚本、Docker、前端 package/storage key 和 Java 包名已统一到 `kaoshi`。
- 后端角色响应契约已返回完整权限对象和菜单对象，前端角色页不再依赖字符串 code 展示主要信息。
- 角色配置弹窗已改为基础信息、管理权限、可见菜单三段式复选框配置，支持全选和清空。
- 管理端基础主题已调整为浅色布局，权限清单和菜单清单以中文名称为主，code/path 只做辅助信息。
- 考试端作答页已包含开始确认、倒计时、答题卡、附件展示、未作答提交确认、提交锁定、超时自动提交和离开风险提示。
- `start_test.py`、`start_browser_test.py`、`stop_test.py`、VSCode task、PowerShell 脚本和 README/spec 已同步到常规验证与真实浏览器验收分层。
- 浏览器验收生命周期已理顺：Docker MySQL/Redis 先启动并通过健康检查，后端随后启动，前端随后启动，Playwright 最后执行；测试结束后统一停止后端、前端和 Docker 服务。
- Playwright E2E 使用真实 Docker MySQL/Redis、真实 Spring Boot 后端、真实 Vue 前端和有头 Chromium，不使用 API mock；登录断言保护登录接口成功、进入管理端、token 写入和核心菜单可用，不绑定宣传标题文案。

验证结果：

- `mvn test`：通过，12 个后端测试通过。
- `npm.cmd run typecheck`：通过。
- `npm.cmd run test:unit`：通过，9 个前端单元测试通过。
- `npm.cmd run build`：通过；Vite 仅提示 bundle chunk size 警告。
- `python .\start_browser_test.py`：通过，3 条真实浏览器 E2E 通过，覆盖真实登录、角色创建保存、后端种子数据读取、考试作答提交和成绩展示。
- `python .\start_test.py`：通过，覆盖 Python 脚本编译、后端测试、前端类型检查、前端单元测试、前端构建、Docker Compose 配置和文档扫描。
- `powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1`：通过，作为 `start_test.py` 的底层常规验证脚本。
- `python .\stop_test.py`：通过，结束后无后端、前端或 Playwright 残留进程；Docker 容器保留为停止状态。
- 扫描 `mockApi|page.route|API mock|有头|后续阶段进入|com.gaokao|agentjz/gaokao`：无运行时代码残留；`plan.md` 中保留“禁止 API mock”作为验收纪律。

剩余风险：

- Vite 构建存在主 chunk 超过 500 kB 的性能警告，当前不影响功能和验收，但后续做前端性能治理时应拆分 vendor/manualChunks。
- 当前真实浏览器验收每次会重置本项目 Docker 数据库，只适合本地验收环境，不应在共享数据库上运行。
