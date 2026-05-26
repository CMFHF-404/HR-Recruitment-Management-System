package com.hrms.recruitment.service;

import org.springframework.stereotype.Service;

import com.hrms.recruitment.common.BusinessException;
import com.hrms.recruitment.domain.Candidate;
import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.repository.ResumeScreeningRepository;
import com.hrms.recruitment.service.ai.AiClient;
import com.hrms.recruitment.service.ai.ResumeAnalysisResult;

@Service
public class ResumeAnalysisService {
    private final ResumeScreeningRepository screenings;
    private final AiClient aiClient;

    public ResumeAnalysisService(ResumeScreeningRepository screenings, AiClient aiClient) {
        this.screenings = screenings;
        this.aiClient = aiClient;
    }

    public ResumeScreening analyzeAndSave(Candidate candidate) {
        if (candidate.getPosition() == null || isBlank(candidate.getPosition().getRequirements())) {
            throw new BusinessException("岗位 JD 不能为空");
        }
        if (isBlank(candidate.getResumeText())) {
            throw new BusinessException("候选人尚未上传可分析的简历");
        }
        ResumeScreening screening = screenings.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new BusinessException("筛选记录不存在"));
        ResumeAnalysisResult result = aiClient.analyzeResume(
                candidate.getPosition().getRequirements(),
                candidate.getResumeText());
        screening.setAiMatchScore(clamp(result.matchScore()));
        screening.setAiQuickReview(result.quickReview() == null ? "" : result.quickReview().trim());
        return screenings.save(screening);
    }

    private int clamp(int value) {
        if (value < 0) return 0;
        if (value > 100) return 100;
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
