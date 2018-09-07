package com.cumulocity.opcua.gateway.model.core;

import com.cumulocity.opcua.gateway.model.gateway.Gateway;

public interface HasGateway {
    Gateway getGateway();
}
