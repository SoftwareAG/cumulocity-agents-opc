package com.cumulocity.opcua.gateway.model.device;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class DeviceAddedEvent implements HasGateway {
    public static final String c8y_OPCUADeviceAdded = "c8y_OPCUADeviceAdded";
    private final Gateway gateway;
    private final Device device;
}
