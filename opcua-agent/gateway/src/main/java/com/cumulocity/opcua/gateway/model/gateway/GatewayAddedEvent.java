package com.cumulocity.opcua.gateway.model.gateway;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.HasGateway;
import lombok.Data;

@Data
public final class GatewayAddedEvent implements HasGateway {
    private final Gateway gateway;

    public GId getGatewayId() {
        return gateway.getId();
    }
}
