package com.cumulocity.opcua.gateway;

import com.cumulocity.microservice.logging.annotation.EnableMicroserviceLogging;
import com.cumulocity.opcua.gateway.repository.core.PersistableType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.context.annotation.AdviceMode.ASPECTJ;

@Slf4j
@ComponentScan(value = "com.cumulocity.opcua", includeFilters = {@ComponentScan.Filter(classes = {PersistableType.class})})
@SpringBootApplication
@EnableScheduling
@EnableAsync(mode = ASPECTJ)
@EnableMicroserviceLogging
@EnableConfigurationProperties
@PropertySources(value = {
        @PropertySource(value = "file:${user.home}/.opcua/opcua-agent-gateway.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${opcua.conf.dir:/etc}/opcua/opcua-agent-gateway.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "classpath:META-INF/spring/opcua-agent-gateway.properties", ignoreResourceNotFound = true)
})
public class GatewayMain {

    public static void main(String... args) {
        SpringApplication.run(GatewayMain.class, args);
    }
}
