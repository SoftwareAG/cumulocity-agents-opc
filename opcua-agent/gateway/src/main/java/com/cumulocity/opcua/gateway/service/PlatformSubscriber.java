package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.factory.GatewayFactory;
import com.cumulocity.opcua.gateway.factory.OperationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.device.DeviceRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayUpdateEvent;
import com.cumulocity.opcua.gateway.model.operation.Operation;
import com.cumulocity.opcua.gateway.model.operation.OperationEvent;
import com.cumulocity.opcua.gateway.model.platform.PlatformSubscribedEvent;
import com.cumulocity.opcua.gateway.model.platform.PlatformUnsubscribedEvent;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.platform.notification.Notifications;
import com.cumulocity.opcua.platform.notification.model.ManagedObjectListener;
import com.cumulocity.opcua.platform.notification.model.OperationListener;
import com.cumulocity.opcua.platform.notification.model.Subscriptions;
import com.cumulocity.opcua.platform.repository.ManagedObjectRepository;
import com.cumulocity.opcua.platform.repository.OperationRepository;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProvider;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.operation.OperationRepresentation;
import com.cumulocity.sdk.client.PlatformParameters;
import com.cumulocity.sdk.client.cep.notification.ManagedObjectDeleteAwareNotification;
import com.cumulocity.sdk.client.devicecontrol.notification.OperationNotificationSubscriber;
import com.cumulocity.sdk.client.notification.Subscriber;
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
public class PlatformSubscriber {
    private final PlatformProvider platformProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final Repository<Gateway> gatewayRepository;
    private final GatewayFactory gatewayFactory;
    private final OperationFactory operationFactory;
    private final ManagedObjectRepository managedObjectRepository;
    private final OperationRepository operationRepository;
    private final Notifications notifications;

    private final Subscriptions managedObjectSubscribers = new Subscriptions();
    private final Subscriptions operationSubscribers = new Subscriptions();

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscriptions(final GatewayAddedEvent event) {
        try {
            final Gateway gateway = event.getGateway();
            final PlatformParameters platform = platformProvider.getPlatformProperties(gateway);

            unsubscribeGateway(gateway);
            subscribeGatewayInventory(platform, gateway);
            subscribeGatewayOperations(platform, gateway);
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void subscribe(final DeviceAddedEvent deviceAddedEvent) {
        try {
            final Gateway gateway = deviceAddedEvent.getGateway();
            final Device device = deviceAddedEvent.getDevice();
            final PlatformParameters platform = platformProvider.getPlatformProperties(gateway);

            subscribeDeviceOperations(platform, gateway, device);
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void unsubscribe(final GatewayRemovedEvent event) {
        try {
            unsubscribeGateway(event.getGateway());
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void unsubscribe(final DeviceRemovedEvent deviceRemovedEvent) {
        try {
            unsubscribeDevice(deviceRemovedEvent.getDevice());
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void subscribeGatewayOperations(final PlatformParameters platform, final Gateway gateway) {
        final OperationNotificationSubscriber operationSubscriber = notifications.subscribeOperations(platform, gateway.getId(), new OperationListener() {
            public void onCreate(OperationRepresentation operationRepresentation) {
//                fieldbus model UI default operations
                final Object opcuaDeviceConfigurationChange = operationRepresentation.get(Operation.OPCUA_DEVICE);
                if (opcuaDeviceConfigurationChange != null) {
                    operationRepository.successful(gateway, operationRepresentation.getId());
                } else {
                    final Optional<Operation> operation = operationFactory.create(operationRepresentation);
                    if (operation.isPresent()) {
                        eventPublisher.publishEvent(new OperationEvent(gateway, operation.get()));
                    } else {
                        operationRepository.failed(gateway, operationRepresentation.getId(), "Operation not supported.");
                    }
                }
            }
        });
        operationSubscribers.add(gateway.getId(), operationSubscriber);
    }

    private void subscribeDeviceOperations(PlatformParameters platform, final Gateway gateway, final Device device) {
        final OperationNotificationSubscriber operationsSubscriber = notifications.subscribeOperations(platform, device.getId(), new OperationListener() {
            public void onCreate(OperationRepresentation operationRepresentation) {
                final Optional<Operation> operation = operationFactory.create(operationRepresentation);
                if (operation.isPresent()) {
                    eventPublisher.publishEvent(new OperationEvent(gateway, operation.get()));
                } else {
                    operationRepository.failed(gateway, operationRepresentation.getId(), "Operation not supported.");
                }
            }
        });
        operationSubscribers.add(device.getId(), operationsSubscriber);
    }

    private void subscribeGatewayInventory(final PlatformParameters platform, final Gateway gateway) {
        final Subscriber<String, ManagedObjectDeleteAwareNotification> inventorySubscriber = notifications.subscribeInventory(platform, gateway.getId(), new ManagedObjectListener() {
            @Override
            public void onUpdate(Object value) {
                final Optional<ManagedObjectRepresentation> managedObject = managedObjectRepository.get(gateway);
                if (managedObject.isPresent()) {
                    final Optional<Gateway> newGatewayOptional = gatewayFactory.create(gateway, managedObject.get());
                    if (newGatewayOptional.isPresent()) {
                        final Gateway newGateway = newGatewayOptional.get();
                        gatewayRepository.save(newGateway);
                        eventPublisher.publishEvent(new GatewayUpdateEvent(newGateway));
                    }
                }
            }
        });
        managedObjectSubscribers.add(gateway.getId(), inventorySubscriber);
        eventPublisher.publishEvent(new PlatformSubscribedEvent(gateway));
    }

    private void unsubscribeGateway(final Gateway gateway) {
        operationSubscribers.disconnect(gateway.getId());
        if (managedObjectSubscribers.disconnect(gateway.getId())) {
            eventPublisher.publishEvent(new PlatformUnsubscribedEvent(gateway));
        }
    }

    private void unsubscribeDevice(final Device device) {
        operationSubscribers.disconnect(device.getId());
        managedObjectSubscribers.disconnect(device.getId());
    }
}
