package com.deptflow.api.auth;

import com.deptflow.api.security.JwtTokenService;
import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.ValidationException;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.application.ports.UserAccountRepository;
import com.deptflow.domain.Person;
import com.deptflow.domain.UserAccount;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Registration and login. Login issues a JWT scoped to the user's institution. */
@Service
public class AuthService {

    private final UserAccountRepository accounts;
    private final PersonRepository persons;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;

    public AuthService(UserAccountRepository accounts, PersonRepository persons,
                       PasswordEncoder passwordEncoder, JwtTokenService tokens) {
        this.accounts = accounts;
        this.persons = persons;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthDtos.UserResponse register(AuthDtos.RegisterRequest req) {
        if (accounts.findByUsername(req.username()).isPresent()) {
            throw new ValidationException("Username already exists");
        }
        if (accounts.findByEmail(req.email()).isPresent()) {
            throw new ValidationException("Email already exists");
        }
        UserAccount account = UserAccount.create(req.username(), req.email(), passwordEncoder.encode(req.password()));
        UserAccount saved = accounts.save(account);
        return new AuthDtos.UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        UserAccount account = accounts.findByUsername(req.username())
                .orElseThrow(() -> new ForbiddenException("Invalid credentials"));
        if (!account.isActive() || !passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }

        List<Person> linked = persons.findByUserId(account.getId());
        UUID institutionId = linked.stream()
                .filter(Person::isActive)
                .findFirst()
                .map(Person::getInstitutionId)
                .orElse(null);

        String token = tokens.issue(account.getId(), account.getUsername(), institutionId);
        return new AuthDtos.TokenResponse(token, institutionId);
    }
}
