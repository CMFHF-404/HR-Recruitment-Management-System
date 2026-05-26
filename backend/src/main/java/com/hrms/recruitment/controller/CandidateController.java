package com.hrms.recruitment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.common.BusinessException;
import com.hrms.recruitment.domain.Candidate;
import com.hrms.recruitment.domain.Interview;
import com.hrms.recruitment.domain.OfferResult;
import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.repository.InterviewRepository;
import com.hrms.recruitment.repository.OfferResultRepository;
import com.hrms.recruitment.repository.ResumeScreeningRepository;
import com.hrms.recruitment.service.RecruitmentService;
import com.hrms.recruitment.service.RecruitmentService.CandidateResumeUpload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    private final RecruitmentService service;
    private final ResumeScreeningRepository screenings;
    private final InterviewRepository interviews;
    private final OfferResultRepository offers;

    public CandidateController(RecruitmentService service, ResumeScreeningRepository screenings,
            InterviewRepository interviews, OfferResultRepository offers) {
        this.service = service;
        this.screenings = screenings;
        this.interviews = interviews;
        this.offers = offers;
    }

    @GetMapping
    public ApiResponse<Page<Candidate>> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long positionId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(service.searchCandidates(keyword, positionId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Candidate> get(@PathVariable Long id) {
        return ApiResponse.ok(service.getCandidate(id));
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<CandidateProgress> progress(@PathVariable Long id) {
        Candidate candidate = service.getCandidate(id);
        ResumeScreening screening = screenings.findByCandidateId(id)
                .orElseThrow(() -> new BusinessException("筛选记录不存在"));
        Interview interview = interviews.findByCandidateId(id)
                .orElseThrow(() -> new BusinessException("面试记录不存在"));
        OfferResult offer = offers.findByCandidateId(id)
                .orElseThrow(() -> new BusinessException("录用记录不存在"));
        return ApiResponse.ok(new CandidateProgress(candidate, screening, interview, offer));
    }

    @PostMapping
    public ApiResponse<Candidate> create(@Valid @RequestBody CandidateRequest request) {
        Candidate candidate = toEntity(new Candidate(), request);
        return ApiResponse.ok("候选人已登记", service.saveCandidate(candidate, request.positionId()));
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<CandidateResumeUpload> uploadResume(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        CandidateResumeUpload result = service.uploadCandidateResume(id, file);
        String message = result.analysisSucceeded()
                ? "简历已上传并完成 AI 分析"
                : "简历已上传，AI 分析失败，可稍后重试：" + result.analysisMessage();
        return ApiResponse.ok(message, result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Candidate> update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        Candidate candidate = toEntity(service.getCandidate(id), request);
        return ApiResponse.ok("候选人已更新", service.saveCandidate(candidate, request.positionId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteCandidate(id);
        return ApiResponse.ok("候选人及流程记录已删除", null);
    }

    private Candidate toEntity(Candidate candidate, CandidateRequest request) {
        candidate.setName(request.name());
        candidate.setGender(request.gender());
        candidate.setPhone(request.phone());
        candidate.setEmail(request.email());
        candidate.setEducation(request.education());
        candidate.setSchool(request.school());
        candidate.setNote(request.note());
        return candidate;
    }

    public record CandidateRequest(
            @NotBlank String name,
            @NotBlank String gender,
            @NotBlank @Pattern(regexp = "^1?\\d{10,11}$|^\\+?[0-9\\- ]{7,20}$", message = "格式不正确") String phone,
            @NotBlank @Email String email,
            @NotBlank String education,
            @NotBlank String school,
            @NotNull Long positionId,
            String note) {}

    public record CandidateProgress(Candidate candidate, ResumeScreening screening, Interview interview, OfferResult offer) {}
}
