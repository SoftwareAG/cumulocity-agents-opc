package com.cumulocity.opcua.platform.repository.configuration;

import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.repository.configuration.ContextProvider;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.alarm.AlarmApi;
import com.cumulocity.sdk.client.devicecontrol.DeviceControlApi;
import com.cumulocity.sdk.client.event.EventApi;
import com.cumulocity.sdk.client.identity.IdentityApi;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sdk.client.measurement.MeasurementApi;
import com.google.common.base.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Configuration
public class PlatformConfiguration {
    @Bean
    public Platform bootstrapPlatform(PlatformProvider platformProvider) {
        return platformProvider.getBootstrapPlatform();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public InventoryApi inventory(PlatformProvider platformProvider) {
        return platform(platformProvider).getInventoryApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public IdentityApi identity(PlatformProvider platformProvider) {
        return platform(platformProvider).getIdentityApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public MeasurementApi measurementApi(PlatformProvider platformProvider) {
        return platform(platformProvider).getMeasurementApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public AlarmApi alarmApi(PlatformProvider platformProvider) {
        return platform(platformProvider).getAlarmApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public EventApi eventApi(PlatformProvider platformProvider) {
        return platform(platformProvider).getEventApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public DeviceControlApi deviceControlApi(PlatformProvider platformProvider) {
        return platform(platformProvider).getDeviceControlApi();
    }

    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
    public Platform platform(PlatformProvider platformProvider) {
        final Optional<Credentials> deviceCredentials = ContextProvider.get(Credentials.class);
        if (!deviceCredentials.isPresent()) {
            throw new IllegalStateException("Should be run in context.");
        }
        return platformProvider.getPlatform(deviceCredentials.get());
    }
}
