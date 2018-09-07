package com.cumulocity.opcua.client.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.client.model.Client;
import com.cumulocity.opcua.client.model.MonitoredDataItem;
import com.cumulocity.opcua.client.model.Subscription;
import com.cumulocity.opcua.client.service.configuration.SubscriptionFactory;
import com.cumulocity.opcua.client.service.configuration.ClientFactory;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.client.ClientDataChangedEvent;
import com.cumulocity.opcua.gateway.model.client.DeviceConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigSuccessEvent;
import com.cumulocity.opcua.gateway.model.core.ConfigEventType;
import com.cumulocity.opcua.gateway.model.device.*;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayUpdateEvent;
import com.cumulocity.opcua.gateway.model.operation.Operation;
import com.cumulocity.opcua.gateway.model.operation.OperationEvent;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.gateway.service.utils.Consumer;
import com.cumulocity.opcua.platform.repository.OperationRepository;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cumulocity.opcua.gateway.model.core.ConfigEventType.*;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientSubscriber {

    private final SubscriptionFactory helper;
    private final Repository<DeviceType> deviceTypeRepository;
    private final Repository<Device> deviceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OperationRepository operationRepository;

    @Getter(PACKAGE)
    private final Map<GId, Subscription> subscriptionMap = new ConcurrentHashMap<>();
    private final ClientFactory clientProvider;

    @Value("${client.url:}")
    private String defaultUrl;

    @EventListener
    @RunWithinContext
    public synchronized void update(final OperationEvent event) {
        final Operation operation = event.getOperation();
        final Gateway gateway = event.getGateway();

        try {
            final Client client = clientProvider.client();
            if (client.writeAttribute(operation)) {
                operationRepository.successful(gateway, operation.getId());
            } else {
                operationRepository.failed(gateway, operation.getId(), "no browse path: " + operation);
                log.error("no browse path: {}", operation);
            }
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            operationRepository.failed(gateway, operation.getId(), ex.getMessage());
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscription(final DeviceTypeAddedEvent event) {
        final Device device = event.getDevice();
        unsubscribe(device.getId());
        subscribe(event.getGateway(), device, event.getDeviceType());
    }

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscription(final DeviceTypeUpdatedEvent event) {
        final Device device = event.getDevice();
        unsubscribe(device.getId());
        subscribe(event.getGateway(), device, event.getDeviceType());
    }

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscription(final DeviceAddedEvent event) {
        final Gateway gateway = event.getGateway();
        final Optional<DeviceType> deviceTypeOptional = deviceTypeRepository.get(event.getDevice().getDeviceType());
        if (deviceTypeOptional.isPresent()) {
            final Device device = event.getDevice();
            unsubscribe(device.getId());
            subscribe(gateway, device, deviceTypeOptional.get());
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscriptions(final GatewayAddedEvent event) {
        final Gateway gateway = event.getGateway();
        try {
            updateSubscriptions(gateway);
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            eventPublisher.publishEvent(new GatewayConfigErrorEvent(gateway, new ConfigEventType(ex.getMessage())));
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void refreshSubscriptions(final GatewayUpdateEvent event) {
        try {
            final Gateway gateway = event.getGateway();
            updateSubscriptions(gateway);
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void updateSubscriptions(Gateway gateway) {
        final List<GId> currentDeviceIds = gateway.getCurrentDeviceIds();
        if (currentDeviceIds != null) {
            for (final GId gId : currentDeviceIds) {
                final Optional<Device> deviceOptional = deviceRepository.get(gId);
                if (deviceOptional.isPresent()) {
                    final Device device = deviceOptional.get();
                    final Optional<DeviceType> deviceTypeOptional = deviceTypeRepository.get(device.getDeviceType());
                    if (deviceTypeOptional.isPresent()) {
                        final Subscription subscription = subscriptionMap.get(device.getId());
                        if (subscription == null) {
                            subscribe(gateway, device, deviceTypeOptional.get());
                        } else {
                            subscription.setPollingRateInSeconds(gateway.getPollingRateInSeconds());
                        }
                    }
                }
            }
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void unsubscribe(final GatewayRemovedEvent event) {
        for (final GId childDeviceId : event.getGateway().getCurrentDeviceIds()) {
            unsubscribe(childDeviceId);
        }
    }

    @EventListener
    @RunWithinContext
    public synchronized void unsubscribe(final DeviceRemovedEvent event) {
        unsubscribe(event.getDeviceId());
    }

    private void subscribe(final Gateway gateway, final Device device, DeviceType deviceType) {
        try {
            if (!validGateway(gateway)) {
                return;
            }
            if (!validRegisters(gateway, device, deviceType)) {
                return;
            }

            clearGatewayValidationErrors(gateway);

            final Subscription subscription = helper.createSubscription(gateway);
            for (final Register register : deviceType.getRegisters()) {
                final Optional<MonitoredDataItem> monitoredDataItem = helper.createMonitoredDataItem(device, register, new Consumer<DataValue>() {
                    public void apply(DataValue object) {
                        eventPublisher.publishEvent(new ClientDataChangedEvent(gateway, device, register, object.getSourceTimestamp(), object.getValue().getValue()));
                    }
                });

                if (validMonitoredItem(monitoredDataItem, gateway, device, register)) {
                    subscription.addItem(monitoredDataItem.get());
                }
            }

            if (validSubscription(gateway, device, subscription)) {
                clientProvider.client().addSubscription(subscription);

                subscriptionMap.put(device.getId(), subscription);
            }
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            eventPublisher.publishEvent(new GatewayConfigErrorEvent(gateway, new ConfigEventType(ex.getMessage())));
        }
    }

    private boolean validSubscription(Gateway gateway, Device device, Subscription subscription) {
        boolean valid = subscription.getItemCount() > 0;
        if (!valid) {
            eventPublisher.publishEvent(new DeviceConfigErrorEvent(gateway, device, null, NO_REGISTERS));
        }
        return valid;
    }

    private boolean validMonitoredItem(Optional<MonitoredDataItem> monitoredDataItem, Gateway gateway, Device device, Register register) {
        boolean present = monitoredDataItem.isPresent();
        if (!present) {
            eventPublisher.publishEvent(new DeviceConfigErrorEvent(gateway, device, register, BROWSE_PATH));
        }
        return present;
    }

    private boolean validRegisters(Gateway gateway, Device device, DeviceType deviceType) {
        boolean valid = isNotEmpty(deviceType.getRegisters());
        if (!valid) {
            eventPublisher.publishEvent(new DeviceConfigErrorEvent(gateway, device, null, NO_REGISTERS));
        }
        return valid;
    }

    private boolean validGateway(Gateway gateway) {
        setUpDefaultUrl(gateway);
        boolean valid = gateway.isUrlValid();
        if (!valid) {
            eventPublisher.publishEvent(new GatewayConfigErrorEvent(gateway, URL));
        }
        return valid;
    }

    private void clearGatewayValidationErrors(Gateway gateway) {
        eventPublisher.publishEvent(new GatewayConfigSuccessEvent(gateway, URL));
    }

    private void unsubscribe(final GId deviceId) {
        final Subscription subscription = subscriptionMap.get(deviceId);
        if (subscription != null) {
            clientProvider.client().removeSubscription(subscription);
            subscriptionMap.remove(deviceId);
        }
    }

    private void setUpDefaultUrl(Gateway gateway) {
        if (StringUtils.isBlank(gateway.getUrl())) {
            gateway.setUrl(defaultUrl);
        }
    }
}
