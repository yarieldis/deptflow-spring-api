package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.DepartmentRepository;
import com.deptflow.application.ports.DocumentRepository;
import com.deptflow.application.ports.InstitutionRepository;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.domain.Department;
import com.deptflow.domain.Document;
import com.deptflow.domain.DocumentStatus;
import com.deptflow.domain.Institution;
import com.deptflow.domain.Person;
import com.deptflow.domain.VersionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(PersistenceTestConfig.class)
class PersistenceTest {

    @Autowired InstitutionRepository institutions;
    @Autowired DepartmentRepository departments;
    @Autowired PersonRepository persons;
    @Autowired DocumentRepository documents;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    void persistsAndReloadsEntities() {
        Institution institution = Institution.create("Acme", "acme");
        institutions.save(institution);

        Department engineering = Department.create(institution.getId(), "Engineering", "eng");
        departments.save(engineering);

        Person alice = Person.create(institution.getId(), "Alice", "Anderson");
        persons.save(alice);

        em.flush();
        em.clear();

        Department loaded = departments.findById(engineering.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Engineering");
        assertThat(loaded.getCode()).isEqualTo("eng");
        assertThat(loaded.getInstitutionId()).isEqualTo(institution.getId());

        Person loadedPerson = persons.findById(alice.getId()).orElseThrow();
        assertThat(loadedPerson.getFirstName()).isEqualTo("Alice");
        assertThat(loadedPerson.getUserId()).isNull();
    }

    @Test
    @Transactional
    void persistsDocumentWithCascadedVersions() {
        Institution institution = Institution.create("Acme", "acme");
        institutions.save(institution);

        Department engineering = Department.create(institution.getId(), "Engineering", "eng");
        departments.save(engineering);

        Person alice = Person.create(institution.getId(), "Alice", "Anderson");
        persons.save(alice);

        Document doc = Document.create(institution.getId(), engineering.getId(), null, "Onboarding", null, alice.getId());
        doc.addVersion("key-1", null, "onboarding.pdf", "application/pdf", 1234, "initial", alice.getId());
        documents.save(doc);

        em.flush();
        em.clear();

        Document loaded = documents.findById(doc.getId()).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo("Onboarding");
        assertThat(loaded.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(loaded.getVersions()).hasSize(1);
        assertThat(loaded.getLatestVersion().getVersionNumber()).isEqualTo(1);
        assertThat(loaded.getLatestVersion().getStatus()).isEqualTo(VersionStatus.DRAFT);
        assertThat(loaded.getLatestVersion().getStorageKey()).isEqualTo("key-1");
    }

    @Test
    @Transactional
    void documentApprovalWorkflowTransitions() {
        Institution institution = Institution.create("Acme", "acme");
        institutions.save(institution);
        Department engineering = Department.create(institution.getId(), "Engineering", "eng");
        departments.save(engineering);
        Person author = Person.create(institution.getId(), "Alice", "Anderson");
        persons.save(author);
        Person approver = Person.create(institution.getId(), "Bob", "Brown");
        persons.save(approver);

        Document doc = Document.create(institution.getId(), engineering.getId(), null, "Policy", null, author.getId());
        doc.addVersion("k1", null, "policy.pdf", "application/pdf", 10, "draft", author.getId());
        documents.save(doc);
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.DRAFT);

        doc.submit(author.getId());
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING_APPROVAL);

        doc.approve(approver.getId());
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PUBLISHED);

        // edit beside live version
        doc.addVersion("k2", null, "policy-v2.pdf", "application/pdf", 11, "revised", author.getId());
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(doc.getLatestVersion().getVersionNumber()).isEqualTo(2);
        assertThat(doc.getVersions()).hasSize(2);
        assertThat(doc.getVersions().get(0).getStatus()).isEqualTo(VersionStatus.APPROVED);

        // reject the new draft
        doc.submit(author.getId());
        doc.reject(approver.getId(), "needs work");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(doc.getLatestVersion().getStatus()).isEqualTo(VersionStatus.REJECTED);

        // archive
        doc.archive(approver.getId());
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
    }

    @Test
    void enforcesUniqueConstraints() {
        institutions.save(Institution.create("Acme", "acme"));

        // different id, same normalized code -> rejected
        assertThatThrownBy(() -> institutions.save(Institution.create("Other", "acme")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void queriesFilterByInstitution() {
        Institution a = Institution.create("Alpha", "alpha");
        Institution b = Institution.create("Beta", "beta");
        institutions.save(a);
        institutions.save(b);

        departments.save(Department.create(a.getId(), "DeptA", "da"));
        departments.save(Department.create(b.getId(), "DeptB", "db"));

        assertThat(departments.findByInstitutionId(a.getId()))
                .extracting(Department::getCode)
                .containsExactly("da");
        assertThat(departments.findByInstitutionId(b.getId()))
                .extracting(Department::getCode)
                .containsExactly("db");
    }
}
