package com.cumulocity.opcua.gateway.model.gateway;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import lombok.Data;

@Data
public final class GatewayRemovedEvent implements HasGateway {
    private final Gateway gateway;
}
