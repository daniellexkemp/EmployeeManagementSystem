
package com.employeemanagement.repository;

import com.employeemanagement.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    // Check whether a username already exists.
    boolean existsByUsername(String username);

    // Check whether an employee already has a user account.
    boolean existsByEmployee_Id(Long employeeId);
}
