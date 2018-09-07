package com.cumulocity.opcua.platform.factory;

import c8y.*;
import com.cumulocity.model.Agent;
import com.cumulocity.opcua.gateway.factory.core.PlatformRepresentationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.mapping.StatusMapping;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.cumulocity.opcua.gateway.model.gateway.Gateway.TYPE;
import static com.cumulocity.opcua.gateway.model.gateway.Gateway.c8y_OPCUAGateway;
import static com.cumulocity.opcua.gateway.model.type.Register.c8y_RegisterStatus;
import static com.google.common.base.Optional.of;

@Component
public class ManagedObjectFactory implements PlatformRepresentationFactory<StatusMapping, ManagedObjectRepresentation> {

    private LoadingCache<Device, ManagedObjectRepresentation> cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new CacheLoader<Device, ManagedObjectRepresentation>() {
                public ManagedObjectRepresentation load(Device device) throws Exception {
                    final ManagedObjectRepresentation result = new ManagedObjectRepresentation();
                    result.setId(device.getId());
                    result.setProperty(c8y_RegisterStatus, new ConcurrentHashMap<>());
                    return result;
                }
            });

    @Override
    public Optional<ManagedObjectRepresentation> apply(DateTime time, Gateway gateway, Device device, Register register, StatusMapping var1, Object value) {
        final ManagedObjectRepresentation result = cache.getUnchecked(device);
        final Map registerFragment = (Map) result.getProperty(c8y_RegisterStatus);
        registerFragment.put(register.getName(), register.convert(value));
        return of(result);
    }

    @Nonnull
    public ManagedObjectRepresentation create(String name) {
        final ManagedObjectRepresentation result = new ManagedObjectRepresentation();
        result.setType(TYPE);
        result.setName(name);

        result.set(new Agent());
        result.set(new IsDevice());
        result.set(new Hardware());
        result.set(new Mobile());
        result.set(new Object(), c8y_OPCUAGateway);

        result.set(createSupportedOperationsFragment());
//        result.set(createRequiredAvailabilityFragment());

        return result;
    }

    private RequiredAvailability createRequiredAvailabilityFragment() {
        return new RequiredAvailability(10);
    }

    private SupportedOperations createSupportedOperationsFragment() {
        SupportedOperations result = new SupportedOperations();
        result.add("c8y_OPCUAConfiguration");
        return result;
    }
}
