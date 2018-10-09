package com.cumulocity.opcua.gateway.model.core;

public interface ConfigEvent {

    String getMessage();

    ConfigEventType getType();
}
