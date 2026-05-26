package com.hrms.recruitment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hrms.recruitment.common.BusinessException;
import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.repository.ResumeScreeningRepository;
import com.hrms.recruitment.service.ai.AiClient;
import com.hrms.recruitment.service.ai.ResumeAnalysisResult;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class HrRecruitmentManagementSystemApplicationTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ResumeScreeningRepository screenings;

    @MockitoBean
    AiClient aiClient;

    @BeforeEach
    void resetAiClient() {
        reset(aiClient);
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginIgnoresStaleBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer stale-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.role").value("HR"));
    }

    @Test
    void managerCanLoginWithManagerRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"manager\",\"password\":\"manager123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    void onlyManagerCanCreatePositions() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");

        mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"HR 不可建岗位","department":"测试部","headcount":1,
                                "requirements":"HR 尝试新增岗位","status":"OPEN"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"主管新增岗位","department":"技术部","headcount":1,
                                "requirements":"主管维护岗位","status":"OPEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void managerCannotAccessHrStatistics() throws Exception {
        String managerToken = loginToken("manager", "manager123");

        mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruitmentFlowCanBeManagedEndToEnd() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionResult = mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Java 开发工程师","department":"技术部","headcount":2,
                                "requirements":"熟悉 Spring Boot 与 MySQL","status":"OPEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();
        String positionId = positionResult.replaceAll(".*\"id\":([0-9]+).*", "$1");

        String candidateResult = mockMvc.perform(post("/api/candidates")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"张三","gender":"男","phone":"13800138000","email":"zhangsan@example.com",
                                "education":"本科","school":"示例大学","positionId":%s,"note":"课程设计测试数据"}
                                """.formatted(positionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.position.id").value(Integer.parseInt(positionId)))
                .andReturn().getResponse().getContentAsString();
        String candidateId = candidateResult.replaceAll(".*\"data\":\\{\"id\":([0-9]+).*", "$1");

        mockMvc.perform(put("/api/screenings/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PASSED\",\"comment\":\"符合岗位要求\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.managerStatus").value("PENDING"));

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewTime\":\"2026-05-01T10:00:00\",\"location\":\"会议室 A\",\"interviewer\":\"李经理\",\"status\":\"SCHEDULED\",\"evaluation\":\"待面试\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("候选人需经部门主管确认通过后才能安排面试"));

        mockMvc.perform(get("/api/manager-reviews")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].candidate.id").value(Integer.parseInt(candidateId)))
                .andExpect(jsonPath("$.data.content[0].managerStatus").value("PENDING"));

        mockMvc.perform(put("/api/manager-reviews/" + candidateId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"comment\":\"部门用人需求匹配\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.managerStatus").value("APPROVED"));

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewTime\":\"2026-05-01T10:00:00\",\"location\":\"会议室 A\",\"interviewer\":\"李经理\",\"status\":\"SCHEDULED\",\"evaluation\":\"待面试\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        mockMvc.perform(put("/api/offers/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFERED\",\"salaryNote\":\"面议\",\"remark\":\"已发 offer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFFERED"));

        mockMvc.perform(get("/api/candidates/" + candidateId + "/progress")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.screening.status").value("PASSED"))
                .andExpect(jsonPath("$.data.screening.managerStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data.offer.status").value("OFFERED"));

        mockMvc.perform(delete("/api/positions/" + positionId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.offeredCount").value(1));
    }

    @Test
    void managerRejectionBlocksInterviewScheduling() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionId = createPosition(managerToken, "主管驳回测试岗位");
        String candidateId = createCandidate(hrToken, positionId, "王五", "wangwu@example.com");

        mockMvc.perform(put("/api/screenings/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PASSED\",\"comment\":\"HR 初筛通过\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.managerStatus").value("PENDING"));

        mockMvc.perform(put("/api/manager-reviews/" + candidateId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\",\"comment\":\"部门暂不匹配\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.managerStatus").value("REJECTED"));

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewTime\":\"2026-05-02T10:00:00\",\"location\":\"会议室 B\",\"interviewer\":\"李经理\",\"status\":\"SCHEDULED\",\"evaluation\":\"待面试\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("候选人需经部门主管确认通过后才能安排面试"));
    }

    @Test
    void canceledInterviewIsNotCountedAsInterviewProgress() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        int before = overviewInterviewCount(hrToken);
        String positionId = createPosition(managerToken, "测试岗位");
        String candidateId = createCandidate(hrToken, positionId, "李四", "lisi@example.com");

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELED\",\"evaluation\":\"候选人临时取消\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.interviewCount").value(before));
    }

    @Test
    void hrCanUploadTxtResumeAndTriggerAiAnalysis() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionId = createPosition(managerToken, "AI 匹配岗位");
        String candidateId = createCandidate(hrToken, positionId, "赵六", "zhaoliu@example.com");
        when(aiClient.analyzeResume(contains("用于自动化测试"), contains("Spring Boot")))
                .thenReturn(new ResumeAnalysisResult(88, "匹配度较高，Java 与 Spring Boot 经历贴合岗位要求。"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "项目经历：负责 Spring Boot 招聘系统开发，熟悉 MySQL。".getBytes());

        mockMvc.perform(multipart("/api/candidates/" + candidateId + "/resume")
                        .file(file)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate.resumeOriginalFileName").value("resume.txt"))
                .andExpect(jsonPath("$.data.candidate.resumeContentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.data.candidate.resumeText").value("项目经历：负责 Spring Boot 招聘系统开发，熟悉 MySQL。"))
                .andExpect(jsonPath("$.data.screening.aiMatchScore").value(88))
                .andExpect(jsonPath("$.data.screening.aiQuickReview").value("匹配度较高，Java 与 Spring Boot 经历贴合岗位要求。"));

        verify(aiClient).analyzeResume(contains("用于自动化测试"), contains("Spring Boot 招聘系统"));
    }

    @Test
    void uploadRejectsUnsupportedResumeFileType() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionId = createPosition(managerToken, "附件类型测试岗位");
        String candidateId = createCandidate(hrToken, positionId, "钱七", "qianqi@example.com");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.png",
                "image/png",
                "fake image".getBytes());

        mockMvc.perform(multipart("/api/candidates/" + candidateId + "/resume")
                        .file(file)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("仅支持 PDF、DOCX、TXT 简历文件"));
    }

    @Test
    void uploadRejectsResumeWithoutExtractedText() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionId = createPosition(managerToken, "空简历测试岗位");
        String candidateId = createCandidate(hrToken, positionId, "孙八", "sunba@example.com");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "   \r\n\t   ".getBytes());

        mockMvc.perform(multipart("/api/candidates/" + candidateId + "/resume")
                        .file(file)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("未能从简历中提取到有效文本"));
    }

    @Test
    void manualAiAnalysisKeepsExistingResultWhenProviderFails() throws Exception {
        String hrToken = loginToken("admin", "admin123");
        String managerToken = loginToken("manager", "manager123");
        String positionId = createPosition(managerToken, "AI 失败保护岗位");
        String candidateId = createCandidate(hrToken, positionId, "周九", "zhoujiu@example.com");
        when(aiClient.analyzeResume(contains("用于自动化测试"), contains("候选人熟悉 Java")))
                .thenReturn(new ResumeAnalysisResult(76, "具备基础匹配度，可进一步复核项目深度。"));
        uploadTxtResume(hrToken, candidateId, "resume.txt", "候选人熟悉 Java、Spring Boot 与数据库。");
        ResumeScreening before = screenings.findByCandidateId(Long.parseLong(candidateId)).orElseThrow();
        when(aiClient.analyzeResume(eq(before.getCandidate().getPosition().getRequirements()), contains("候选人熟悉 Java")))
                .thenThrow(new BusinessException("AI 服务未配置，请设置 AI_API_KEY"));

        mockMvc.perform(post("/api/screenings/" + candidateId + "/ai-analysis")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("AI 服务未配置，请设置 AI_API_KEY"));

        mockMvc.perform(get("/api/candidates/" + candidateId + "/progress")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.screening.aiMatchScore").value(76))
                .andExpect(jsonPath("$.data.screening.aiQuickReview").value("具备基础匹配度，可进一步复核项目深度。"));
    }

    private String loginToken(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private String createPosition(String managerToken, String name) throws Exception {
        String body = mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","department":"测试部","headcount":1,
                                "requirements":"用于自动化测试","status":"OPEN"}
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"data\":\\{\"id\":([0-9]+).*", "$1");
    }

    private String createCandidate(String hrToken, String positionId, String name, String email) throws Exception {
        String body = mockMvc.perform(post("/api/candidates")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","gender":"女","phone":"13900139000","email":"%s",
                                "education":"本科","school":"测试大学","positionId":%s,"note":"流程测试"}
                                """.formatted(name, email, positionId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"data\":\\{\"id\":([0-9]+).*", "$1");
    }

    private int overviewInterviewCount(String token) throws Exception {
        String body = mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String value = body.replaceAll(".*\"interviewCount\":([0-9]+).*", "$1");
        return Integer.parseInt(value);
    }

    private void uploadTxtResume(String hrToken, String candidateId, String fileName, String text) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                text.getBytes());
        mockMvc.perform(multipart("/api/candidates/" + candidateId + "/resume")
                        .file(file)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());
    }
}
