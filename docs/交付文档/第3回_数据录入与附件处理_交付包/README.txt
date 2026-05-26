课程：管理信息系统
实验：第3回 数据录入、校验与附件处理
姓名：
学号：
系统名称：小型企业招聘管理系统

1. 本回实现的功能：
- 候选人资料录入
- 数据校验：必填、邮箱格式、附件格式/大小、数据库约束
- 附件上传：保存到 uploads/
- 写入 SQLite 数据库
- 数据预览
- AI 辅助匹配标签保存

2. 数据库文件：
data/hr_recruitment_lab3.db

3. 主要数据表：
candidate_entry

4. 已实现的校验规则：
- 姓名、电话、邮箱、学历、毕业院校、应聘岗位必填
- 邮箱必须包含 @
- 附件仅允许 pdf、doc、docx、jpg、jpeg、png、txt，且不超过 5MB
- phone/email 唯一，status/ai_confidence 使用数据库 CHECK 约束

5. 附件保存位置：
uploads/

6. 运行命令：
python -m streamlit run app.py
