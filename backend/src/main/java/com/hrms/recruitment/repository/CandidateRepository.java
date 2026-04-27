package com.hrms.recruitment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.recruitment.domain.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    boolean existsByPositionId(Long positionId);
    long countByPositionId(Long positionId);

    @Query("""
            select c from Candidate c
            where (:positionId is null or c.position.id = :positionId)
              and (:keyword is null
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(c.phone) like lower(concat('%', :keyword, '%'))
                or lower(c.email) like lower(concat('%', :keyword, '%')))
            order by c.createdAt desc
            """)
    Page<Candidate> search(@Param("keyword") String keyword, @Param("positionId") Long positionId, Pageable pageable);
}
