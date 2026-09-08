package com.deptflow.api.web;

import com.deptflow.application.people.CreatePerson;
import com.deptflow.application.people.PeopleDtos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final CreatePerson createPerson;

    public PersonController(CreatePerson createPerson) {
        this.createPerson = createPerson;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeopleDtos.PersonView create(@RequestBody PeopleDtos.CreatePersonCommand command) {
        return createPerson.execute(command);
    }
}
