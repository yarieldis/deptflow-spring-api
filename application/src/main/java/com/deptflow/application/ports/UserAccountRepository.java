package com.deptflow.application.ports;

import com.deptflow.domain.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends Repository<UserAccount, UUID> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);
}
