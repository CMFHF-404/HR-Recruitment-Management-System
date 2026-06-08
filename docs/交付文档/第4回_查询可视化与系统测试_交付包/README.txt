课程：管理信息系统
实验：第4回 数据查询、可视化与系统测试
系统名称：小型企业招聘管理信息系统

1. 系统主题
本系统面向小型企业 HR 与部门主管，支持岗位维护、候选人登记、简历筛选、主管确认、面试安排、录用结果登记、数据查询与统计可视化。

2. 本次实现的功能
- 候选人按姓名、电话、邮箱关键词查询。
- 候选人按应聘岗位筛选。
- 岗位按岗位名称、部门关键词查询。
- 统计首页展示岗位数量、候选人数、待筛选数、面试人数、录用人数。
- 统计首页使用 ECharts 展示各岗位候选人数、面试人数、录用人数柱状图。
- 候选人列表支持导出 CSV。
- 招聘流程页面支持简历筛选、主管确认后的面试安排、录用结果登记。
- 后端自动化测试覆盖登录、角色权限、异常输入、流程前置条件、统计和 CSV 导出。

3. 数据库文件与脚本
- MySQL 建表脚本：database/schema.sql
- 初始化数据脚本：database/init-data.sql
- 演示可使用 H2：backend/data/hrms.mv.db

4. 主要数据表
- admin：管理员与部门主管账号
- position：招聘岗位
- candidate：候选人
- resume_screening：简历筛选与主管确认
- interview：面试安排与评价
- offer_result：录用结果

5. 查询功能说明
- 候选人管理支持按姓名、电话、邮箱模糊查询。
- 候选人管理支持按应聘岗位筛选。
- 岗位管理支持按岗位名称、所属部门模糊查询。

6. 可视化功能说明
- 指标卡：岗位数量、候选人数、待筛选、面试人数、录用人数。
- 柱状图：各岗位候选人数、面试人数、录用人数对比。

7. 是否实现地图展示
否。本系统不是 GIS 项目，按讲义“非 GIS 项目”要求，使用业务统计图替代地图展示。

8. CSV 导出文件位置
exports/candidates.csv

9. 运行命令
后端：
cd backend
$env:SPRING_PROFILES_ACTIVE='h2'
.\mvnw.cmd spring-boot:run

前端：
cd frontend
npm install
npm run dev -- --host 127.0.0.1 --port 5179

访问地址：
http://127.0.0.1:5179

默认账号：
HR：admin / admin123
部门主管：manager / manager123

10. 验证命令
后端测试：
cd backend
.\mvnw.cmd test

前端构建：
cd frontend
npm run build

