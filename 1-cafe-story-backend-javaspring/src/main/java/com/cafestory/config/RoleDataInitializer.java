package com.cafestory.config;

import com.cafestory.entity.Role;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        for (UserRole userRole : UserRole.values()) {
            createRoleIfMissing(userRole.name());
        }
    }

    private void createRoleIfMissing(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            return;
        }
        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
    }
}
