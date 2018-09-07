package com.cumulocity.opcua.gateway.repository;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.factory.DeviceTypeFactory;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import com.cumulocity.opcua.platform.repository.ManagedObjectRepository;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeviceTypeInventoryRepository {
    private final ManagedObjectRepository managedObjectRepository;
    private final DeviceTypeFactory deviceTypeFactory;

    @RunWithinContext
    public Optional<DeviceType> get(@NonNull Gateway gateway, @NonNull GId managedObjectId) {
        final Optional<ManagedObjectRepresentation> managedObject = managedObjectRepository.get(managedObjectId);
        if (managedObject.isPresent()) {
            return deviceTypeFactory.create(managedObject.get());
        }
        return Optional.absent();
    }
}
