package com.cumulocity.opcua.client.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.client.model.Client;
import com.cumulocity.opcua.client.model.MonitoredDataItem;
import com.cumulocity.opcua.client.model.Subscription;
import com.cumulocity.opcua.client.service.configuration.SubscriptionFactory;
import com.cumulocity.opcua.client.service.configuration.ClientFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayUpdateEvent;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.gateway.service.utils.Consumer;
import com.cumulocity.opcua.platform.repository.OperationRepository;
import com.google.common.base.Optional;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientSubscriberTest {

    @Mock
    private SubscriptionFactory clientFactory;

    @Mock
    private Repository<DeviceType> deviceTypeRepository;

    @Mock
    private Repository<Device> deviceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private ClientFactory clientProvider;

    private ClientSubscriber subscriber;

    @Before
    public void before() {
        subscriber = new ClientSubscriber(
                clientFactory, deviceTypeRepository, deviceRepository, eventPublisher, operationRepository, clientProvider
        );
    }

    @Test
    public void shouldUpdatePublishingInterval() {
        final Gateway gateway = new Gateway()
                .withUrl("opc.tcp://localhost:52520/OPCUA/IntegrationTestServer")
                .withCurrentDeviceIds(newArrayList(createId()));
        final Device device = new Device()
                .withId(createId());
        final Register register = new Register();
        final DeviceType deviceType = new DeviceType()
                .withId(createId())
                .withRegisters(Lists.newArrayList(register));
        final MonitoredDataItem monitoredDataItem = mock(MonitoredDataItem.class);
        final Subscription subscription = mock(Subscription.class);
        final Client client = mock(Client.class);

        when(subscription.getItemCount()).thenReturn(1);
        when(clientProvider.client()).thenReturn(client);
        when(clientFactory.createSubscription(gateway)).thenReturn(subscription);
        when(clientFactory.createMonitoredDataItem(eq(device), eq(register), any(Consumer.class))).thenReturn(Optional.of(monitoredDataItem));
        when(deviceRepository.get(any(GId.class))).thenReturn(Optional.of(device));
        when(deviceTypeRepository.get(any(GId.class))).thenReturn(Optional.of(deviceType));
        subscriber.refreshSubscriptions(new GatewayAddedEvent(gateway));
        subscriber.refreshSubscriptions(new GatewayUpdateEvent(gateway.withPollingRateInSeconds(15d)));

        assertThat(subscriber.getSubscriptionMap()).hasSize(1);
        verify(subscription).setPollingRateInSeconds(15d);
    }

    private GId createId() {
        return GId.asGId(ThreadLocalRandom.current().nextInt());
    }
}
