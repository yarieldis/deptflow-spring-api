package com.deptflow.application.people;

import com.deptflow.application.common.CallerResolver;
import com.deptflow.application.ports.PersonRepository;
import com.deptflow.domain.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Creates a person record, tenant-stamped with the current institution. */
@Service
@Transactional
public class CreatePerson {

    private final CallerResolver caller;
    private final PersonRepository persons;

    public CreatePerson(CallerResolver caller, PersonRepository persons) {
        this.caller = caller;
        this.persons = persons;
    }

    public PeopleDtos.PersonView execute(PeopleDtos.CreatePersonCommand cmd) {
        UUID tenantId = caller.tenantId();
        Person person = Person.create(tenantId, cmd.firstName(), cmd.lastName());
        person.updateContactInfo(cmd.email(), cmd.phone(), cmd.jobTitle());
        return PeopleDtos.PersonView.from(persons.save(person));
    }
}
