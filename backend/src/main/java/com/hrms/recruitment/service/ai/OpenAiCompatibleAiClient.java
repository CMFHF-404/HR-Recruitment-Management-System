package com.hrms.recruitment.service.ai;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.recruitment.common.BusinessException;

@Service
@ConditionalOnProperty(name = "app.ai.mode", havingValue = "REMOTE", matchIfMissing = true)
public class OpenAiCompatibleAiClient implements AiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiCompatibleAiClient(
            RestTemplateBuilder builder,
            ObjectMapper objectMapper,
            @Value("${app.ai.api-key:${AI_API_KEY:}}") String apiKey,
            @Value("${app.ai.base-url:${AI_BASE_URL:https://api.openai.com/v1}}") String baseUrl,
            @Value("${app.ai.model:${AI_MODEL:gpt-4o-mini}}") String model) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(45))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = trimTrailingSlash(baseUrl == null ? "" : baseUrl.trim());
        this.model = model == null ? "" : model.trim();
    }

    @Override
    public ResumeAnalysisResult analyzeResume(String jobDescription, String resumeText) {
        if (apiKey.isBlank()) {
            throw new BusinessException("AI 服务未配置，请设置 AI_API_KEY");
        }
        if (model.isBlank()) {
            throw new BusinessException("AI 服务未配置，请设置 AI_MODEL");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        你是招聘系统中的简历匹配助手。只输出 JSON，不要输出 Markdown。
                                        JSON 结构必须是 {"matchScore": 0-100整数, "quickReview": "不超过80字的中文评价"}。
                                        评分应依据岗位 JD 与候选人简历文本的技能、经验、教育和职责匹配度。
                                        """),
                        Map.of(
                                "role", "user",
                                "content", "岗位JD：\n" + jobDescription + "\n\n候选人简历：\n" + resumeText)));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            JsonNode response = restTemplate.postForObject(
                    chatCompletionsUrl(),
                    new HttpEntity<>(payload, headers),
                    JsonNode.class);
            String content = response == null ? null : response.path("choices").path(0).path("message").path("content").asText(null);
            return parseResult(content);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BusinessException("AI 服务调用失败：" + safeMessage(ex));
        }
    }

    private ResumeAnalysisResult parseResult(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("AI 服务返回内容为空");
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            if (!root.has("matchScore") || !root.has("quickReview")) {
                throw new BusinessException("AI 服务返回格式无效");
            }
            int score = clamp(root.path("matchScore").asInt(-1));
            String review = root.path("quickReview").asText("").trim();
            if (score < 0 || review.isBlank()) {
                throw new BusinessException("AI 服务返回格式无效");
            }
            return new ResumeAnalysisResult(score, review);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("AI 服务返回格式无效");
        }
    }

    private int clamp(int value) {
        if (value < 0) return -1;
        if (value > 100) return 100;
        return value;
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String chatCompletionsUrl() {
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}
