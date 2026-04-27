package com.hrms.recruitment.controller;

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
import com.hrms.recruitment.domain.OfferResult;
import com.hrms.recruitment.domain.OfferStatus;
import com.hrms.recruitment.repository.OfferResultRepository;
import com.hrms.recruitment.service.RecruitmentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferResultRepository offers;
    private final RecruitmentService service;

    public OfferController(OfferResultRepository offers, RecruitmentService service) {
        this.offers = offers;
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<OfferResult>> list(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(offers.findAllByOrderByIdDesc(pageable));
    }

    @PutMapping("/{candidateId}")
    public ApiResponse<OfferResult> update(@PathVariable Long candidateId, @Valid @RequestBody OfferRequest request) {
        return ApiResponse.ok("录用结果已更新",
                service.updateOffer(candidateId, request.status(), request.salaryNote(), request.remark()));
    }

    public record OfferRequest(@NotNull OfferStatus status, String salaryNote, String remark) {}
}
