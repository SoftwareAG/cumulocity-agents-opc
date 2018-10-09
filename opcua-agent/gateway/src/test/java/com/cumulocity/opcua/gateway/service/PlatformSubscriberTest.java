package com.cumulocity.opcua.gateway.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.factory.GatewayFactory;
import com.cumulocity.opcua.gateway.factory.OperationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.operation.Operation;
import com.cumulocity.opcua.gateway.model.operation.OperationEvent;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.platform.notification.Notifications;
import com.cumulocity.opcua.platform.notification.model.ManagedObjectListener;
import com.cumulocity.opcua.platform.notification.model.OperationListener;
import com.cumulocity.opcua.platform.repository.ManagedObjectRepository;
import com.cumulocity.opcua.platform.repository.OperationRepository;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProvider;
import com.cumulocity.rest.representation.AbstractExtensibleRepresentation;
import com.cumulocity.rest.representation.operation.OperationRepresentation;
import com.cumulocity.sdk.client.PlatformParameters;
import com.cumulocity.sdk.client.cep.notification.InventoryRealtimeDeleteAwareNotificationsSubscriber;
import com.cumulocity.sdk.client.devicecontrol.notification.OperationNotificationSubscriber;
import com.cumulocity.sdk.client.notification.SubscriptionListener;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.atomic.AtomicReference;

import static com.cumulocity.model.idtype.GId.asGId;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PlatformSubscriberTest {


    private PlatformProvider platformProvider = mock(PlatformProvider.class);
    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private Repository<Gateway> gatewayRepository = mock(Repository.class);
    private GatewayFactory gatewayFactory = mock(GatewayFactory.class);
    private OperationFactory operationFactory = mock(OperationFactory.class);
    private ManagedObjectRepository managedObjectRepository = mock(ManagedObjectRepository.class);
    private OperationRepository operationRepository = mock(OperationRepository.class);
    private OperationNotificationSubscriber operationSubscriber = mock(OperationNotificationSubscriber.class);
    private InventoryRealtimeDeleteAwareNotificationsSubscriber inventorySubscriber = mock(InventoryRealtimeDeleteAwareNotificationsSubscriber.class);
    private PlatformParameters platform = mock(PlatformParameters.class);
    private Notifications notifications = new Notifications() {
        @Override
        protected OperationNotificationSubscriber createOperationsSubscriber(PlatformParameters platform) {
            return operationSubscriber;
        }

        @Override
        protected InventoryRealtimeDeleteAwareNotificationsSubscriber createInventorySubscriber(PlatformParameters platform) {
            return inventorySubscriber;
        }
    };

    public PlatformSubscriber subscriber = new PlatformSubscriber(
            platformProvider, 
            eventPublisher, 
            gatewayRepository, 
            gatewayFactory, 
            operationFactory, 
            managedObjectRepository, 
            operationRepository,
            notifications
    );

    private AtomicReference<SubscriptionListener> subscription = new AtomicReference<>();

    @Before
    public void before() {
        when(operationSubscriber.subscribe(any(GId.class), any(SubscriptionListener.class))).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) {
                final SubscriptionListener argumentAt = invocationOnMock.getArgumentAt(1, SubscriptionListener.class);
                subscription.set(argumentAt);
                return null;
            }
        });
        when(platformProvider.getPlatformProperties(any(Gateway.class))).thenReturn(platform);
    }

    @Test
    public void shouldUpdateOperationWithStateSuccessful() {
        final Gateway gateway = createGateway();
        final OperationRepresentation operation = createOperationRepresentation();

        when(operationFactory.create(any(AbstractExtensibleRepresentation.class))).thenReturn(Optional.<Operation>absent());
        subscriber.refreshSubscriptions(new GatewayAddedEvent(gateway));
        subscription.get().onNotification(null, operation);

        verify(operationRepository).successful(gateway, operation.getId());
    }

    @Test
    public void shouldSendOperationEvent() {
        final Gateway gateway = createGateway();
        final Device device = createDevice();
        final Operation operation = createOperation();

        when(operationFactory.create(any(AbstractExtensibleRepresentation.class))).thenReturn(Optional.of(operation));
        subscriber.subscribe(new DeviceAddedEvent(gateway, device));
        subscription.get().onNotification(null, new OperationRepresentation());

        verify(eventPublisher).publishEvent(new OperationEvent(gateway, operation));
    }

    private OperationRepresentation createOperationRepresentation() {
        final OperationRepresentation result = new OperationRepresentation();
        result.setProperty("c8y_OPCUADevice", ImmutableMap.of());
        result.setId(asGId(randomNumeric(2)));
        return result;
    }

    private Device createDevice() {
        return new Device().withId(GId.asGId(randomNumeric(2)));
    }

    private Gateway createGateway() {
        return new Gateway().withId(asGId(randomNumeric(2)));
    }

    private Operation createOperation() {
        return new Operation().withId(asGId(randomNumeric(2)));
    }
}
