package com.cumulocity.opcua.client.service.configuration;

import com.cumulocity.opcua.client.model.ExpandedNodeId;
import com.cumulocity.opcua.client.model.MonitoredDataItem;
import com.cumulocity.opcua.client.model.Subscription;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.service.utils.Consumer;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.core.MonitoringMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.cumulocity.opcua.gateway.model.type.core.BrowsePath.concat;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.opcfoundation.ua.builtintypes.UnsignedInteger.valueOf;
import static org.opcfoundation.ua.core.Attributes.Value;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubscriptionFactory {

    private final ClientFactory clientProvider;

    @Value("${subscription.notificationBufferSize:10000}")
    private int notificationBufferSize;

    @Value("${subscription.publishingEnabled:true}")
    private boolean publishingEnabled;

    @Value("${subscription.publishingInterval:1000}")
    private int defaultPublishingInterval;

    @Value("${subscription.maxNotificationPerPublish:1000}")
    private long maxNotificationPerPublish;

    @Value("${subscription.maxMonitoredItemsPerCall:1000}")
    private int maxMonitoredItemsPerCall;

    @Value("${monitoredDataItem.queueSize:1}")
    private int monitoredDataItemQueueSize;

//    if shorter then publishing interval then server should queue all notifications on its side
    @Value("${monitoredDataItem.samplingInterval:50}")
    private int samplingInterval;

    @Value("${monitoredDataItem.monitoringMode:Reporting}")
    private String monitoringMode;

    @SneakyThrows
    public Subscription createSubscription(Gateway gateway) {
        final Double pollingRateInSeconds = gateway.getPollingRateInSeconds();
        return new Subscription(pollingRateInSeconds, defaultPublishingInterval, publishingEnabled, monitoringMode, notificationBufferSize, maxNotificationPerPublish, maxMonitoredItemsPerCall);
    }

    @SneakyThrows
    public Optional<MonitoredDataItem> createMonitoredDataItem(Device device, Register register, Consumer<DataValue> consumer) {
        final BrowsePath fullBrowsePath = concat(device.getBrowsePath(), register.getBrowsePath());
        final Optional<ExpandedNodeId> nodeIdOptional = clientProvider.client().targetNodeId(fullBrowsePath);
        if (nodeIdOptional.isPresent()) {
            final long attributeType = targetAttributeId(register);
            return of(monitoredDataItem(nodeIdOptional.get(), attributeType, consumer));
        }
        return absent();
    }

    @SneakyThrows
    private MonitoredDataItem monitoredDataItem(ExpandedNodeId nodeId, long attributeType, final Consumer<DataValue> listener) {
        MonitoringMode monitoringMode = MonitoringMode.valueOf(this.monitoringMode);
        final com.prosysopc.ua.client.MonitoredDataItem monitoredDataItem = new com.prosysopc.ua.client.MonitoredDataItem(nodeId.getTarget(), valueOf(attributeType), monitoringMode);
        monitoredDataItem.setDataChangeListener(new com.prosysopc.ua.client.MonitoredDataItemListener() {
            @Override
            public void onDataChange(final com.prosysopc.ua.client.MonitoredDataItem monitoredDataItem, DataValue prevValue, DataValue newValue) {
                listener.apply(newValue);
            }
        });
        monitoredDataItem.setQueueSize(monitoredDataItemQueueSize);
        if (samplingInterval != monitoredDataItem.getSamplingInterval()) {
            log.info("samplingInterval {} => {}", monitoredDataItem.getSamplingInterval(), samplingInterval);
            monitoredDataItem.setSamplingInterval(samplingInterval, MILLISECONDS);
        }
        return new MonitoredDataItem(monitoredDataItem);
    }

    private long targetAttributeId(Register register) {
        if (register.getAttributeType() == null) {
            return Value.getValue();
        }
        return register.getAttributeType().getValue().getValue();
    }
}
