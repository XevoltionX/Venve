package com.cs.auth.repository;

import com.cs.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Page<User> findByUserNameContaining(String userName, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByUserNameContainingAndRole(String userName, String role, Pageable pageable);
}
