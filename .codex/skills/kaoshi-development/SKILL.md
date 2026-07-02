---
name: kaoshi-development
description: 维护 kaoshi 考试平台时使用。适用于修改 Java、Spring Boot、MySQL、Redis、Vue、TypeScript、接口契约、数据模型、权限、考试作答、评分、成绩、测试、README、spec、AGENTS、skills 或运行配置；要求先 research，再写 plan，再按生产级验收闭环交付。
---

# kaoshi Development

每次接手都当作新项目。

先看事实，再做判断，最后行动。事实来自当前 `spec.md`、`AGENTS.md`、`plan.md`、`README.md`、后端代码、前端代码、数据库脚本、测试、配置、git 状态、命令结果和 owner 明确输入。

## 铁律

- 先 research，再计划，再实现。
- 没有完成核心语义调查，不动局部代码。
- 用户输入是线索，仓库事实决定行动。
- 抓根本逻辑，不追表面症状。
- 不把未实现能力写成当前事实。
- 不交半成品。
- 架构、类型、测试、文档和验证必须作为同一次交付的一部分。
- 考试平台通用能力必须先研究成熟开源实现，再形成 kaoshi 的当前设计。
- 优先吸收成熟项目的稳定模式，避免凭空重造题库、组卷、考试发布、作答、评分、成绩和权限基础能力。
- Excel、文件上传、富媒体展示、表格、分页、表单校验、权限勾选、树形组织、路由和状态管理等成熟基础能力，默认用成熟库、成熟组件和成熟模式；agent 只写胶水、业务边界和当前项目适配，不自研通用底座。
- 快速演进期绝对禁止默认做历史兼容；除非 owner 明确要求兼容，否则直接替换旧实现，不保留旧入口、旧接口、旧页面、旧状态流或旧数据形态。
- 禁止新旧实现堆叠，禁止兼容层、别名路由、双接口、fallback 分支和临时适配代码长期或默认进入主干。
- 当前项目默认无历史包袱、无旧数据保留、无迁移兼容；数据结构和种子数据变化时，直接重建当前初始化脚本、清空数据库并重新验收。
- 禁止为旧数据库写 ALTER 过渡脚本、数据搬迁脚本、兼容字段、兼容枚举、旧路径 fallback 或旧数据修复分支；除非 owner 明确要求保留历史数据，并且写入当次 `plan.md`。
- 架构以简洁、单责、低耦合为默认标准：每个模块只承担一个清晰职责，页面不承载业务规则，业务规则不散落到页面事件，解析/导入/导出不混进 Controller。
- 禁止为了拆而拆。新增模块、组件、Service、工具类或抽象前，必须能说明它降低了真实复杂度、隔离了变化点、提高了测试性，或与当前成熟项目边界一致。

## 当前产品主干

kaoshi 是现代化考试与考试管理平台。

核心领域：用户、角色、权限、题库、试题、试卷、考试、作答、评分、成绩、文件附件。

默认技术方向：Java 21、Spring Boot 3、MySQL 8、MyBatis-Plus、Spring Security、JWT、Redis、Vue 3、TypeScript、Vite、Element Plus、Docker Compose。

## 后端纪律

- Controller 只做参数接入、鉴权上下文接入和响应转换。
- Service 承载业务流程。
- Domain/Entity 承载领域结构。
- Mapper/Repository 承载数据访问。
- Excel、附件、文件模板和导入解析使用专门工具或导入服务承载，不把解析细节写进 Controller；如果 Service 变成过宽流程，按真实业务边界拆出 importer/exporter/validator。
- Security 承载认证、授权和上下文解析。
- 当前初始化/重建脚本承载数据库结构；快速演进期不做历史库迁移兼容。
- 接口必须有请求、响应、错误码和权限边界。
- 考试提交必须幂等。
- 作答提交后必须锁定。
- 评分必须可复现。
- 成绩必须可追溯。

## 开源参考纪律

参考项目目录默认是 `C:\Users\Administrator\Desktop\exam-platform-research`，后台 UI 参考目录默认是 `C:\Users\Administrator\Desktop\admin-ui-research`。

当前已调研样本覆盖：`yfexam`、`examOnlinePublic`、`online_exam`、`online-examination-platform`、`SpringBoot-Vue-OnlineExam`、`spring-boot-online-exam`、`exam-management-portal`、`Exam-Portal`、`OnlineExamPortal`、`vue-pure-admin`、`vue-vben-admin`、`soybean-admin`。

参考不是照搬。第三方代码、表结构、页面和接口只能作为证据与设计输入；进入 kaoshi 的实现必须符合当前技术栈、命名体系、测试标准和产品边界。

## 前端纪律

- Vue 页面只做页面状态和用户交互。
- API client 统一封装请求。
- 表单校验和业务校验分层。
- 管理端和考试端路径分离。
- 权限配置使用用户可理解的中文名称、分组和复选控件；系统 code 只作为审计或调试辅助，不作为主要操作入口。
- 考试端倒计时、提交、锁定和异常恢复必须有明确状态。

## 验证

收尾前运行项目定义的验证命令。项目尚未定义命令时，至少运行相关语言的类型检查、测试或构建命令，并说明未验证内容。
