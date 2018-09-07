package com.cumulocity.opcua.gateway.model.gateway;

import com.cumulocity.opcua.gateway.model.core.HasGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public final class GatewayUpdateEvent implements HasGateway {
    private final Gateway gateway;
}
