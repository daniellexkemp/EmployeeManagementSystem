
package com.employeemanagement.config;

import com.employeemanagement.model.Users;
import com.employeemanagement.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CurrentRoleFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public CurrentRoleFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)
            && authentication.getPrincipal() instanceof String username) {

            Optional<Users> userOptional =
                    userRepository.findByUsername(username);

            if (userOptional.isEmpty()
                    || !userOptional.get().isEnabled()) {

                SecurityContextHolder.clearContext();

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "User account is no longer active"
                );
                return;
            }

            Users user = userOptional.get();

            Authentication updatedAuthentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            user.getRole()
                                    )
                            )
                    );

            SecurityContext updatedContext =
                    SecurityContextHolder.createEmptyContext();

            updatedContext.setAuthentication(
                    updatedAuthentication
            );

            SecurityContextHolder.setContext(
                    updatedContext
            );
        }

        filterChain.doFilter(request, response);
    }
}
