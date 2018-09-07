package com.cumulocity.opcua.platform.notification;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.platform.notification.model.ManagedObjectListener;
import com.cumulocity.opcua.platform.notification.model.OperationListener;
import com.cumulocity.rest.representation.operation.OperationRepresentation;
import com.cumulocity.sdk.client.PlatformParameters;
import com.cumulocity.sdk.client.cep.notification.InventoryRealtimeDeleteAwareNotificationsSubscriber;
import com.cumulocity.sdk.client.cep.notification.ManagedObjectDeleteAwareNotification;
import com.cumulocity.sdk.client.devicecontrol.notification.OperationNotificationSubscriber;
import com.cumulocity.sdk.client.notification.Subscription;
import com.cumulocity.sdk.client.notification.SubscriptionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Notifications {

    public OperationNotificationSubscriber subscribeOperations(PlatformParameters platform, GId id, final OperationListener listener) {
        final OperationNotificationSubscriber result = createOperationsSubscriber(platform);
        result.subscribe(id, new SubscriptionListener<GId, OperationRepresentation>() {
            @Override
            public void onNotification(Subscription<GId> subscription, OperationRepresentation operation) {
                listener.onCreate(operation);
            }

            @Override
            public void onError(Subscription<GId> subscription, Throwable throwable) {
                listener.onError(throwable);
            }
        });
        return result;
    }

    public InventoryRealtimeDeleteAwareNotificationsSubscriber subscribeInventory(PlatformParameters platform, GId id, final ManagedObjectListener listener) {
        final InventoryRealtimeDeleteAwareNotificationsSubscriber result = createInventorySubscriber(platform);
        result.subscribe(id.getValue(), new SubscriptionListener<String, ManagedObjectDeleteAwareNotification>() {
            @Override
            public void onNotification(Subscription<String> subscription, ManagedObjectDeleteAwareNotification notification) {
                if ("UPDATE".equals(notification.getRealtimeAction())) {
                    listener.onUpdate(notification.getData());
                } else if ("DELETE".equals(notification.getRealtimeAction())) {
                    listener.onDelete();
                }
            }

            @Override
            public void onError(Subscription<String> subscription, Throwable throwable) {
                listener.onError(throwable);
            }
        });
        return result;
    }

    protected OperationNotificationSubscriber createOperationsSubscriber(PlatformParameters platform) {
        return new OperationNotificationSubscriber(platform);
    }

    protected InventoryRealtimeDeleteAwareNotificationsSubscriber createInventorySubscriber(PlatformParameters platform) {
        return new InventoryRealtimeDeleteAwareNotificationsSubscriber(platform);
    }
}
