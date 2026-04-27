package com.hrms.recruitment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.recruitment.domain.OfferResult;
import com.hrms.recruitment.domain.OfferStatus;

public interface OfferResultRepository extends JpaRepository<OfferResult, Long> {
    Optional<OfferResult> findByCandidateId(Long candidateId);
    long countByStatus(OfferStatus status);
    long countByCandidatePositionIdAndStatus(Long positionId, OfferStatus status);
    void deleteByCandidateId(Long candidateId);
    Page<OfferResult> findAllByOrderByIdDesc(Pageable pageable);
}
