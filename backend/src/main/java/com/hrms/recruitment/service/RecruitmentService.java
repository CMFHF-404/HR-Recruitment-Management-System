package com.hrms.recruitment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ResumeTextExtractor resumeTextExtractor;
    private final ResumeAnalysisService resumeAnalysisService;
    private final Path resumeUploadDir;

    public RecruitmentService(PositionRepository positions, CandidateRepository candidates,
            ResumeScreeningRepository screenings, InterviewRepository interviews, OfferResultRepository offers,
            ResumeTextExtractor resumeTextExtractor, ResumeAnalysisService resumeAnalysisService,
            @Value("${app.resume.upload-dir:uploads/resumes}") String resumeUploadDir) {
        this.positions = positions;
        this.candidates = candidates;
        this.screenings = screenings;
        this.interviews = interviews;
        this.offers = offers;
        this.resumeTextExtractor = resumeTextExtractor;
        this.resumeAnalysisService = resumeAnalysisService;
        this.resumeUploadDir = Paths.get(resumeUploadDir).toAbsolutePath().normalize();
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
        Candidate candidate = getCandidate(id);
        screenings.deleteByCandidateId(id);
        interviews.deleteByCandidateId(id);
        offers.deleteByCandidateId(id);
        candidates.deleteById(id);
        deleteStoredResume(candidate);
    }

    @Transactional
    public CandidateResumeUpload uploadCandidateResume(Long candidateId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("简历文件不能为空");
        }
        Candidate candidate = getCandidate(candidateId);
        byte[] bytes = readBytes(file);
        String originalFileName = cleanFileName(file.getOriginalFilename());
        String resumeText = resumeTextExtractor.extract(originalFileName, bytes);
        Path storedPath = storeResumeFile(candidateId, originalFileName, bytes);

        deleteStoredResume(candidate);
        candidate.setResumeOriginalFileName(originalFileName);
        candidate.setResumeContentType(file.getContentType());
        candidate.setResumeStoragePath(storedPath.toString());
        candidate.setResumeText(resumeText);
        candidate.setResumeUploadedAt(LocalDateTime.now());
        Candidate saved = candidates.save(candidate);

        ResumeScreening screening = screenings.findByCandidateId(saved.getId())
                .orElseThrow(() -> new BusinessException("筛选记录不存在"));
        try {
            screening = resumeAnalysisService.analyzeAndSave(saved);
            return new CandidateResumeUpload(saved, screening, true, "AI 分析完成");
        } catch (BusinessException ex) {
            return new CandidateResumeUpload(saved, screening, false, ex.getMessage());
        }
    }

    @Transactional
    public ResumeScreening analyzeResume(Long candidateId) {
        return resumeAnalysisService.analyzeAndSave(getCandidate(candidateId));
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
        if (status != OfferStatus.PENDING) {
            Interview interview = interviews.findByCandidateId(candidateId)
                    .orElseThrow(() -> new BusinessException("面试记录不存在"));
            if (interview.getStatus() != InterviewStatus.COMPLETED) {
                throw new BusinessException("候选人需完成面试后才能登记录用结果");
            }
        }
        offer.setStatus(status);
        offer.setSalaryNote(salaryNote);
        offer.setRemark(remark);
        offer.setRegisteredAt(LocalDateTime.now());
        return offers.save(offer);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("简历文件读取失败：" + ex.getMessage());
        }
    }

    private Path storeResumeFile(Long candidateId, String originalFileName, byte[] bytes) {
        try {
            Files.createDirectories(resumeUploadDir);
            String extension = extensionOf(originalFileName);
            Path target = resumeUploadDir.resolve(
                    "candidate-" + candidateId + "-" + UUID.randomUUID() + "." + extension).normalize();
            if (!target.startsWith(resumeUploadDir)) {
                throw new BusinessException("简历文件名不安全");
            }
            Files.write(target, bytes);
            return target;
        } catch (IOException ex) {
            throw new BusinessException("简历文件保存失败：" + ex.getMessage());
        }
    }

    private void deleteStoredResume(Candidate candidate) {
        if (candidate.getResumeStoragePath() == null || candidate.getResumeStoragePath().isBlank()) {
            return;
        }
        try {
            Path stored = Paths.get(candidate.getResumeStoragePath()).toAbsolutePath().normalize();
            if (stored.startsWith(resumeUploadDir)) {
                Files.deleteIfExists(stored);
            }
        } catch (IOException ignored) {
        }
    }

    private String cleanFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
        }
        String cleaned = fileName.replace("\\", "/");
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1).trim();
        if (cleaned.isBlank()) {
            throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
        }
        extensionOf(cleaned);
        return cleaned;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!extension.equals("pdf") && !extension.equals("docx") && !extension.equals("txt")) {
            throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
        }
        return extension;
    }

    public record CandidateResumeUpload(
            Candidate candidate,
            ResumeScreening screening,
            boolean analysisSucceeded,
            String analysisMessage) {}
}
