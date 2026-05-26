# 小型企业招聘管理信息系统

面向小型企业 HR 的招聘管理信息系统，覆盖岗位信息管理、候选人信息管理、简历筛选、部门主管二次确认、面试安排、录用结果管理和招聘数据统计。

## 技术栈

- 后端：Spring Boot 3、Java 17、Spring Security、Spring Data JPA、MySQL 8、H2 测试库
- 前端：Vue 3、Vite、Element Plus、Axios、ECharts
- HR 默认账号：admin
- HR 默认密码：admin123
- 部门主管默认账号：manager
- 部门主管默认密码：manager123

## 本地运行

### 1. 准备 MySQL

创建数据库，也可以直接执行 `database/schema.sql`：

```sql
CREATE DATABASE IF NOT EXISTS hr_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

如果是从旧版本数据库升级，先执行 `database/migrate-manager-review.sql` 补齐部门主管和二次确认相关字段，再执行 `database/migrate-ai-resume-matching.sql` 补齐 AI 简历匹配字段，最后执行 `database/init-data.sql` 初始化默认账号。

默认连接配置在 `backend/src/main/resources/application.properties`：

```properties
DB_URL=jdbc:mysql://localhost:3306/hr_recruitment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=root
AI_API_KEY=你的 OpenAI 兼容接口密钥
AI_BASE_URL=https://api.openai.com/v1
AI_MODEL=gpt-4o-mini
AI_MODE=REMOTE
RESUME_UPLOAD_DIR=uploads/resumes
```

如本机 MySQL 密码或 AI 服务配置不同，可在启动前设置环境变量覆盖。未设置 `AI_API_KEY` 时，简历仍可上传，但 AI 匹配分析会提示稍后重试。

课堂实验可启用本地 FAST 模式，不需要外部 AI 密钥：

```powershell
$env:AI_MODE='FAST'
.\mvnw.cmd spring-boot:run
```

FAST 模式会使用岗位 JD 与简历文本中的关键词命中情况生成匹配度和快速评价，适合演示“AI 辅助判断”流程；它不是训练模型，也不能替代人工审核。

### 2. 启动后端

```powershell
cd backend
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot'
.\mvnw.cmd spring-boot:run
```

后端地址：`http://localhost:8080`

没有 MySQL 时，可用 H2 演示：

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5179`

## 验证命令

```powershell
cd backend
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot'
.\mvnw.cmd test

cd ..\frontend
npm run build
```

## 交付内容

- `backend/`：Spring Boot 后端接口与测试
- `frontend/`：Vue 前端管理界面
- `database/`：数据库建表和初始化数据脚本
- `docs/`：课程设计报告草稿、测试记录、数据库设计说明
