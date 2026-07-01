# kaoshi

kaoshi 是一个现代化考试与考试管理平台。

它面向需要长期维护题库、组织考试、管理考生、发布试卷、记录成绩和分析结果的教育场景。平台以考试业务为中心，把题库建设、试卷编排、考试发布、在线作答、自动评分和成绩管理组织成一套清晰、可扩展、可部署的系统。

## 产品方向

- 管理端用于维护用户、角色、题库、试题、试卷、考试、成绩和系统配置。
- 考试端用于考生登录、查看考试任务、进入考试、计时作答、提交试卷和查看结果。
- 数据模型以题库、试题、试卷、考试、作答、成绩和用户权限为核心。
- 富媒体题目支持图片、音频等附件能力，并通过统一文件服务接入。
- 系统按前后端分离方式建设，便于后续扩展 H5、桌面 Web 和小程序考试入口。

## 技术栈

- 后端：Java 21、Spring Boot 3
- 数据库：MySQL 8
- ORM：MyBatis-Plus
- 认证权限：Spring Security、JWT
- 缓存与限流：Redis
- 管理端：Vue 3、TypeScript、Vite、Element Plus
- 考试端：Vue 3、TypeScript
- 部署：Docker Compose

## 工程原则

kaoshi 会按生产级平台方式建设。业务边界、数据模型、接口契约、页面结构、测试验证和文档同步必须保持一致。

当前仓库使用 `roadmap.md` 管阶段总览，使用 `plan.md` 管当前阶段执行。

## 当前工程

- `backend/`：Spring Boot 3、Java 21、MyBatis-Plus、Flyway、Spring Security、JWT、MySQL、Redis。
- `backend/` 当前包含登录、当前用户、用户管理、角色管理、权限清单、菜单清单、题库、试题、试卷、考试发布、考试端作答、提交锁定、自动评分和成绩归档。
- `frontend/`：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus。
- `frontend/` 当前包含登录页、管理端布局、考试端布局、用户管理、角色管理、权限清单、菜单清单、题库管理、试题管理、试卷管理、考试管理、成绩管理、考试任务列表、在线作答、题目附件展示和提交结果页。
- `docker-compose.yml`：MySQL、Redis、后端和前端的本地编排。
- `scripts/verify.ps1`：常规验证入口，覆盖 Python 脚本编译、后端测试、前端类型检查、前端单元测试、前端构建、Docker 配置和文档扫描。

## 本地运行

最简单的方式是在 VSCode 打开根目录的 `start_dev.py`，点击运行按钮。

普通启动：

```powershell
python .\start_dev.py
```

停止本地服务：

```powershell
python .\stop_dev.py
```

清空 Docker 数据库并启动：

```powershell
python .\start_dev.py --reset
```

`start_dev.py` 会启动 Docker MySQL/Redis、后端、前端，等待服务可访问后打开普通浏览器窗口。

先启动数据库和缓存：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\reset-docker-data.ps1
```

这条命令会清空并重建本项目的 Docker MySQL/Redis 数据。MySQL 暴露在 `localhost:13306`，Redis 暴露在 `localhost:16379`，避免和本机已安装的 MySQL/Redis 抢默认端口。

启动后端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
```

启动前端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-frontend.ps1
```

浏览器访问 `http://localhost:5173/`。前端会把 `/api` 请求代理到后端 `http://localhost:8080/`。

VSCode 可以用 `Terminal: Run Task` 运行这些任务：

- `kaoshi: start all`
- `kaoshi: reset and start all`
- `kaoshi: stop all`
- `kaoshi: reset docker data`
- `kaoshi: start backend`
- `kaoshi: start frontend`
- `kaoshi: verify`
- `kaoshi: browser test`
- `kaoshi: stop test`

本地种子账号：

- 账号：`admin`
- 密码：`password`

## 验证

常规测试不打开浏览器：

```powershell
python .\start_test.py
```

真实浏览器测试会重置本项目 Docker 数据库，启动真实 Java 后端和 Vue 前端，并用 Chromium 自动操作完整链路：

```powershell
python .\start_browser_test.py
```

停止测试相关服务：

```powershell
python .\stop_test.py
```

底层命令：

```powershell
.\scripts\verify.ps1
cd backend; mvn test
cd frontend; npm.cmd install; npm.cmd run typecheck; npm.cmd run test:unit; npm.cmd run build
cd frontend; npm.cmd run test:e2e
```

## License

MIT License

