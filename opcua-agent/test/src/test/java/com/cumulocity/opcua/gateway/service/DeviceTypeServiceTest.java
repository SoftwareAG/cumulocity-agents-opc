package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.configuration.BaseIntegrationTest;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceTypeAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.repository.core.GatewayRepository;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DeviceTypeServiceTest extends BaseIntegrationTest {

    @Autowired
    private GatewayRepository<DeviceType> deviceTypeRepository;

    @Test
    public void shouldAddDeviceType() {
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

//        then
        assertThat(deviceTypeRepository.findAll(event.getGateway())).hasSize(1);
        assertThat(deviceTypeRepository.get(event.getGateway(), deviceType.getId()).get()).isEqualTo(DeviceType.builder()
                .id(deviceType.getId())
                .fieldbusType("opcua")
                .register(new Register().withBrowsePath(valueBrowsePath))
                .build());
    }
}
