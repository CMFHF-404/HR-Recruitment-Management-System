package com.hrms.recruitment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class HrRecruitmentManagementSystemApplicationTests {
    @Autowired
    MockMvc mockMvc;

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
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void recruitmentFlowCanBeManagedEndToEnd() throws Exception {
        String token = loginToken();
        String positionResult = mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + token)
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
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"张三","gender":"男","phone":"13800138000","email":"zhangsan@example.com",
                                "education":"本科","school":"示例大学","positionId":%s,"note":"课程设计测试数据"}
                                """.formatted(positionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.position.id").value(Integer.parseInt(positionId)))
                .andReturn().getResponse().getContentAsString();
        String candidateId = candidateResult.replaceAll(".*\"id\":([0-9]+).*", "$1");

        mockMvc.perform(put("/api/screenings/" + candidateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PASSED\",\"comment\":\"符合岗位要求\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSED"));

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewTime\":\"2026-05-01T10:00:00\",\"location\":\"会议室 A\",\"interviewer\":\"李经理\",\"status\":\"SCHEDULED\",\"evaluation\":\"待面试\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        mockMvc.perform(put("/api/offers/" + candidateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFERED\",\"salaryNote\":\"面议\",\"remark\":\"已发 offer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFFERED"));

        mockMvc.perform(get("/api/candidates/" + candidateId + "/progress")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.screening.status").value("PASSED"))
                .andExpect(jsonPath("$.data.offer.status").value("OFFERED"));

        mockMvc.perform(delete("/api/positions/" + positionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.offeredCount").value(1));
    }

    @Test
    void canceledInterviewIsNotCountedAsInterviewProgress() throws Exception {
        String token = loginToken();
        int before = overviewInterviewCount(token);

        String positionResult = mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"测试岗位","department":"测试部","headcount":1,
                                "requirements":"用于验证取消面试统计","status":"OPEN"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String positionId = positionResult.replaceAll(".*\"id\":([0-9]+).*", "$1");

        String candidateResult = mockMvc.perform(post("/api/candidates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"李四","gender":"女","phone":"13900139000","email":"lisi@example.com",
                                "education":"本科","school":"测试大学","positionId":%s,"note":"取消面试统计测试"}
                                """.formatted(positionId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String candidateId = candidateResult.replaceAll(".*\"id\":([0-9]+).*", "$1");

        mockMvc.perform(put("/api/interviews/" + candidateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELED\",\"evaluation\":\"候选人临时取消\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.interviewCount").value(before));
    }

    private String loginToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private int overviewInterviewCount(String token) throws Exception {
        String body = mockMvc.perform(get("/api/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String value = body.replaceAll(".*\"interviewCount\":([0-9]+).*", "$1");
        return Integer.parseInt(value);
    }
}
