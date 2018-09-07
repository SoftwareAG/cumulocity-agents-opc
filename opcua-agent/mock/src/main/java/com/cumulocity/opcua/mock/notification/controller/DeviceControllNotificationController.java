package com.cumulocity.opcua.mock.notification.controller;

import org.cometd.server.BayeuxServerImpl;

public class DeviceControllNotificationController extends NotificationController {
    public DeviceControllNotificationController(BayeuxServerImpl server) {
        super(server);
    }
}
