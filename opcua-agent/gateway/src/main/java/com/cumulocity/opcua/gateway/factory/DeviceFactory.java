package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.cumulocity.opcua.gateway.model.common.SimpleTypeUtils.parseGId;
import static com.cumulocity.opcua.gateway.model.device.Device.c8y_OPCUADevice;
import static com.google.common.base.Optional.absent;

@Component
public class DeviceFactory {
    @Nonnull
    public Optional<Device> create(ManagedObjectRepresentation managedObject) {
        if (managedObject.hasProperty(c8y_OPCUADevice)) {
            final Map property = (Map) managedObject.getProperty(c8y_OPCUADevice);
            final GId deviceId = managedObject.getId();
            return Optional.of(Device.builder()
                    .id(deviceId)
                    .browsePath(BrowsePath.asBrowsePath(property.get("browsePath")))
                    .deviceType(parseGId(property.get("type")))
                    .build());
        }
        return absent();
    }
}
