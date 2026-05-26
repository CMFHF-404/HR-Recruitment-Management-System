package com.hrms.recruitment.service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KeywordFastAiClientTests {
    @Test
    void scoresHighWhenResumeContainsMostJobKeywords() {
        KeywordFastAiClient client = new KeywordFastAiClient();

        ResumeAnalysisResult result = client.analyzeResume(
                "熟悉 Java、Spring Boot、MySQL，具备良好的沟通能力。",
                "候选人参与 Java 招聘系统开发，使用 Spring Boot 和 MySQL，负责跨部门沟通。");

        assertThat(result.matchScore()).isGreaterThanOrEqualTo(85);
        assertThat(result.quickReview()).contains("FAST本地模型").contains("命中");
    }

    @Test
    void scoresLowWhenResumeMissesJobKeywords() {
        KeywordFastAiClient client = new KeywordFastAiClient();

        ResumeAnalysisResult result = client.analyzeResume(
                "熟悉 Java、Spring Boot、MySQL。",
                "候选人主要负责行政接待和文档整理。");

        assertThat(result.matchScore()).isLessThan(60);
        assertThat(result.quickReview()).contains("关键词命中较少");
    }
}
