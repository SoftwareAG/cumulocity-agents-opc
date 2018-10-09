package com.cumulocity.opcua.gateway.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.factory.DeviceFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayUpdateEvent;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.platform.repository.ManagedObjectRepository;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cumulocity.model.idtype.GId.asGId;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.collections.ListUtils.subtract;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeviceService {

    private final Repository<Device> deviceRepository;
    private final DeviceFactory deviceFactory;
    private final ManagedObjectRepository managedObjectRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @RunWithinContext
    public void updateDevice(GatewayUpdateEvent event) {
        updateDevice(event.getGateway());
    }

    @EventListener
    @RunWithinContext
    public void updateDevice(final GatewayAddedEvent event) {
        try {
            updateDevice(event.getGateway());
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @EventListener
    @RunWithinContext
    public void removeDevice(final GatewayRemovedEvent event) {
        for (final GId child : event.getGateway().getCurrentDeviceIds()) {
            removeDevice(event.getGateway(), child);
        }
    }

    private void updateDevice(Gateway gateway) {
        final List<String> oldDeviceIds = from(deviceRepository.findAll()).transform(Device.Method.getId()).toList();
        final List<String> newDeviceIds = from(gateway.getCurrentDeviceIds()).transform(Device.Method.gidGetValue()).toList();
        final List<String> addedIds = subtract(newDeviceIds, oldDeviceIds);
        final List<String> removedIds = subtract(oldDeviceIds, newDeviceIds);
        for (final String addedDeviceId : addedIds) {
            final Optional<ManagedObjectRepresentation> optional = managedObjectRepository.get(gateway, asGId(addedDeviceId));
            if (optional.isPresent()) {
                addDevice(gateway, optional.get());
            }
        }
        for (final String removedDeviceId : removedIds) {
            removeDevice(gateway, asGId(removedDeviceId));
        }
    }

    private void addDevice(Gateway gateway, ManagedObjectRepresentation deviceManagedObject) {
        final Optional<Device> device = deviceFactory.create(deviceManagedObject);
        if (device.isPresent()) {
            if (!deviceRepository.exists(device.get().getId())) {
                deviceRepository.save(device.get());
                eventPublisher.publishEvent(new DeviceAddedEvent(gateway, device.get()));
            }
        }
    }

    private void removeDevice(Gateway gateway, GId deviceId) {
        if (deviceRepository.exists(deviceId)) {
            final Device deleted = deviceRepository.delete(deviceId);
            eventPublisher.publishEvent(new DeviceRemovedEvent(gateway, deleted));
        }
    }
}
