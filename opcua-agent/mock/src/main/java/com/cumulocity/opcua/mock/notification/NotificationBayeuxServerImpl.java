package com.cumulocity.opcua.mock.notification;

import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;

public class NotificationBayeuxServerImpl extends BayeuxServerImpl implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        addExtension(new AcknowledgedMessagesExtension());
        setTransports(new BayeuxNotificationJSONTransport(this));
        initializeDefaultTransports();

        setOptions(buildConfiguration());
        start();
    }

    protected Map<String, Object> buildConfiguration() {
        final Map<String, Object> configuration = new HashMap<>();
        configuration.put(JSON_CONTEXT, new ServerSvensonJSONContext());
        return configuration;
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}
