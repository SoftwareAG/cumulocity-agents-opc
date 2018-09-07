package com.cumulocity.opcua.gateway;

import com.cumulocity.opcua.configuration.BaseIntegrationTest;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.gateway.service.configuration.GatewayConfigurationProperties;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.asBrowsePath;
import static com.cumulocity.opcua.gateway.repository.configuration.ContextProvider.doInvoke;
import static org.assertj.core.api.Assertions.assertThat;

public class GatewayMainTest extends BaseIntegrationTest {

    @Autowired
    private Repository<Gateway> gatewayRepository;

    @Autowired
    private Repository<Device> deviceRepository;

    @Autowired
    private GatewayConfigurationProperties properties;

    @Test
    public void shouldBootstrapNewGateway() {
        //when
        deviceControlService.registerGateway(properties.getIdentifier());
        bootstrapService.refreshGateways();
        eventWatcher.waitFor(GatewayAddedEvent.class);

        //then
        assertThat(gatewayRepository.findAll()).isNotEmpty();
        assertThat(inventoryMockService.findByType("c8y_OPCUA").getManagedObjects()).isNotEmpty();
    }

    @Test
    public void shouldSaveNewDeviceWhenChildDeviceConfigured() {
        //given
        final ManagedObjectRepresentation deviceType = givenDeviceType(asBrowsePath("aaa/bbb"));
        final ManagedObjectRepresentation device = givenDevice(asBrowsePath("ccc/ddd"), deviceType.getId());

        //when
        deviceControlService.registerGateway(properties.getIdentifier());
        bootstrapService.refreshGateways();
        final GatewayAddedEvent event = eventWatcher.waitFor(GatewayAddedEvent.class);

        inventoryMockService.addChildDevice(event.getGatewayId(), device);
        eventWatcher.waitFor(DeviceAddedEvent.class);

        //then
        doInvoke(event.getGateway(), new Runnable() {
            public void run() {
                assertThat(deviceRepository.exists(device.getId())).isTrue();
            }
        });
    }
}
