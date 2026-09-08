package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.UserAccountRepository;
import com.deptflow.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUserAccountRepository extends UserAccountRepository, JpaRepository<UserAccount, UUID> {
}
