package com.deptflow.api.config;

import com.deptflow.application.ports.StorageService;
import com.deptflow.infrastructure.storage.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires infrastructure adapters that need configuration values. */
@Configuration
public class ApiBeansConfig {

    @Bean
    StorageService storageService(@Value("${deptflow.storage.root-path:./storage}") String rootPath) {
        return new FileSystemStorageService(rootPath);
    }
}
