package com.employeemanagement.audit;

import com.employeemanagement.config.SecurityConfig;
import com.employeemanagement.controller.AuditController;
import com.employeemanagement.model.AuditLogEntry;
import com.employeemanagement.model.Users;
import com.employeemanagement.repository.UserRepository;
import com.employeemanagement.service.AuditService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class AuditControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockUser("hradmin", "HR_ADMIN");
        mockUser("sysadmin", "SYS_ADMIN");
        mockUser("manager", "MANAGER");
        mockUser("employee", "EMPLOYEE");
    }

    private void mockUser(String username, String role) {
        Users user = new Users();
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));
    }

    @Test
    void hrAdminCanViewAuditLog() throws Exception {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setId(1L);
        entry.setActorUsername("hradmin");
        entry.setActorRole("HR_ADMIN");
        entry.setAction("CHANGE_ROLE");
        entry.setTarget("User jdoe");
        entry.setDetail("EMPLOYEE -> MANAGER");

        when(auditService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/api/audit-log")
                        .with(user("hradmin").authorities(
                                new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].action").value("CHANGE_ROLE"))
                .andExpect(jsonPath("$.items[0].detail").value("EMPLOYEE -> MANAGER"));
    }

    @Test
    void filtersArePassedToService() throws Exception {
        when(auditService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 25), 0));

        mockMvc.perform(get("/api/audit-log")
                        .param("action", "LOGIN")
                        .param("actor", "jdoe")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-14")
                        .param("page", "2")
                        .param("size", "25")
                        .with(user("hradmin").authorities(
                                new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isOk());

        verify(auditService).search(
                eq("LOGIN"),
                eq("jdoe"),
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 14)),
                eq(2),
                eq(25));
    }

    @Test
    void reversedDateRangeIsRejected() throws Exception {
        mockMvc.perform(get("/api/audit-log")
                        .param("from", "2026-11-14")
                        .param("to", "2026-11-01")
                        .with(user("hradmin").authorities(
                                new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(auditService);
    }

    @Test
    void hrAdminCanListActions() throws Exception {
        mockMvc.perform(get("/api/audit-log/actions")
                        .with(user("hradmin").authorities(
                                new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("LOGIN"));
    }

    @Test
    void systemAdminCannotViewAuditLog() throws Exception {
        mockMvc.perform(get("/api/audit-log")
                        .with(user("sysadmin").authorities(
                                new SimpleGrantedAuthority("SYS_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotViewAuditLog() throws Exception {
        mockMvc.perform(get("/api/audit-log")
                        .with(user("manager").authorities(
                                new SimpleGrantedAuthority("MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotViewAuditLog() throws Exception {
        mockMvc.perform(get("/api/audit-log")
                        .with(user("employee").authorities(
                                new SimpleGrantedAuthority("EMPLOYEE"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/audit-log/actions")
                        .with(user("employee").authorities(
                                new SimpleGrantedAuthority("EMPLOYEE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotViewAuditLog() throws Exception {
        mockMvc.perform(get("/api/audit-log"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleIsReadFromDatabaseNotSession() throws Exception {
        mockUser("demoted", "EMPLOYEE");

        mockMvc.perform(get("/api/audit-log")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        "demoted", null,
                                        List.of(new SimpleGrantedAuthority("HR_ADMIN"))))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditService);
    }
}
