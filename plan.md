# gaokao 初始化与开源参考纪律 Plan

## 当前任务：开源参考项目调研和参考优先纪律

### 1. 需求文档

本次任务确认 gaokao 的后续建设方式：先充分研究成熟考试平台，再基于产品目标吸收其优势，形成自己的架构和实现。gaokao 的开发过程应避免凭空设计考试平台通用能力，特别是题库、试卷、考试发布、作答、评分、成绩、权限和管理端信息架构。

业务完成标准：

- 对已克隆的考试平台参考项目做一轮结构调研。
- 记录哪些项目值得重点参考，以及各自适合参考的方向。
- 把“成熟项目优先参考、吸收优势、再做当前产品设计”的纪律写入 `AGENTS.md` 和 `gaokao-development` skill。
- 文档表达为工程纪律和设计方法，不把任何第三方项目写成运行时依赖或当前产品能力。

### 2. 当前事实

- gaokao 已初始化根文档和项目级 skills。
- 参考项目目录位于 `C:\Users\Administrator\Desktop\exam-platform-research`。
- 已克隆多个前后端分离或考试管理相关项目，覆盖 Spring Boot、MySQL、MyBatis/JPA、Vue、React 和 Angular 等路线。
- 当前 gaokao 尚未创建后端、前端、数据库和部署代码。

### 3. 失败测试

- `AGENTS.md` 没有要求实现前先研究成熟考试平台。
- `gaokao-development` skill 没有要求参考成熟项目、复用成熟模式和避免重新发明通用考试平台能力。
- 文档把第三方项目直接写成 gaokao 的当前能力或运行时依赖。
- 调研只停留在口头判断，没有基于本地参考项目文件结构、依赖和模块事实。

### 4. 目标

- 完成参考项目结构扫描。
- 明确重点参考项目和参考方向。
- 更新 `AGENTS.md` 和 `.codex/skills/gaokao-development/SKILL.md`。
- 更新 `plan.md` 收口，记录调研事实、验证和剩余风险。

### 5. 不做范围

- 本次不复制第三方项目源码到 gaokao。
- 本次不选择某一个项目作为 fork 基座。
- 本次不创建业务代码。
- 本次不修改 README/spec 的当前产品能力描述。

### 6. 设计

参考纪律写入两层：

- `AGENTS.md` 记录仓库级最高协作纪律：大功能实现前必须先研究成熟项目和当前项目事实。
- `gaokao-development` skill 记录开发执行规则：优先参考成熟项目的领域模型、页面组织、权限边界、数据库结构和验证方式；只吸收设计，不直接照搬不适配的代码。

参考项目只作为 research 输入，不成为 gaokao 的运行时依赖。后续每条实现主线都要在 `plan.md` 中写明参考了哪些项目、采用了哪些模式、舍弃了哪些模式。

### 7. 实施任务

- [x] R001 扫描参考项目目录和技术栈。
- [x] R002 总结重点参考项目和参考方向。
- [x] R003 更新 `AGENTS.md` 加入成熟项目参考纪律。
- [x] R004 更新 `gaokao-development` skill 加入参考优先规则。
- [x] R005 验证文档没有把第三方项目写成当前能力。
- [x] R006 收口记录完成事实和剩余风险。

### 8. 验证计划

执行：

```powershell
rg -n "成熟|参考|开源|research|第三方|运行时依赖" AGENTS.md .codex/skills/gaokao-development/SKILL.md plan.md
rg -n "xzs|yfexam|examOnlinePublic|online_exam|SpringBoot-Vue-OnlineExam" README.md spec.md
```

### 9. 收口

已完成。

完成事实：

- 已扫描 `C:\Users\Administrator\Desktop\exam-platform-research` 下参考项目。
- 参考样本覆盖 Java/Spring Boot/MySQL/MyBatis/JPA、Vue/React/Angular、Vite/TypeScript、单体和多模块架构。
- 已确认重点参考方向：
  - `yfexam`、`examOnlinePublic`、`online_exam` 适合参考题库、试题、试卷和管理端组织。
  - `SpringBoot-Vue-OnlineExam` 适合参考考试、试题、试卷 SQL 样例和考试流程。
  - `online-examination-platform` 适合参考多模块边界，但当前不作为默认复杂度基准。
  - `Exam-Portal` 适合参考 Spring Security、JWT 和学生考试流程。
  - React/Angular 项目适合补充对照，不作为默认前端路线。
- 已在 `AGENTS.md` 增加开源参考纪律。
- 已在 `.codex/skills/gaokao-development/SKILL.md` 增加开源参考纪律、参考项目清单和按任务类型选择参考项目的规则。

验证结果：

- `rg -n "成熟|参考|开源|research|第三方|运行时依赖" AGENTS.md .codex/skills/gaokao-development/SKILL.md plan.md` 已执行，能命中新规则。
- `rg -n "xzs|yfexam|examOnlinePublic|online_exam|SpringBoot-Vue-OnlineExam" README.md spec.md` 已执行，未发现 README/spec 把第三方项目写成当前产品能力。

未验证内容：

- 尚未逐个运行参考项目。
- 尚未把参考项目差异整理成正式架构设计。

剩余风险：

- 后续进入具体模块实现时，仍需按模块重新做更细粒度 research，不能只依赖本次粗扫描结论。

