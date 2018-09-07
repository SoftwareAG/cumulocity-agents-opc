package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.configuration.BaseIntegrationTest;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceTypeAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import org.junit.Test;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientValidationServiceTest extends BaseIntegrationTest {

    @Test
    public void shouldGenerateUrlValidationAlarm() {
//        given
        final BrowsePath deviceBrowsePath = asBrowsePath("Boilers/Boiler #1");
        final BrowsePath valueBrowsePath = asBrowsePath("PipeX001/FTX001/Output");

        final ManagedObjectRepresentation deviceType = givenDeviceType(valueBrowsePath);
        final ManagedObjectRepresentation device = givenDevice(deviceBrowsePath, deviceType.getId());
        server.createObjectNode(deviceBrowsePath.concat(valueBrowsePath));

//        when
        deviceControlService.registerGateway(properties.getIdentifier());
        bootstrapService.refreshGateways();

        final GatewayAddedEvent event = eventWatcher.waitFor(GatewayAddedEvent.class);
        inventoryMockService.addChildDevice(event.getGatewayId(), device);
        eventWatcher.waitFor(DeviceAddedEvent.class);
        eventWatcher.waitFor(DeviceTypeAddedEvent.class);
        final GatewayConfigErrorEvent errorEvent = eventWatcher.waitFor(GatewayConfigErrorEvent.class);

        assertThat(errorEvent).isNotNull();
        assertThat(errorEvent.getMessage()).isEqualTo("Connection url not set");
    }
}
