package com.cafestory.config;

import com.cafestory.entity.Role;
import com.cafestory.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleDataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleDataInitializer roleDataInitializer;

    @Test
    void run_success_createsMissingDefaultRoles_TC001() throws Exception {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.empty());
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        roleDataInitializer.run();

        verify(roleRepository, times(4)).save(roleCaptor.capture());
        assertThat(roleCaptor.getAllValues())
                .extracting(Role::getName)
                .containsExactly("USER", "REVIEWER", "ADMIN", "CAFE_PAGE");
    }

    @Test
    void run_success_skipsExistingDefaultRoles_TC002() throws Exception {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role("USER")));
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role("REVIEWER")));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role("ADMIN")));
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.of(role("CAFE_PAGE")));

        roleDataInitializer.run();

        verify(roleRepository, never()).save(any(Role.class));
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
