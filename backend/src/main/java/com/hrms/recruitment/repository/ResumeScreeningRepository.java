package com.hrms.recruitment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.recruitment.domain.ResumeScreening;
import com.hrms.recruitment.domain.ScreeningStatus;

public interface ResumeScreeningRepository extends JpaRepository<ResumeScreening, Long> {
    Optional<ResumeScreening> findByCandidateId(Long candidateId);
    long countByStatus(ScreeningStatus status);
    void deleteByCandidateId(Long candidateId);
    Page<ResumeScreening> findAllByOrderByIdDesc(Pageable pageable);
    Page<ResumeScreening> findByStatusOrderByIdDesc(ScreeningStatus status, Pageable pageable);
}
