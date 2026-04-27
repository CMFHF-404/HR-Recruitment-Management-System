package com.hrms.recruitment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.recruitment.domain.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query("""
            select p from Position p
            where :keyword is null
               or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(p.department) like lower(concat('%', :keyword, '%'))
            order by p.createdAt desc
            """)
    Page<Position> search(@Param("keyword") String keyword, Pageable pageable);
}
