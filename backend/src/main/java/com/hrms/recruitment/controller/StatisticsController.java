package com.hrms.recruitment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.domain.InterviewStatus;
import com.hrms.recruitment.domain.OfferStatus;
import com.hrms.recruitment.domain.ScreeningStatus;
import com.hrms.recruitment.repository.CandidateRepository;
import com.hrms.recruitment.repository.InterviewRepository;
import com.hrms.recruitment.repository.OfferResultRepository;
import com.hrms.recruitment.repository.PositionRepository;
import com.hrms.recruitment.repository.ResumeScreeningRepository;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final List<InterviewStatus> ACTIVE_INTERVIEW_STATUSES =
            List.of(InterviewStatus.SCHEDULED, InterviewStatus.COMPLETED);

    private final PositionRepository positions;
    private final CandidateRepository candidates;
    private final ResumeScreeningRepository screenings;
    private final InterviewRepository interviews;
    private final OfferResultRepository offers;

    public StatisticsController(PositionRepository positions, CandidateRepository candidates,
            ResumeScreeningRepository screenings, InterviewRepository interviews, OfferResultRepository offers) {
        this.positions = positions;
        this.candidates = candidates;
        this.screenings = screenings;
        this.interviews = interviews;
        this.offers = offers;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewStats> overview() {
        return ApiResponse.ok(new OverviewStats(
                positions.count(),
                candidates.count(),
                screenings.countByStatus(ScreeningStatus.PENDING),
                interviews.countByStatusIn(ACTIVE_INTERVIEW_STATUSES),
                offers.countByStatus(OfferStatus.OFFERED)));
    }

    @GetMapping("/positions")
    public ApiResponse<List<PositionStats>> positions() {
        return ApiResponse.ok(positions.findAll().stream()
                .map(position -> new PositionStats(
                        position.getId(),
                        position.getName(),
                        position.getDepartment(),
                        candidates.countByPositionId(position.getId()),
                        interviews.countByCandidatePositionIdAndStatusIn(position.getId(), ACTIVE_INTERVIEW_STATUSES),
                        offers.countByCandidatePositionIdAndStatus(position.getId(), OfferStatus.OFFERED)))
                .toList());
    }

    @GetMapping("/pending-candidates")
    public ApiResponse<Long> pendingCandidates() {
        return ApiResponse.ok(screenings.countByStatus(ScreeningStatus.PENDING));
    }

    public record OverviewStats(long positionCount, long candidateCount, long pendingScreeningCount,
            long interviewCount, long offeredCount) {}

    public record PositionStats(Long positionId, String positionName, String department,
            long candidateCount, long interviewCount, long offeredCount) {}
}
