package com.hrms.recruitment.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.domain.Position;
import com.hrms.recruitment.domain.PositionStatus;
import com.hrms.recruitment.service.RecruitmentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/positions")
public class PositionController {
    private final RecruitmentService service;

    public PositionController(RecruitmentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<Position>> list(@RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(service.searchPositions(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Position> get(@PathVariable Long id) {
        return ApiResponse.ok(service.getPosition(id));
    }

    @PostMapping
    public ApiResponse<Position> create(@Valid @RequestBody PositionRequest request) {
        return ApiResponse.ok("岗位已创建", service.savePosition(toEntity(new Position(), request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Position> update(@PathVariable Long id, @Valid @RequestBody PositionRequest request) {
        Position position = service.getPosition(id);
        return ApiResponse.ok("岗位已更新", service.savePosition(toEntity(position, request)));
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<Position> close(@PathVariable Long id) {
        Position position = service.getPosition(id);
        position.setStatus(PositionStatus.CLOSED);
        return ApiResponse.ok("岗位已关闭", service.savePosition(position));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deletePosition(id);
        return ApiResponse.ok("岗位已删除", null);
    }

    private Position toEntity(Position position, PositionRequest request) {
        position.setName(request.name());
        position.setDepartment(request.department());
        position.setHeadcount(request.headcount());
        position.setRequirements(request.requirements());
        position.setPublishDate(request.publishDate() == null ? LocalDate.now() : request.publishDate());
        position.setStatus(request.status() == null ? PositionStatus.OPEN : request.status());
        return position;
    }

    public record PositionRequest(
            @NotBlank String name,
            @NotBlank String department,
            @NotNull @Min(1) Integer headcount,
            @NotBlank String requirements,
            LocalDate publishDate,
            PositionStatus status) {}
}
