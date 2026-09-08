package com.deptflow.api.web;

import com.deptflow.application.people.AssignMembership;
import com.deptflow.application.people.PeopleDtos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final AssignMembership assignMembership;

    public MembershipController(AssignMembership assignMembership) {
        this.assignMembership = assignMembership;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeopleDtos.MembershipView create(@RequestBody PeopleDtos.AssignMembershipCommand command) {
        return assignMembership.execute(command);
    }
}
