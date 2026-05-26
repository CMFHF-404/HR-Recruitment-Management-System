package com.hrms.recruitment.service.ai;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.ai.mode", havingValue = "FAST")
public class KeywordFastAiClient implements AiClient {
    private static final List<String> DOMAIN_KEYWORDS = List.of(
            "java", "spring boot", "spring", "mysql", "sql", "vue", "react", "python", "excel",
            "hr", "招聘", "筛选", "面试", "沟通", "协调", "数据", "统计", "报表", "管理",
            "本科", "硕士", "项目", "系统", "数据库", "前端", "后端", "开发", "测试");
    private static final Pattern ASCII_TERM = Pattern.compile("[a-zA-Z][a-zA-Z0-9+#.\\-]{1,}");

    @Override
    public ResumeAnalysisResult analyzeResume(String jobDescription, String resumeText) {
        Set<String> keywords = extractKeywords(jobDescription);
        String normalizedResume = normalize(resumeText);
        int matched = 0;
        for (String keyword : keywords) {
            if (normalizedResume.contains(keyword)) {
                matched++;
            }
        }
        int total = Math.max(keywords.size(), 1);
        int score = score(matched, total, normalizedResume);
        return new ResumeAnalysisResult(score, review(score, matched, total));
    }

    private Set<String> extractKeywords(String jobDescription) {
        String normalized = normalize(jobDescription);
        Set<String> keywords = new LinkedHashSet<>();
        for (String keyword : DOMAIN_KEYWORDS) {
            if (normalized.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        Matcher matcher = ASCII_TERM.matcher(jobDescription == null ? "" : jobDescription);
        while (matcher.find()) {
            String term = matcher.group().toLowerCase(Locale.ROOT);
            if (term.length() >= 3) {
                keywords.add(term);
            }
        }
        return keywords;
    }

    private int score(int matched, int total, String normalizedResume) {
        if (matched == 0) {
            return 25;
        }
        int score = 35 + Math.round(60f * matched / total);
        if (normalizedResume.contains("项目") || normalizedResume.contains("经验") || normalizedResume.contains("经历")) {
            score += 5;
        }
        return Math.max(0, Math.min(score, 95));
    }

    private String review(int score, int matched, int total) {
        if (score >= 80) {
            return "FAST本地模型：关键词匹配度较高，命中 " + matched + "/" + total + " 个岗位关键词，建议优先复核。";
        }
        if (score >= 60) {
            return "FAST本地模型：具备一定匹配度，命中 " + matched + "/" + total + " 个岗位关键词，建议人工复核。";
        }
        return "FAST本地模型：关键词命中较少，仅命中 " + matched + "/" + total + " 个岗位关键词，建议谨慎推进。";
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
