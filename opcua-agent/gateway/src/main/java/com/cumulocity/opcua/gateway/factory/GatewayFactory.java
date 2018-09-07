package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.Alarms;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.platform.factory.ManagedObjectMapper;
import com.cumulocity.rest.representation.devicebootstrap.DeviceCredentialsRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectReferenceRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatewayFactory {
    private final ManagedObjectMapper managedObjectMapper;

    public Optional<Gateway> create(Gateway credentials, ManagedObjectRepresentation managedObject) {
        return create(managedObject, credentials.getTenant(), credentials.getName(), credentials.getPassword(), credentials.getAlarms());
    }

    public Optional<Gateway> create(DeviceCredentialsRepresentation credentials, ManagedObjectRepresentation managedObject) {
        return create(managedObject, credentials.getTenantId(), credentials.getUsername(), credentials.getPassword(), new Alarms());
    }

    private Optional<Gateway> create(final ManagedObjectRepresentation managedObject, final String tenant, final String username, final String password, final Alarms alarms) {
        return managedObjectMapper.convert(Gateway.class, managedObject).transform(new Function<Gateway, Gateway>() {
            public Gateway apply(Gateway gateway) {
                return gateway
                        .withTenant(tenant)
                        .withName(username)
                        .withPassword(password)
                        .withAlarms(alarms)
                        .withCurrentDeviceIds(from(getChildDevices(managedObject)).transform(Method.toManagedObjectToId()).toList());
            }
        });
    }

    private Iterable<ManagedObjectReferenceRepresentation> getChildDevices(ManagedObjectRepresentation managedObject) {
        if (managedObject.getChildDevices() == null) {
            return newArrayList();
        }
        return managedObject.getChildDevices();
    }

    @UtilityClass
    public static class Method {
        public static Function<ManagedObjectReferenceRepresentation, GId> toManagedObjectToId() {
            return new Function<ManagedObjectReferenceRepresentation, GId>() {
                @Override
                public GId apply(final ManagedObjectReferenceRepresentation representation) {
                    return representation.getManagedObject().getId();
                }
            };
        }
    }
}
