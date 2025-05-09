package com.Management.SecureDocument.Access.repository;



import com.Management.SecureDocument.Access.model.ERole;
import com.Management.SecureDocument.Access.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
