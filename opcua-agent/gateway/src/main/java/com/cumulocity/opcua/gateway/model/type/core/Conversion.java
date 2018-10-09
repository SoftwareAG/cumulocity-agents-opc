package com.cumulocity.opcua.gateway.model.type.core;

public interface Conversion {
    Double getMultiplier();

    Double getDivisor();

    Double getOffset();

    Integer getDecimalPlaces();
}
