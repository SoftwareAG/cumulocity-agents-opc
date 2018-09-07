package com.cumulocity.opcua.gateway.model.device;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.DeviceType;
import lombok.Data;

@Data
public class DeviceTypeUpdatedEvent implements HasGateway {
    private final Gateway gateway;
    private final Device device;
    private final DeviceType deviceType;
}
