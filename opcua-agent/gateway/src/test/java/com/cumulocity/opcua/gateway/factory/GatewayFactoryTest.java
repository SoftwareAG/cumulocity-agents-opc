package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.platform.factory.ManagedObjectMapper;
import com.cumulocity.rest.representation.devicebootstrap.DeviceCredentialsRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.HashMap;

import static com.cumulocity.model.idtype.GId.asGId;
import static com.cumulocity.opcua.Conditions.present;
import static com.cumulocity.opcua.gateway.repository.configuration.RepositoryConfiguration.objectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;

public class GatewayFactoryTest {

    private GatewayFactory gatewayFactory = new GatewayFactory(new ManagedObjectMapper(objectMapper()));

    @Test
    public void shouldCreate() {
        final HashMap<Object, Object> fragment = new HashMap<>();
        fragment.put("pollingRate", "10");
        fragment.put("transmitRate", "20");
        fragment.put("securityMode", "BASIC256SHA256_SIGN");
        fragment.put("userIdentityName", "user");
        final ManagedObjectRepresentation managedObject = new ManagedObjectRepresentation();
        managedObject.setId(asGId("10400"));
        managedObject.setProperty("c8y_OPCUAGateway", fragment);
        final DeviceCredentialsRepresentation credentials = new DeviceCredentialsRepresentation();
        credentials.setTenantId("tenant");
        credentials.setUsername("username");
        credentials.setPassword("password");

        final Optional<Gateway> gatewayOptional = gatewayFactory.create(credentials, managedObject);

        assertThat(gatewayOptional).is(present());
        assertThat(gatewayOptional.get()).isEqualTo(Gateway.gateway()
                .tenant("tenant")
                .name("username")
                .password("password")
                .id(GId.asGId("10400"))
                .pollingRateInSeconds(10D)
                .transmitRateInSeconds(20L)
                .securityMode("BASIC256SHA256_SIGN")
                .userIdentityName("user")
                .currentDeviceIds(Lists.<GId>newArrayList())
                .build());
    }

    @Test
    public void shouldNotCreate() {
        final ManagedObjectRepresentation managedObject = new ManagedObjectRepresentation();
        final DeviceCredentialsRepresentation credentials = new DeviceCredentialsRepresentation();

        Optional<Gateway> gatewayOptional = gatewayFactory.create(credentials, managedObject);

        assertThat(gatewayOptional).is(not(present()));
    }
}
