package com.cumulocity.opcua.mock.notification.controller;

import org.cometd.server.BayeuxServerImpl;

public class CepRealtimController extends NotificationController {
    public CepRealtimController(BayeuxServerImpl server) {
        super(server);
    }
}
