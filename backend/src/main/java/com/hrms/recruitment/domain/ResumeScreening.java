package com.hrms.recruitment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_screening")
public class ResumeScreening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScreeningStatus status = ScreeningStatus.PENDING;

    @Column(columnDefinition = "text")
    private String comment;

    private Integer aiMatchScore;

    @Column(columnDefinition = "text")
    private String aiQuickReview;

    private LocalDateTime screeningTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ManagerReviewStatus managerStatus = ManagerReviewStatus.NOT_SUBMITTED;

    @Column(columnDefinition = "text")
    private String managerComment;

    private LocalDateTime managerReviewTime;

    public Long getId() { return id; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    public ScreeningStatus getStatus() { return status; }
    public void setStatus(ScreeningStatus status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getAiMatchScore() { return aiMatchScore; }
    public void setAiMatchScore(Integer aiMatchScore) { this.aiMatchScore = aiMatchScore; }
    public String getAiQuickReview() { return aiQuickReview; }
    public void setAiQuickReview(String aiQuickReview) { this.aiQuickReview = aiQuickReview; }
    public LocalDateTime getScreeningTime() { return screeningTime; }
    public void setScreeningTime(LocalDateTime screeningTime) { this.screeningTime = screeningTime; }
    public ManagerReviewStatus getManagerStatus() { return managerStatus == null ? ManagerReviewStatus.NOT_SUBMITTED : managerStatus; }
    public void setManagerStatus(ManagerReviewStatus managerStatus) { this.managerStatus = managerStatus; }
    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }
    public LocalDateTime getManagerReviewTime() { return managerReviewTime; }
    public void setManagerReviewTime(LocalDateTime managerReviewTime) { this.managerReviewTime = managerReviewTime; }
}
