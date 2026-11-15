package com.employeemanagement.service;

import com.employeemanagement.model.AuditAction;
import com.employeemanagement.model.AuditLogEntry;
import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.AuditLogRepository;

import jakarta.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditService.class);

    public static final int MAX_PAGE_SIZE = 500;

    private final AuditLogRepository repository;
    private final TransactionTemplate auditTransaction;

    public AuditService(
            AuditLogRepository repository,
            PlatformTransactionManager transactionManager) {

        this.repository = repository;

        this.auditTransaction = new TransactionTemplate(transactionManager);
        this.auditTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(AuditAction action, String target, String detail) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = null;
        String role = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            username = authentication.getName();
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        }

        recordAs(username, role, action, target, detail);
    }

    public void recordAs(
            String username,
            String role,
            AuditAction action,
            String target,
            String detail) {

        try {
            AuditLogEntry entry = new AuditLogEntry();

            entry.setActorUsername(truncate(username, 100));
            entry.setActorRole(truncate(role, 30));
            entry.setAction(action.name());
            entry.setTarget(truncate(target, 255));
            entry.setDetail(truncate(detail, 1000));

            auditTransaction.executeWithoutResult(
                    status -> repository.save(entry));

        } catch (RuntimeException e) {
            log.error("Unable to write audit log entry for action {}", action, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogEntry> search(
            String action,
            String actor,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Specification<AuditLogEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(
                        root.get("action"),
                        action.trim().toUpperCase()));
            }

            if (actor != null && !actor.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("actorUsername")),
                        "%" + escapeLike(actor.trim().toLowerCase()) + "%",
                        '\\'));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from.atStartOfDay()));
            }

            if (to != null) {
                predicates.add(cb.lessThan(
                        root.get("createdAt"),
                        to.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        return repository.findAll(spec, pageRequest);
    }

    public static String describeEmployee(Employee employee) {
        if (employee == null) {
            return null;
        }

        return "Employee #" + employee.getId() + " ("
                + employee.getFirstName() + " "
                + employee.getLastName() + ")";
    }

    static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }

        return value.length() <= max ? value : value.substring(0, max);
    }
}
