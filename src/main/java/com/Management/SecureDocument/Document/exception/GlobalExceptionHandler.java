//package com.Management.SecureDocument.Document.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.multipart.MaxUploadSizeExceededException;
//
//import java.util.Map;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(FileStorageException.class)
//    public ResponseEntity<Map<String, String>> handleFileStorageException(FileStorageException ex) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(Map.of("error", ex.getMessage()));
//    }
//
//    @ExceptionHandler(DocumentNotFoundException.class)
//    public ResponseEntity<Map<String, String>> handleDocumentNotFoundException(DocumentNotFoundException ex) {
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(Map.of("error", ex.getMessage()));
//    }
//
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ResponseEntity<Map<String, String>> handleMaxSizeException() {
//        return ResponseEntity
//                .status(HttpStatus.PAYLOAD_TOO_LARGE)
//                .body(Map.of("error", "File size exceeds maximum limit"));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", ex.getMessage()));
//    }
//}
