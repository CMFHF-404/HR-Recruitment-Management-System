# Department Manager Review Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a department manager role that owns position maintenance and confirms HR-passed candidates before interviews.

**Architecture:** Extend the existing single-user `Admin` model with a role enum and use Spring Security authorities from JWT. Store manager confirmation fields on `ResumeScreening` so candidate progress remains simple. Add a manager review endpoint and a Vue manager review page while preserving the current HR workflow.

**Tech Stack:** Spring Boot 3, Java 17, Spring Security, Spring Data JPA, H2/MySQL, Vue 3, Vite, Element Plus.

---

### Task 1: Backend Role And Manager Review Flow

**Files:**
- Modify: `backend/src/test/java/com/hrms/recruitment/HrRecruitmentManagementSystemApplicationTests.java`
- Create: `backend/src/main/java/com/hrms/recruitment/domain/AdminRole.java`
- Create: `backend/src/main/java/com/hrms/recruitment/domain/ManagerReviewStatus.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/domain/Admin.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/domain/ResumeScreening.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/config/DataInitializer.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/security/JwtService.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/security/JwtAuthenticationFilter.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/security/SecurityConfig.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/controller/AuthController.java`
- Create: `backend/src/main/java/com/hrms/recruitment/controller/ManagerReviewController.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/service/RecruitmentService.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/repository/ResumeScreeningRepository.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/repository/InterviewRepository.java`
- Modify: `backend/src/main/java/com/hrms/recruitment/controller/InterviewController.java`

- [ ] Write failing MockMvc tests for login role, manager-only position creation, manager approval enabling interview, and manager rejection blocking interview.
- [ ] Run `backend\mvnw.cmd test -Dtest=HrRecruitmentManagementSystemApplicationTests` and confirm failures are due to missing role/review behavior.
- [ ] Add role and manager review enums/entities.
- [ ] Update JWT/authentication/security to expose `ROLE_HR` and `ROLE_MANAGER`.
- [ ] Add manager review service and controller behavior.
- [ ] Gate interview scheduling on `PASSED` + `APPROVED`.
- [ ] Run backend tests and fix until green.

### Task 2: Frontend Role-Aware UI

**Files:**
- Modify: `frontend/src/api.js`
- Modify: `frontend/src/router.js`
- Modify: `frontend/src/views/LoginView.vue`
- Modify: `frontend/src/views/AppShell.vue`
- Modify: `frontend/src/views/WorkflowView.vue`
- Modify: `frontend/src/views/CandidatesView.vue`
- Create: `frontend/src/views/ManagerReviewsView.vue`

- [ ] Store `role` from login response.
- [ ] Add route metadata and guards for HR and manager pages.
- [ ] Add manager review status text and colors.
- [ ] Add manager confirmation navigation and page.
- [ ] Show manager review status in candidate progress and screening table.
- [ ] Run `npm run build` in `frontend` and fix until green.

### Task 3: Database And Documentation

**Files:**
- Modify: `database/schema.sql`
- Modify: `database/init-data.sql`
- Modify: `README.md`

- [ ] Add `role` and manager review columns to SQL schema.
- [ ] Add default manager account to init data.
- [ ] Document both default accounts and role behavior.
- [ ] Run backend tests and frontend build again before completion.
