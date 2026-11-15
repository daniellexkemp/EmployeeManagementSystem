package com.employeemanagement.audit;

import com.employeemanagement.model.AuditAction;
import com.employeemanagement.model.AuditLogEntry;
import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.AuditLogRepository;
import com.employeemanagement.service.AuditService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceTest {

    private AuditLogRepository repository;
    private PlatformTransactionManager transactionManager;
    private AuditService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuditLogRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        service = new AuditService(repository, transactionManager);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private AuditLogEntry captureSaved() {
        ArgumentCaptor<AuditLogEntry> captor =
                ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void recordUsesSignedInUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "hradmin", null,
                        List.of(new SimpleGrantedAuthority("HR_ADMIN"))));

        service.record(AuditAction.CHANGE_ROLE, "User jdoe", "EMPLOYEE -> MANAGER");

        AuditLogEntry saved = captureSaved();
        assertEquals("hradmin", saved.getActorUsername());
        assertEquals("HR_ADMIN", saved.getActorRole());
        assertEquals("CHANGE_ROLE", saved.getAction());
        assertEquals("User jdoe", saved.getTarget());
        assertEquals("EMPLOYEE -> MANAGER", saved.getDetail());
    }

    @Test
    void recordWithoutSignedInUserLeavesActorEmpty() {
        service.record(AuditAction.LOGOUT, null, null);

        AuditLogEntry saved = captureSaved();
        assertNull(saved.getActorUsername());
        assertNull(saved.getActorRole());
        assertEquals("LOGOUT", saved.getAction());
    }

    @Test
    void recordAsUsesExplicitUser() {
        service.recordAs("jdoe", null, AuditAction.LOGIN_FAILED, null, "Invalid username or password");

        AuditLogEntry saved = captureSaved();
        assertEquals("jdoe", saved.getActorUsername());
        assertNull(saved.getActorRole());
        assertEquals("LOGIN_FAILED", saved.getAction());
    }

    @Test
    void longValuesAreTruncatedToColumnSize() {
        service.recordAs("u".repeat(500), "R".repeat(100),
                AuditAction.LOGIN_FAILED, "t".repeat(400), "d".repeat(5000));

        AuditLogEntry saved = captureSaved();
        assertEquals(100, saved.getActorUsername().length());
        assertEquals(30, saved.getActorRole().length());
        assertEquals(255, saved.getTarget().length());
        assertEquals(1000, saved.getDetail().length());
    }

    @Test
    void repositoryFailureDoesNotBreakTheRequest() {
        when(repository.save(any())).thenThrow(new RuntimeException("database down"));

        assertDoesNotThrow(() ->
                service.recordAs("jdoe", "EMPLOYEE", AuditAction.LOGIN, null, null));
    }

    @Test
    void failedCommitDoesNotBreakTheRequest() {
        doThrow(new UnexpectedRollbackException("rolled back"))
                .when(transactionManager).commit(any());

        assertDoesNotThrow(() ->
                service.recordAs("jdoe", null, AuditAction.LOGIN_FAILED, null, null));
    }

    @Test
    void auditWriteUsesItsOwnTransaction() {
        service.recordAs("jdoe", "EMPLOYEE", AuditAction.LOGIN, null, null);

        ArgumentCaptor<TransactionDefinition> captor =
                ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(captor.capture());

        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                captor.getValue().getPropagationBehavior());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchSortsNewestFirstAndCapsPageSize() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.search(null, null, null, null, -3, 10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(AuditService.MAX_PAGE_SIZE, pageable.getPageSize());

        Sort.Order createdAt = pageable.getSort().getOrderFor("createdAt");
        assertEquals(Sort.Direction.DESC, createdAt.getDirection());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchReturnsRepositoryPage() {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setAction("LOGIN");

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry)));

        Page<AuditLogEntry> result = service.search("login", "jdoe", null, null, 0, 50);

        assertEquals(1, result.getTotalElements());
        assertEquals("LOGIN", result.getContent().get(0).getAction());
    }

    @Test
    void describeEmployeeFormatsTarget() {
        Employee employee = new Employee();
        employee.setId(12L);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");

        assertEquals("Employee #12 (Jane Doe)", AuditService.describeEmployee(employee));
        assertNull(AuditService.describeEmployee(null));
    }
}
