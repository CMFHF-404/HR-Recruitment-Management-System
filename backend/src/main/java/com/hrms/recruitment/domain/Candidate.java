package com.hrms.recruitment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidate")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 50)
    private String education;

    @Column(nullable = false, length = 120)
    private String school;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(columnDefinition = "text")
    private String note;

    @Column(length = 255)
    private String resumeOriginalFileName;

    @Column(length = 120)
    private String resumeContentType;

    @Column(length = 500)
    private String resumeStoragePath;

    @Column(columnDefinition = "text")
    private String resumeText;

    private LocalDateTime resumeUploadedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getResumeOriginalFileName() { return resumeOriginalFileName; }
    public void setResumeOriginalFileName(String resumeOriginalFileName) { this.resumeOriginalFileName = resumeOriginalFileName; }
    public String getResumeContentType() { return resumeContentType; }
    public void setResumeContentType(String resumeContentType) { this.resumeContentType = resumeContentType; }
    public String getResumeStoragePath() { return resumeStoragePath; }
    public void setResumeStoragePath(String resumeStoragePath) { this.resumeStoragePath = resumeStoragePath; }
    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
    public LocalDateTime getResumeUploadedAt() { return resumeUploadedAt; }
    public void setResumeUploadedAt(LocalDateTime resumeUploadedAt) { this.resumeUploadedAt = resumeUploadedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
