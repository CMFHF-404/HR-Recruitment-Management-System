package com.hrms.recruitment.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import com.hrms.recruitment.common.BusinessException;

@Service
public class ResumeTextExtractor {
    public String extract(String fileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException("简历文件不能为空");
        }
        String extension = extensionOf(fileName);
        try {
            String text = switch (extension) {
                case "txt" -> new String(bytes, StandardCharsets.UTF_8);
                case "pdf" -> extractPdf(bytes);
                case "docx" -> extractDocx(bytes);
                default -> throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
            };
            String normalized = normalize(text);
            if (normalized.isBlank()) {
                throw new BusinessException("未能从简历中提取到有效文本");
            }
            return normalized;
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException("简历文件解析失败：" + ex.getMessage());
        }
    }

    private String extractPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractDocx(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            return document.getParagraphs().stream()
                    .map(paragraph -> paragraph.getText())
                    .collect(Collectors.joining("\n"));
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException("仅支持 PDF、DOCX、TXT 简历文件");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }
}
