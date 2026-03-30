package com.smartsure.auth.config;

import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.RoleName;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.repository.RoleRepository;
import com.smartsure.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, RoleName.ROLE_CUSTOMER));
            roleRepository.save(new Role(null, RoleName.ROLE_ADMIN));
            log.info("Default roles initialized");
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));

            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@smartsure.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin@smartsure.com / admin123");
        }
    }
}
