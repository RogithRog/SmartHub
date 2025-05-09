package com.Management.SecureDocument.Document.controller;
import com.Management.SecureDocument.Document.dto.DocumentDTO;
import com.Management.SecureDocument.Document.model.Document;
import com.Management.SecureDocument.Document.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.List;
//import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, Principal principal) {
        Document document = documentService.storeFile(file, principal.getName());

        // Create success response with file URL
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/docs/download/")
                .path(document.getId().toString())
                .toUriString();

        return ResponseEntity.ok().body(Map.of(
                "id", document.getId(),
                "fileName", document.getFileName(),
                "fileType", document.getFileType(),
                "size", document.getFileSize(),
                "downloadUri", fileDownloadUri
        ));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = documentService.loadFileAsResource(id, principal.getName());
        Document document = documentService.getDocument(id, principal.getName());

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Fallback to the default content type if type could not be determined
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/user")
    public List<DocumentDTO> getUserDocuments(Principal principal) {
        return documentService.getUserDocuments(principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id, Principal principal) {
        Document document = documentService.getDocument(id, principal.getName());
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setUploadDate(document.getUploadDate());
        dto.setUsername(document.getUser().getUsername());

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Principal principal) {
        documentService.deleteDocument(id, principal.getName());
        return ResponseEntity.ok().body(Map.of("message", "Document deleted successfully"));
    }
}
