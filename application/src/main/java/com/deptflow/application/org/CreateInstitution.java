package com.deptflow.application.org;

import com.deptflow.application.exceptions.ValidationException;
import com.deptflow.application.ports.InstitutionRepository;
import com.deptflow.domain.Codes;
import com.deptflow.domain.Institution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates an institution (tenant root) with a globally unique code. */
@Service
@Transactional
public class CreateInstitution {

    private final InstitutionRepository institutions;

    public CreateInstitution(InstitutionRepository institutions) {
        this.institutions = institutions;
    }

    public OrgDtos.InstitutionView execute(OrgDtos.CreateInstitutionCommand cmd) {
        if (institutions.findByCode(Codes.normalize(cmd.code())).isPresent()) {
            throw new ValidationException("Institution code already exists");
        }
        Institution institution = Institution.create(cmd.parentInstitutionId(), cmd.name(), cmd.code());
        return OrgDtos.InstitutionView.from(institutions.save(institution));
    }
}
