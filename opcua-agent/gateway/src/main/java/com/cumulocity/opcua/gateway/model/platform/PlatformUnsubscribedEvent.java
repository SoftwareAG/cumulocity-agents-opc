package com.cumulocity.opcua.gateway.model.platform;

import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import lombok.Data;

@Data
public class PlatformUnsubscribedEvent {
    private final Gateway gateway;
}
