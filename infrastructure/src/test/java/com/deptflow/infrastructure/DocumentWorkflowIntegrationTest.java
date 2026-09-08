package com.deptflow.infrastructure;

import com.deptflow.application.documents.AddDocumentVersion;
import com.deptflow.application.documents.ApproveDocument;
import com.deptflow.application.documents.ArchiveDocument;
import com.deptflow.application.documents.CreateDocument;
import com.deptflow.application.documents.DocumentDtos;
import com.deptflow.application.documents.GetDocument;
import com.deptflow.application.documents.RejectDocument;
import com.deptflow.application.documents.SubmitForApproval;
import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.org.CreateDepartment;
import com.deptflow.application.org.CreateInstitution;
import com.deptflow.application.org.CreateRole;
import com.deptflow.application.org.OrgDtos;
import com.deptflow.application.people.AssignMembership;
import com.deptflow.application.people.CreatePerson;
import com.deptflow.application.people.PeopleDtos;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.application.ports.UserAccountRepository;
import com.deptflow.domain.DocumentStatus;
import com.deptflow.domain.Person;
import com.deptflow.domain.UserAccount;
import com.deptflow.domain.VersionStatus;
import com.deptflow.infrastructure.persistence.ThreadLocalCurrentTenantProvider;
import com.deptflow.infrastructure.security.ThreadLocalCurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(ApplicationTestConfig.class)
class DocumentWorkflowIntegrationTest {

    @Autowired CreateInstitution createInstitution;
    @Autowired CreateDepartment createDepartment;
    @Autowired CreateRole createRole;
    @Autowired CreatePerson createPerson;
    @Autowired AssignMembership assignMembership;
    @Autowired CreateDocument createDocument;
    @Autowired AddDocumentVersion addDocumentVersion;
    @Autowired SubmitForApproval submitForApproval;
    @Autowired ApproveDocument approveDocument;
    @Autowired RejectDocument rejectDocument;
    @Autowired ArchiveDocument archiveDocument;
    @Autowired GetDocument getDocument;
    @Autowired PersonRepository persons;
    @Autowired UserAccountRepository userAccounts;

    private UUID institutionId;
    private UUID departmentId;
    private UUID authorId;
    private UUID approverId;
    private UUID outsiderId;
    private UUID authorUserId;
    private UUID approverUserId;
    private UUID outsiderUserId;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String code = "acme-" + suffix;
        institutionId = createInstitution.execute(new OrgDtos.CreateInstitutionCommand(null, "Acme", code)).id();
        ThreadLocalCurrentTenantProvider.setCurrent(institutionId);

        departmentId = createDepartment.execute(new OrgDtos.CreateDepartmentCommand(null, "Engineering", "eng")).id();
        UUID approverRoleId = createRole.execute(new OrgDtos.CreateRoleCommand("Approver", null, true)).id();
        UUID memberRoleId = createRole.execute(new OrgDtos.CreateRoleCommand("Member", null, false)).id();

        authorId = createPerson.execute(new PeopleDtos.CreatePersonCommand("Alice", "Anderson", null, null, null)).id();
        approverId = createPerson.execute(new PeopleDtos.CreatePersonCommand("Bob", "Brown", null, null, null)).id();
        outsiderId = createPerson.execute(new PeopleDtos.CreatePersonCommand("Mallory", "Mallory", null, null, null)).id();

        authorUserId = linkLogin("alice-" + suffix, "alice-" + suffix + "@example.com", authorId);
        approverUserId = linkLogin("bob-" + suffix, "bob-" + suffix + "@example.com", approverId);
        outsiderUserId = linkLogin("mallory-" + suffix, "mallory-" + suffix + "@example.com", outsiderId);

        assignMembership.execute(new PeopleDtos.AssignMembershipCommand(authorId, departmentId, memberRoleId, true, LocalDate.now().minusDays(1)));
        assignMembership.execute(new PeopleDtos.AssignMembershipCommand(approverId, departmentId, approverRoleId, true, LocalDate.now().minusDays(1)));
    }

    private UUID linkLogin(String username, String email, UUID personId) {
        UserAccount account = userAccounts.save(UserAccount.create(username, email, "hash"));
        Person person = persons.findById(personId).orElseThrow();
        person.linkLogin(account.getId());
        persons.save(person);
        return account.getId();
    }

    private DocumentDtos.CreateDocumentCommand command(String title, String fileName, String content, String note) {
        return new DocumentDtos.CreateDocumentCommand(departmentId, null, title, null, fileName, "text/plain",
                content.length(), new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), note);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalCurrentTenantProvider.clear();
        ThreadLocalCurrentUserProvider.clear();
    }

    @Test
    void fullApprovalWorkflow() {
        ThreadLocalCurrentUserProvider.setCurrent(authorUserId);
        UUID docId = createDocument.execute(command("Onboarding", "onboarding.txt", "hello", "initial")).id();

        submitForApproval.execute(new DocumentDtos.SubmitCommand(docId));
        assertThat(getDocument.execute(docId).status()).isEqualTo(DocumentStatus.PENDING_APPROVAL);

        ThreadLocalCurrentUserProvider.setCurrent(approverUserId);
        approveDocument.execute(new DocumentDtos.ApproveCommand(docId));
        assertThat(getDocument.execute(docId).status()).isEqualTo(DocumentStatus.PUBLISHED);

        // edit beside live version
        ThreadLocalCurrentUserProvider.setCurrent(authorUserId);
        addDocumentVersion.execute(new DocumentDtos.AddDocumentVersionCommand(docId, "onboarding-v2.txt", "text/plain",
                "hello2".length(), new ByteArrayInputStream("hello2".getBytes(StandardCharsets.UTF_8)), "revised"));
        DocumentDtos.DocumentView afterEdit = getDocument.execute(docId);
        assertThat(afterEdit.status()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(afterEdit.versions()).hasSize(2);
        assertThat(afterEdit.versions().get(0).status()).isEqualTo(VersionStatus.APPROVED);

        // submit, reject, resubmit, approve, archive
        submitForApproval.execute(new DocumentDtos.SubmitCommand(docId));
        ThreadLocalCurrentUserProvider.setCurrent(approverUserId);
        rejectDocument.execute(new DocumentDtos.RejectCommand(docId, "needs work"));
        assertThat(getDocument.execute(docId).status()).isEqualTo(DocumentStatus.DRAFT);

        ThreadLocalCurrentUserProvider.setCurrent(authorUserId);
        submitForApproval.execute(new DocumentDtos.SubmitCommand(docId));
        ThreadLocalCurrentUserProvider.setCurrent(approverUserId);
        approveDocument.execute(new DocumentDtos.ApproveCommand(docId));
        archiveDocument.execute(new DocumentDtos.ArchiveCommand(docId));
        assertThat(getDocument.execute(docId).status()).isEqualTo(DocumentStatus.ARCHIVED);
    }

    @Test
    void nonMemberCannotCreateDocument() {
        ThreadLocalCurrentUserProvider.setCurrent(outsiderUserId);
        assertThatThrownBy(() -> createDocument.execute(command("Intrusion", "x.txt", "x", "x")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void nonApproverCannotApprove() {
        ThreadLocalCurrentUserProvider.setCurrent(authorUserId);
        UUID docId = createDocument.execute(command("Doc", "d.txt", "d", "d")).id();
        submitForApproval.execute(new DocumentDtos.SubmitCommand(docId));

        assertThatThrownBy(() -> approveDocument.execute(new DocumentDtos.ApproveCommand(docId)))
                .isInstanceOf(ForbiddenException.class);
    }
}
