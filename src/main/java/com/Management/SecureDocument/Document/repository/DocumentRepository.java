package com.Management.SecureDocument.Document.repository;


//import com.Management.SecureDocument.model.Document;
//import com.Management.SecureDocument.auth.User;
import com.Management.SecureDocument.Access.model.User;
import com.Management.SecureDocument.Document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser(User user);
    List<Document> findByUserId(Long userId);
}
