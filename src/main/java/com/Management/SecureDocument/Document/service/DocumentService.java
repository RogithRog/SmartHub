package com.Management.SecureDocument.Document.service;

import com.Management.SecureDocument.Access.model.User;
import com.Management.SecureDocument.Access.repository.UserRepository;
import com.Management.SecureDocument.Document.dto.DocumentDTO;
import com.Management.SecureDocument.Document.exception.DocumentNotFoundException;
import com.Management.SecureDocument.Document.exception.FileStorageException;
import com.Management.SecureDocument.Document.model.Document;
import com.Management.SecureDocument.Document.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final Path fileStorageLocation;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository,
                           UserRepository userRepository,
                           @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;

        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Document storeFile(MultipartFile file, String username) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence " + originalFileName);
            }

            // Validate file type
            validateFileType(file);

            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate unique file name
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create new document record
            Document document = new Document();
            document.setFileName(originalFileName);
            document.setFileType(file.getContentType());
            document.setFilePath(uniqueFileName);
            document.setFileSize(file.getSize());
            document.setUser(user);

            return documentRepository.save(document);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(Long documentId, String username) {
        try {
            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get document
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("File not found with id: " + documentId));

            // Security check - only allow access to user's own documents
            if (!document.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to access this document");
            }

            Path filePath = this.fileStorageLocation.resolve(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new DocumentNotFoundException("File not found: " + document.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new DocumentNotFoundException("File not found", ex);
        }
    }

    public List<DocumentDTO> getUserDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Document> documents = documentRepository.findByUser(user);

        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Document getDocument(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + id));

        // Security check - only allow access to user's own documents
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to access this document");
        }

        return document;
    }

    public void deleteDocument(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + id));

        // Security check - only allow deletion of user's own documents
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this document");
        }

        try {
            // Delete the file
            Path filePath = this.fileStorageLocation.resolve(document.getFilePath()).normalize();
            Files.deleteIfExists(filePath);

            // Delete the database record
            documentRepository.delete(document);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file", ex);
        }
    }

    private DocumentDTO convertToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setUploadDate(document.getUploadDate());
        dto.setUsername(document.getUser().getUsername());
        return dto;
    }

    private void validateFileType(MultipartFile file) {
        String fileType = file.getContentType();
        if (fileType == null) {
            throw new FileStorageException("File type cannot be determined");
        }

        // List of allowed file types
        List<String> allowedTypes = List.of(
                "application/pdf",                     // PDF
                "application/msword",                  // DOC
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "application/vnd.ms-excel",            // XLS
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
                "application/vnd.ms-powerpoint",       // PPT
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
                "image/jpeg",                          // JPEG
                "image/png",                           // PNG
                "image/gif",                           // GIF
                "text/plain"                           // TXT
        );

        if (!allowedTypes.contains(fileType)) {
            throw new FileStorageException("File type not supported: " + fileType);
        }
    }
}
