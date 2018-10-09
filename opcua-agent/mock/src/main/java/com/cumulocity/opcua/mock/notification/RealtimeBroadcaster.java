package com.cumulocity.opcua.mock.notification;

import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;

public interface RealtimeBroadcaster {

    void sendDelete(final String deviceId);

    void sendUpdate(final ManagedObjectRepresentation representation);
}
