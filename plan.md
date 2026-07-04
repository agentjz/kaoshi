# kaoshi 通用试卷内容树重构计划

## 需求

- 把 CET4 暴露出来的题组问题修正为通用试卷内容树能力，不能做成四级特判。
- 支持听力、阅读、写作、翻译等大组模板，也支持普通单选/多选无题组的简单维护模式。
- 题组材料、说明、音频、共享选项池只维护和展示一次；小题只作为作答、保存、评分和成绩明细单位。
- 管理端编辑必须用户友好，不能让用户反复编辑同一段材料和同一组选项。
- 允许直接重构数据库初始化结构，不做历史兼容和软过渡；验收以清空数据库后重新初始化为准。
- 继续遵守文件职责原则：数据结构、导入、评分、展示、编辑器状态和 UI 渲染拆清边界。

## 当前事实

- 当前 `questions`、`exam_draft_questions`、`exam_published_questions`、`exam_attempt_questions` 仍把 `section_*`、`group_*`、`item_*` 字段挂在每个小题上；这是必须在本轮完成的底层归一化缺口。
- 当前 CET4 seed JSON 已改为 `sections -> groups -> items`，匹配题和选词填空的共享选项只在 group 上维护。
- 当前考试端用 `groupQuestionsForDisplay` 按 `sectionCode + groupCode` 分组，并能识别 `WORD_BANK`/`MATCHING` 共享选项池。
- 当前题库管理已有内容结构树预览，普通单题编辑仍走原试题编辑弹窗。
- 当前评分、保存、提交和成绩明细都以小题为单位，这个边界是正确的，不能丢。
- 当前项目允许快速演进，数据库脚本表达当前初始化事实，不要求历史兼容。

## 生产级验收保护点

- 内容结构必须通用：不能出现 CET4 专用表、专用字段或专用前端分支。
- 普通单选/多选仍可作为无上级题组的独立题维护、组卷、作答和评分。
- 题组树节点只承载结构、说明、材料、共享附件、共享选项和显示规则；ITEM 承载可作答、可评分事实。
- 发布快照、作答快照必须冻结树节点、共享选项、共享附件和 ITEM，不能依赖题库后续修改。
- 选词填空和匹配题在考试端、成绩详情中必须只显示一次共享材料/共享选项，小题按顺序紧凑作答。
- 管理端必须至少提供可理解的内容树视图：大组/小组/小题层级清楚，不能只暴露一堆重复伪单选题。
- 浏览器验收必须覆盖 CET4 在线作答、写作保存读回、听力音频、选词/匹配共享展示、提交、阅卷和成绩详情。

## 目标

- 新增通用题库内容树源数据结构：`question_nodes`、`question_node_options`、`question_node_attachments`。
- 新增试卷草稿/发布/作答快照节点结构，ITEM 小题引用节点，删除小题表上重复的 group/section 材料字段。
- 重构资源导入格式为 `sections -> groups -> items`，CET4 2023 set 1 只作为一份通用内容树数据。
- 扩展 API 响应，向前端返回可由 `questions` 组装的 `contentPath`/`group` 信息和共享选项/附件。
- 重构考试端和成绩详情渲染：普通题按单题面板，题组题按共享上下文 + 小题列表渲染。
- 管理端题库页面新增内容树面板/节点预览，先让导入后的树结构可扫描、可理解；普通题编辑继续可用。

## 不做范围

- 本轮不实现无限层级的所有高级拖拽编辑细节。
- 本轮不实现每个 section 独立倒计时。
- 本轮不导入更多 CET4/CET6 试卷。
- 本轮不保留旧 schema 或旧 seed 的兼容迁移。

## 设计

- 源数据：
  - `question_nodes`：题库内通用树节点，`node_type = SECTION/GROUP`，支持 `parent_id`，承载 `title`、`direction`、`material`、`rule_json`、`sort_order`。
  - `questions`：只保留 ITEM 事实：题库、所属 `node_id`、题型、题干/小题题干、题号、解析、难度、状态。
  - `question_node_options`：题组共享选项池，例如选词填空 A-O、匹配段落 A-L。
  - `question_node_attachments`：题组共享音频/图片/文件。
  - `question_options` 和 `question_attachments`：仍支持独立小题自己的选项和附件。
- 快照：
  - `exam_draft_nodes`、`exam_published_nodes`、`exam_attempt_nodes` 冻结内容树。
  - `exam_*_questions` 只引用对应快照节点，并保存 ITEM 字段、分值、排序。
  - `exam_*_node_options`、`exam_*_node_attachments` 冻结共享选项和共享附件。
- 前端：
  - `exam-question-groups` 从题目列表和节点路径构造通用树，不按 CET4 特判。
  - `QuestionGroupContext` 展示树节点说明、材料、共享附件、共享选项。
  - 新增题组小题渲染组件：选词填空/匹配用共享选项池 + 小题行，不重复渲染选项池。
  - 题库管理新增树视图组件，展示导入题库的大组、小组、小题数量和共享资源。

## 实施任务

- [x] 确认当前 schema、导入器、考试快照、作答/评分和前端展示的重复边界。
- [x] 重构数据库初始化脚本为通用内容树 schema，删除小题表上的重复 section/group 材料字段。
- [x] 重构资源导入器为树状 `sections/groups/items` 输入。
- [x] 把 2023 CET4 set 1 JSON 改为 `sections -> groups -> items`，去掉 seed 文件里的重复材料和重复共享选项。
- [x] 重构草稿快照、发布快照、作答快照、响应组装和成绩详情为独立 snapshot node 表。
- [x] 拆分考试数据访问职责，`ExamMapper` 只作为 MyBatis 聚合入口，SQL 按 core/paper/attempt/result 分离。
- [x] 重构考试端/成绩详情题组渲染，选词填空和匹配不重复显示选项池。
- [x] 管理端题库页面新增内容树可视化和清晰预览。
- [x] 更新后端测试、前端单测和 Playwright 验收。
- [x] 运行 `mvn test -Dtest=ExamBusinessFlowTests,QuestionSetResourceIntegrityTests`。
- [x] 运行 `npm.cmd run typecheck` 和 `npm.cmd run test:unit`。
- [x] 运行 `python .\start_test.py`。
- [x] 运行 `python .\start_browser_test.py`。
- [x] 收口更新 `plan.md`。

## 验证计划

- 后端：接口契约、CET4 seed 树导入、发布快照冻结、作答快照冻结、客观评分、主观阅卷。
- 前端：题组组装工具、题型渲染策略、管理端内容树展示。
- 浏览器：题库树预览、CET4 在线作答、音频展示、选词/匹配共享选项去重、提交、阅卷、成绩详情。

## 收口

- [x] 当前 schema 不再在每个小题重复保存题组材料、共享附件和共享选项。
- [x] CET4 seed 以通用内容树导入。
- [x] 选词填空/匹配题视觉和交互不再重复。
- [x] 普通单选/多选编辑和组卷不退化。
- [x] 考试数据访问不再集中硬塞到单个巨型 Mapper。
- [x] 常规验证通过：`python .\start_test.py`，后端 28 tests、前端 26 unit tests、typecheck、build、Docker Compose config、文档扫描通过。
- [x] 真实浏览器验收通过：`python .\start_browser_test.py`，Chromium 10 tests 通过。

已知边界：

- 管理端当前提供内容结构树预览，未做完整树状拖拽编辑器；底层数据结构已经支持后续无历史包袱地扩展题组级编辑。
