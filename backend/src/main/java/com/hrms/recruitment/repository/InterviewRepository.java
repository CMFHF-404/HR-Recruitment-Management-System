package com.hrms.recruitment.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.recruitment.domain.Interview;
import com.hrms.recruitment.domain.InterviewStatus;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Optional<Interview> findByCandidateId(Long candidateId);
    long countByStatusNot(InterviewStatus status);
    long countByCandidatePositionIdAndStatusNot(Long positionId, InterviewStatus status);
    long countByStatusIn(Collection<InterviewStatus> statuses);
    long countByCandidatePositionIdAndStatusIn(Long positionId, Collection<InterviewStatus> statuses);
    void deleteByCandidateId(Long candidateId);
    Page<Interview> findAllByOrderByIdDesc(Pageable pageable);
}
