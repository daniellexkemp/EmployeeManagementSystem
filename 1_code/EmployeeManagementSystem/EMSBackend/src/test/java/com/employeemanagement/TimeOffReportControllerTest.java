
package com.employeemanagement;

import com.employeemanagement.config.SecurityConfig;
import com.employeemanagement.controller.TimeOffController;
import com.employeemanagement.model.Users;
import com.employeemanagement.repository.UserRepository;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.TimeOffService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeOffController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class TimeOffReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeOffService timeOffService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockUser("hradmin", "HR_ADMIN");
        mockUser("employee", "EMPLOYEE");
        mockUser("manager", "MANAGER");
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
    void hrAdminCanAccessReport() throws Exception {

        when(timeOffService.getAllRequestsForReport())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/timeoff/report")
                .with(user("hradmin").authorities(
                        new SimpleGrantedAuthority("HR_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void employeeCannotAccessReport() throws Exception {

        mockMvc.perform(get("/api/timeoff/report")
                .with(user("employee").authorities(
                        new SimpleGrantedAuthority("EMPLOYEE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotAccessReport() throws Exception {

        mockMvc.perform(get("/api/timeoff/report")
                .with(user("manager").authorities(
                        new SimpleGrantedAuthority("MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessReport() throws Exception {

        mockMvc.perform(get("/api/timeoff/report"))
                .andExpect(status().isUnauthorized());
    }
}
