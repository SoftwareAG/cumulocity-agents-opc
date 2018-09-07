package com.cumulocity.opcua.gateway.service.utils;

import com.cumulocity.opcua.gateway.service.configuration.GatewayConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Scheduler {
    private final TaskScheduler taskScheduler;
    private final GatewayConfigurationProperties properties;

    public void scheduleWithFixedDelay(final Runnable task) {
        if (properties.getBootstrapFixedDelay() != null && properties.getBootstrapFixedDelay() > 0) {
            taskScheduler.scheduleWithFixedDelay(task, properties.getBootstrapFixedDelay());
        }
    }

    public void scheduleOnce(Runnable task1) {
        if (properties.getBootstrapFixedDelay() != null && properties.getBootstrapFixedDelay() > 0) {
            taskScheduler.schedule(task1, DateTime.now().plusMillis(properties.getBootstrapFixedDelay()).toDate());
        }
    }
}
