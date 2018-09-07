package com.cumulocity.opcua.gateway.model.core;

public interface Credentials extends HasTenant {
    String getTenant();
    String getName();
    String getPassword();
}
