package com.cumulocity.opcua.client.model;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opcfoundation.ua.core.MonitoringMode;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@RequiredArgsConstructor
public class Subscription {
    @Getter(PACKAGE)
    private volatile com.prosysopc.ua.client.Subscription target;

    private final Double pollingRateInSeconds;
    private final int defaultPublishingInterval;
    private final boolean publishingEnabled;
    private final String monitoringMode;
    private final int notificationBufferSize;
    private final long maxNotificationPerPublish;
    private final int maxMonitoredItemsPerCall;

    @SneakyThrows
    public void addItem(MonitoredDataItem monitoredDataItem) {
        ensureCreated();
        target.addItem(monitoredDataItem.getTarget());
    }

    public int getItemCount() {
        ensureCreated();
        return target.getItemCount();
    }

    public void setPollingRateInSeconds(Double pollingRateInSeconds) {
        ensureCreated();
        setPublishingInterval(target, pollingRateInSeconds);
    }

    private com.prosysopc.ua.client.Subscription create() throws ServiceException, StatusException {
        final com.prosysopc.ua.client.Subscription target = new com.prosysopc.ua.client.Subscription();
        setPublishingInterval(target, pollingRateInSeconds);

        if (publishingEnabled != target.isPublishingEnabled()) {
            log.info("publishingEnabled {} => {}", target.isPublishingEnabled(), publishingEnabled);
            target.setPublishingEnabled(publishingEnabled);
        }

        target.setMonitoringMode(MonitoringMode.valueOf(monitoringMode));
        target.setNotificationBufferSize(notificationBufferSize);
        target.setMaxNotificationsPerPublish(maxNotificationPerPublish);
        target.setMaxMonitoredItemsPerCall(maxMonitoredItemsPerCall);
        return target;
    }

    @SneakyThrows
    private void setPublishingInterval(com.prosysopc.ua.client.Subscription target, Double pollingRateInSeconds) {
        int newPublishingInterval = getPublishingIntervalInMillis(pollingRateInSeconds);
        if (newPublishingInterval != target.getPublishingInterval()) {
            log.info("publishingInterval {} => {}", target.getPublishingInterval(), newPublishingInterval);
            target.setPublishingInterval(newPublishingInterval, MILLISECONDS);
        }
    }

    private int getPublishingIntervalInMillis(Double pollingRateInSeconds) {
        if (pollingRateInSeconds != null) {
            final double publishingIntervalInMillis = pollingRateInSeconds * 1000;
            return (int) publishingIntervalInMillis;
        } else {
            return defaultPublishingInterval;
        }
    }

    @SneakyThrows
    private void ensureCreated() {
        if (target == null) {
            target = create();
        }
    }
}
