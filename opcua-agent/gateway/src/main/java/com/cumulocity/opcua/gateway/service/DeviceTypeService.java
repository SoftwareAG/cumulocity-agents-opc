package com.cumulocity.opcua.gateway.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceTypeAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceTypeUpdatedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.gateway.repository.DeviceTypeInventoryRepository;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.platform.notification.Notifications;
import com.cumulocity.opcua.platform.notification.model.ManagedObjectListener;
import com.cumulocity.opcua.platform.notification.model.Subscriptions;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProvider;
import com.cumulocity.sdk.client.PlatformParameters;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeviceTypeService {

    private final Repository<DeviceType> deviceTypePeristedRepository;
    private final DeviceTypeInventoryRepository deviceTypeInventoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformProvider platformProvider;
    private final Notifications notifications;
    private final Subscriptions subscribers = new Subscriptions();

    @EventListener
    @RunWithinContext
    public void addDeviceType(final DeviceAddedEvent event) {
        final Gateway gateway = event.getGateway();
        final Device device = event.getDevice();
        final Optional<DeviceType> newDeviceTypeOptional = deviceTypeInventoryRepository.get(gateway, device.getDeviceType());
        if (newDeviceTypeOptional.isPresent()) {
            final Optional<DeviceType> previousDeviceTypeOptional = deviceTypePeristedRepository.get(newDeviceTypeOptional.get().getId());
            if (!previousDeviceTypeOptional.equals(newDeviceTypeOptional)) {
                final DeviceType deviceType = deviceTypePeristedRepository.save(newDeviceTypeOptional.get());
                eventPublisher.publishEvent(new DeviceTypeAddedEvent(event.getGateway(), device, deviceType));
                unsubscribe(deviceType);
                subscribe(gateway, device, deviceType);
            }
        }
    }

    private void subscribe(final Gateway gateway, final Device device, final DeviceType deviceType) {
        final PlatformParameters platform = platformProvider.getPlatformProperties(gateway);
        final GId id = deviceType.getId();

        subscribers.add(id, notifications.subscribeInventory(platform, id, new ManagedObjectListener() {
            public void onUpdate(Object value) {
                eventPublisher.publishEvent(new DeviceTypeUpdatedEvent(gateway, device, deviceType));
            }

            @Override
            public void onDelete() {
                subscribers.disconnect(id);
            }
        }));
    }

    private void unsubscribe(DeviceType deviceType) {
        subscribers.disconnect(deviceType.getId());
    }
}
