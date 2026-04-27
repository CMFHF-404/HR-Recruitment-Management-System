package com.hrms.recruitment.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.recruitment.common.BusinessException;
import com.hrms.recruitment.domain.Candidate;
import com.hrms.recruitment.domain.Interview;
import com.hrms.recruitment.domain.InterviewStatus;
import com.hrms.recruitment.domain.ManagerReviewStatus;
import com.hrms.recruitment.domain.OfferResult;
import com.hrms.recruitment.domain.OfferStatus;
import com.hrms.recruitment.domain.Position;
import com.hrms.recruitment.domain.PositionStatus;
import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.domain.ScreeningStatus;
import com.hrms.recruitment.repository.CandidateRepository;
import com.hrms.recruitment.repository.InterviewRepository;
import com.hrms.recruitment.repository.OfferResultRepository;
import com.hrms.recruitment.repository.PositionRepository;
import com.hrms.recruitment.repository.ResumeScreeningRepository;

@Service
public class RecruitmentService {
    private final PositionRepository positions;
    private final CandidateRepository candidates;
    private final ResumeScreeningRepository screenings;
    private final InterviewRepository interviews;
    private final OfferResultRepository offers;

    public RecruitmentService(PositionRepository positions, CandidateRepository candidates,
            ResumeScreeningRepository screenings, InterviewRepository interviews, OfferResultRepository offers) {
        this.positions = positions;
        this.candidates = candidates;
        this.screenings = screenings;
        this.interviews = interviews;
        this.offers = offers;
    }

    public Position getPosition(Long id) {
        return positions.findById(id).orElseThrow(() -> new BusinessException("岗位不存在"));
    }

    public Candidate getCandidate(Long id) {
        return candidates.findById(id).orElseThrow(() -> new BusinessException("候选人不存在"));
    }

    public Page<Position> searchPositions(String keyword, Pageable pageable) {
        return positions.search(blankToNull(keyword), pageable);
    }

    @Transactional
    public Position savePosition(Position position) {
        if (position.getStatus() == null) {
            position.setStatus(PositionStatus.OPEN);
        }
        return positions.save(position);
    }

    @Transactional
    public void deletePosition(Long id) {
        if (candidates.existsByPositionId(id)) {
            throw new BusinessException("该岗位已有候选人，不能删除，可改为已关闭");
        }
        positions.delete(getPosition(id));
    }

    @Transactional
    public Candidate saveCandidate(Candidate candidate, Long positionId) {
        candidate.setPosition(getPosition(positionId));
        Candidate saved = candidates.save(candidate);
        screenings.findByCandidateId(saved.getId()).orElseGet(() -> {
            ResumeScreening screening = new ResumeScreening();
            screening.setCandidate(saved);
            return screenings.save(screening);
        });
        interviews.findByCandidateId(saved.getId()).orElseGet(() -> {
            Interview interview = new Interview();
            interview.setCandidate(saved);
            return interviews.save(interview);
        });
        offers.findByCandidateId(saved.getId()).orElseGet(() -> {
            OfferResult offer = new OfferResult();
            offer.setCandidate(saved);
            return offers.save(offer);
        });
        return saved;
    }

    public Page<Candidate> searchCandidates(String keyword, Long positionId, Pageable pageable) {
        return candidates.search(blankToNull(keyword), positionId, pageable);
    }

    @Transactional
    public void deleteCandidate(Long id) {
        getCandidate(id);
        screenings.deleteByCandidateId(id);
        interviews.deleteByCandidateId(id);
        offers.deleteByCandidateId(id);
        candidates.deleteById(id);
    }

    @Transactional
    public ResumeScreening updateScreening(Long candidateId, ScreeningStatus status, String comment) {
        ResumeScreening screening = screenings.findByCandidateId(candidateId)
                .orElseThrow(() -> new BusinessException("筛选记录不存在"));
        ScreeningStatus previousStatus = screening.getStatus();
        screening.setStatus(status);
        screening.setComment(comment);
        screening.setScreeningTime(LocalDateTime.now());
        if (status == ScreeningStatus.PASSED && previousStatus != ScreeningStatus.PASSED) {
            screening.setManagerStatus(ManagerReviewStatus.PENDING);
            screening.setManagerComment(null);
            screening.setManagerReviewTime(null);
        } else if (status != ScreeningStatus.PASSED) {
            screening.setManagerStatus(ManagerReviewStatus.NOT_SUBMITTED);
            screening.setManagerComment(null);
            screening.setManagerReviewTime(null);
        }
        return screenings.save(screening);
    }

    @Transactional
    public ResumeScreening updateManagerReview(Long candidateId, ManagerReviewStatus status, String comment) {
        ResumeScreening screening = screenings.findByCandidateId(candidateId)
                .orElseThrow(() -> new BusinessException("筛选记录不存在"));
        if (screening.getStatus() != ScreeningStatus.PASSED) {
            throw new BusinessException("只有 HR 初筛通过的候选人才能进行主管确认");
        }
        if (status != ManagerReviewStatus.APPROVED && status != ManagerReviewStatus.REJECTED) {
            throw new BusinessException("主管确认只能选择通过或驳回");
        }
        screening.setManagerStatus(status);
        screening.setManagerComment(comment);
        screening.setManagerReviewTime(LocalDateTime.now());
        return screenings.save(screening);
    }

    @Transactional
    public Interview updateInterview(Long candidateId, LocalDateTime interviewTime, String location,
            String interviewer, InterviewStatus status, String evaluation) {
        Interview interview = interviews.findByCandidateId(candidateId)
                .orElseThrow(() -> new BusinessException("面试记录不存在"));
        if (status == InterviewStatus.SCHEDULED && interviewTime == null) {
            throw new BusinessException("安排面试时必须填写面试时间");
        }
        if (status == InterviewStatus.SCHEDULED || status == InterviewStatus.COMPLETED) {
            ResumeScreening screening = screenings.findByCandidateId(candidateId)
                    .orElseThrow(() -> new BusinessException("筛选记录不存在"));
            if (screening.getStatus() != ScreeningStatus.PASSED
                    || screening.getManagerStatus() != ManagerReviewStatus.APPROVED) {
                throw new BusinessException("候选人需经部门主管确认通过后才能安排面试");
            }
        }
        interview.setInterviewTime(interviewTime);
        interview.setLocation(location);
        interview.setInterviewer(interviewer);
        interview.setStatus(status);
        interview.setEvaluation(evaluation);
        return interviews.save(interview);
    }

    @Transactional
    public OfferResult updateOffer(Long candidateId, OfferStatus status, String salaryNote, String remark) {
        OfferResult offer = offers.findByCandidateId(candidateId)
                .orElseThrow(() -> new BusinessException("录用记录不存在"));
        offer.setStatus(status);
        offer.setSalaryNote(salaryNote);
        offer.setRemark(remark);
        offer.setRegisteredAt(LocalDateTime.now());
        return offers.save(offer);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
