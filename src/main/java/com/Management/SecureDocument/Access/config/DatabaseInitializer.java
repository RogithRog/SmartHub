package com.Management.SecureDocument.Access.config;





import com.Management.SecureDocument.Access.model.ERole;
import com.Management.SecureDocument.Access.model.Role;
import com.Management.SecureDocument.Access.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if roles exist, if not create them
        if (roleRepository.count() == 0) {
            // Create default roles
            Role userRole = new Role(ERole.ROLE_USER);
            Role adminRole = new Role(ERole.ROLE_ADMIN);

            roleRepository.saveAll(Arrays.asList(userRole, adminRole));

            System.out.println("Default roles created successfully");
        }
    }
}
