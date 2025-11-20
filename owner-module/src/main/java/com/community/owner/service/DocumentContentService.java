package com.community.owner.service;

import com.community.owner.domain.entity.SmartQaKnowledge;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
public class DocumentContentService {
    
    @Value("${file.upload.path:uploads/}")
    private String uploadPath;
    
    /**
     * 提取文档内容
     */
    public String extractDocumentContent(SmartQaKnowledge doc) throws Exception {
        String filePath = uploadPath + doc.getFilePath();
        String fileType = doc.getFileType().toLowerCase();
        
        switch (fileType) {
            case "txt":
                return extractTxtContent(filePath);
            case "pdf":
                return extractPdfContent(filePath);
            case "doc":
            case "docx":
                return extractWordContent(filePath);
            default:
                throw new Exception("不支持的文件类型: " + fileType);
        }
    }
    
    /**
     * 提取TXT文件内容
     */
    private String extractTxtContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }
    
    /**
     * 提取PDF文件内容
     */
    private String extractPdfContent(String filePath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
    
    /**
     * 提取Word文件内容
     */
    private String extractWordContent(String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        
        if (filePath.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(new FileInputStream(filePath))) {
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    content.append(paragraph.getText()).append("\n");
                }
            }
        } else {
            try (HWPFDocument document = new HWPFDocument(new FileInputStream(filePath))) {
                WordExtractor extractor = new WordExtractor(document);
                content.append(extractor.getText());
                extractor.close();
            }
        }
        
        return content.toString();
    }
    
    /**
     * 获取文档摘要（用于预览）
     */
    public String getDocumentSummary(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
