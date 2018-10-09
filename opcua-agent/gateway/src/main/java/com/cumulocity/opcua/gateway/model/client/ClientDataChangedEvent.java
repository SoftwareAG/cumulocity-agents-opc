package com.cumulocity.opcua.gateway.model.client;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import lombok.Data;
import org.opcfoundation.ua.builtintypes.DateTime;

import java.util.Date;

@Data
public class ClientDataChangedEvent implements HasGateway {
    private final Gateway gateway;
    private final Device device;
    private final Register register;
    private final DateTime time;
    private final Object value;
}
