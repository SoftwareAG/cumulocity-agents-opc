package com.cumulocity.opcua.gateway.service.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class GatewayConfigurationProperties {
    @Value("${gateway.identifier:opcua}")
    private String identifier;
    @Value("${gateway.bootstrapFixedDelay:10000}")
    private Integer bootstrapFixedDelay;
}
