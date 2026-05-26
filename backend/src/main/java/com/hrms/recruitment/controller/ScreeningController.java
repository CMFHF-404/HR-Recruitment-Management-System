package com.hrms.recruitment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.domain.ScreeningStatus;
import com.hrms.recruitment.repository.ResumeScreeningRepository;
import com.hrms.recruitment.service.RecruitmentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {
    private final ResumeScreeningRepository screenings;
    private final RecruitmentService service;

    public ScreeningController(ResumeScreeningRepository screenings, RecruitmentService service) {
        this.screenings = screenings;
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<ResumeScreening>> list(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(screenings.findAllByOrderByIdDesc(pageable));
    }

    @PutMapping("/{candidateId}")
    public ApiResponse<ResumeScreening> update(@PathVariable Long candidateId,
            @Valid @RequestBody ScreeningRequest request) {
        return ApiResponse.ok("筛选状态已更新",
                service.updateScreening(candidateId, request.status(), request.comment()));
    }

    @PostMapping("/{candidateId}/ai-analysis")
    public ApiResponse<ResumeScreening> analyze(@PathVariable Long candidateId) {
        return ApiResponse.ok("AI 分析已更新", service.analyzeResume(candidateId));
    }

    public record ScreeningRequest(@NotNull ScreeningStatus status, String comment) {}
}
