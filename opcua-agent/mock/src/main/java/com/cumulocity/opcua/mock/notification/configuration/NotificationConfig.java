package com.cumulocity.opcua.mock.notification.configuration;

import com.cumulocity.opcua.mock.notification.BayeuxRealtimeBroadcaster;
import com.cumulocity.opcua.mock.notification.NotificationBayeuxServerImpl;
import com.cumulocity.opcua.mock.notification.RealtimeBroadcaster;
import com.cumulocity.opcua.mock.notification.controller.CepRealtimController;
import com.cumulocity.opcua.mock.notification.controller.DeviceControllNotificationController;
import org.cometd.server.BayeuxServerImpl;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class NotificationConfig {

    @Bean
    public BayeuxServerImpl realtimeServer() {
        return new NotificationBayeuxServerImpl();
    }

    @Bean
    public RealtimeBroadcaster realtimeBroadcaster(final BayeuxServerImpl realtimeServer) {
        return new BayeuxRealtimeBroadcaster(realtimeServer);
    }

    @Bean
    public ServletRegistrationBean cepRealtime(final BayeuxServerImpl realtimeServer) {
        return new ServletRegistrationBean(new CepRealtimController(realtimeServer), "/cep/realtime");
    }

    @Bean
    public ServletRegistrationBean devicecontrolNotifications(final BayeuxServerImpl realtimeServer) {
        return new ServletRegistrationBean(new DeviceControllNotificationController(realtimeServer), "/devicecontrol/notifications");
    }
}
