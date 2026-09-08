package com.deptflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root. Scans the whole {@code com.deptflow} tree to pick up the
 * application use cases, infrastructure persistence, and API controllers.
 */
@SpringBootApplication(scanBasePackages = "com.deptflow")
public class DeptFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeptFlowApplication.class, args);
    }
}
