package com.cumulocity.opcua.gateway.model.device;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import lombok.Value;

@Value
public class DeviceRemovedEvent implements HasGateway {
    public static final String c8y_OPCUADeviceRemoved = "c8y_OPCUADeviceRemoved";
    private final Gateway gateway;
    private final Device device;

    public GId getDeviceId() {
        return getDevice().getId();
    }
}
