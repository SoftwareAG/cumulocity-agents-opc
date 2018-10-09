package com.cumulocity.opcua.gateway.model.client;

import com.cumulocity.opcua.gateway.model.core.ConfigEvent;
import com.cumulocity.opcua.gateway.model.core.ConfigEventType;
import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import lombok.Data;

@Data
public class DeviceConfigSuccessEvent implements HasGateway, ConfigEvent {
    private final Gateway gateway;
    private final Device device;
    private final ConfigEventType type;

    @Override
    public String getMessage() {
        return ConfigEventType.getMessage(type);
    }
}
