package com.hrms.recruitment.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.domain.Interview;
import com.hrms.recruitment.domain.InterviewStatus;
import com.hrms.recruitment.repository.InterviewRepository;
import com.hrms.recruitment.service.RecruitmentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewRepository interviews;
    private final RecruitmentService service;

    public InterviewController(InterviewRepository interviews, RecruitmentService service) {
        this.interviews = interviews;
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<Interview>> list(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(interviews.findAllReadyForInterview(pageable));
    }

    @PutMapping("/{candidateId}")
    public ApiResponse<Interview> update(@PathVariable Long candidateId, @Valid @RequestBody InterviewRequest request) {
        return ApiResponse.ok("面试信息已更新", service.updateInterview(candidateId,
                request.interviewTime(), request.location(), request.interviewer(), request.status(), request.evaluation()));
    }

    public record InterviewRequest(LocalDateTime interviewTime, String location, String interviewer,
            @NotNull InterviewStatus status, String evaluation) {}
}
