package com.cumulocity.opcua.gateway.model.operation;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import lombok.Data;

@Data
public class OperationEvent implements HasGateway{
    private final Gateway gateway;
    private final Operation operation;
}
