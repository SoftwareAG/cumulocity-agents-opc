package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.platform.repository.configuration.PlatformProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingService {

    @Autowired
    @SneakyThrows
    public void onStart(PlatformProperties platformProperties, ObjectMapper objectMapper) {
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(platformProperties));
    }
}