---

## 1. 需求文档

本次任务初始化 gaokao 的 AI 协作骨架和项目根文档，让后续开发可以围绕统一产品事实、技术方向、执行计划、安全边界和贡献规则展开。

业务完成标准：

- 项目根目录拥有清晰的产品介绍、产品事实、执行计划、计划模板、协作规约、安全说明、贡献指南、开源协议和忽略规则。
- `.codex/skills` 拥有开发、UI 和计划三个项目级 skill。
- 文档统一表达 gaokao 是现代化考试与考试管理平台。
- 技术方向统一为 Java 21、Spring Boot 3、MySQL 8、MyBatis-Plus、Spring Security/JWT、Redis、Vue 3、TypeScript、Vite 和 Element Plus。

## 2. 当前事实

- 目标目录是 `C:\Users\Administrator\Desktop\gaokao`。
- owner 已确认项目名为 `gaokao`。
- owner 已确认先初始化 AI 辅助骨架和根文档。
- 本机已安装 Java 21、Maven、MySQL、DBeaver、Postman 和 VS Code Java/Spring 插件。
- 当前任务只创建文档和 skill，不生成后端、前端或数据库实现代码。

## 3. 失败测试

- 根目录缺少 `README.md`、`AGENTS.md`、`spec.md`、`plan.md`、`plan.example.md`、`LICENSE`、`SECURITY.md`、`CONTRIBUTING.md` 或 `.gitignore`。
- `.codex/skills` 缺少开发、UI 或 plan skill。
- 文档中项目名不统一。
- 文档把尚未实现的接口、页面、数据库或部署写成当前能力。
- 文档没有记录当前技术方向和执行纪律。

## 4. 目标

- 创建 gaokao 根文档和项目级 skills。
- README 提供简单、有吸引力的产品介绍。
- spec 记录当前产品定位、核心概念、技术方向和初始化验收标准。
- AGENTS 记录协作纪律、根文档职责、skill 使用规则和验证规则。
- SECURITY、CONTRIBUTING、LICENSE、.gitignore 完整可用。

## 5. 不做范围

- 本次不创建 Spring Boot 后端代码。
- 本次不创建 Vue 前端代码。
- 本次不创建数据库 migration。
- 本次不初始化远程仓库或执行 commit/push。

## 6. 设计

根文档负责项目事实和协作边界；skills 负责后续开发时的专项规则。

文档边界：

- `README.md` 面向新读者，介绍产品和技术栈。
- `spec.md` 记录当前产品事实。
- `AGENTS.md` 约束 AI 协作行为。
- `plan.md` 记录当前任务执行合同。
- `plan.example.md` 作为未来计划模板。
- `.codex/skills/*/SKILL.md` 作为后续开发、UI 和计划执行的专项规则。

## 7. 实施任务

- [x] T001 创建目录和 `.codex/skills` 结构。
- [x] T002 创建 README、spec、AGENTS、plan 和 plan.example。
- [x] T003 创建 LICENSE、SECURITY、CONTRIBUTING 和 `.gitignore`。
- [x] T004 创建 gaokao-development、gaokao-ui 和 plan skills。
- [x] T005 检查文件存在、项目命名和未实现能力表述。
- [x] T006 收口记录完成事实和验证结果。

## 8. 验证计划

执行文件检查：

```powershell
Test-Path README.md, AGENTS.md, spec.md, plan.md, plan.example.md, LICENSE, SECURITY.md, CONTRIBUTING.md, .gitignore
Test-Path .codex/skills/gaokao-development/SKILL.md, .codex/skills/gaokao-ui/SKILL.md, .codex/skills/plan/SKILL.md
```

执行内容扫描：

```powershell
rg -n "gaokao" README.md AGENTS.md spec.md plan.md plan.example.md SECURITY.md CONTRIBUTING.md .codex/skills
```

## 9. 收口

已完成。

完成事实：

- 创建 `C:\Users\Administrator\Desktop\gaokao` 项目目录。
- 创建根文档：`README.md`、`AGENTS.md`、`spec.md`、`plan.md`、`plan.example.md`、`LICENSE`、`SECURITY.md`、`CONTRIBUTING.md` 和 `.gitignore`。
- 创建项目级 skills：`gaokao-development`、`gaokao-ui` 和 `plan`。
- README 已写入 gaokao 的简短宣传介绍、产品方向、技术栈和工程原则。
- spec 已记录产品定位、技术方向、核心用户、核心概念和初始化验收标准。
- AGENTS 已记录协作纪律、根文档职责、skill 规则、事实纪律、计划规则、产品纪律、技术纪律和验证规则。
- LICENSE 使用 MIT License。

验证结果：

- 文件存在检查通过。
- `.codex/skills` 三个 skill 文件存在。
- 内容扫描未发现旧项目名或对照式定位表述。
- 技术方向扫描确认 Java 21、Spring Boot 3、MySQL 8、Vue 3、TypeScript、Vite 和 Element Plus 已在关键文档中一致出现。

未验证内容：

- 尚未初始化 Git 仓库。
- 尚未创建后端、前端、数据库和部署代码。

剩余风险：

- 后续进入工程实现前，需要围绕第一条实现主线重新更新 `plan.md`。
