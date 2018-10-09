package com.cumulocity.opcua.gateway.service.utils;

public interface Consumer <T> {
    void apply(T object);
}
