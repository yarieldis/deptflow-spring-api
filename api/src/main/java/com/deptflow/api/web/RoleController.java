package com.deptflow.api.web;

import com.deptflow.application.org.CreateRole;
import com.deptflow.application.org.OrgDtos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final CreateRole createRole;

    public RoleController(CreateRole createRole) {
        this.createRole = createRole;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrgDtos.RoleView create(@RequestBody OrgDtos.CreateRoleCommand command) {
        return createRole.execute(command);
    }
}
