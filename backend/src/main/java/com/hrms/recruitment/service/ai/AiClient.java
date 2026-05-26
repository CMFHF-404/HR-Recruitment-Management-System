package com.hrms.recruitment.service.ai;

public interface AiClient {
    ResumeAnalysisResult analyzeResume(String jobDescription, String resumeText);
}
