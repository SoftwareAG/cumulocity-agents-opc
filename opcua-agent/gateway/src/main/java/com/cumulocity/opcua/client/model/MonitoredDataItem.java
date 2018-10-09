package com.cumulocity.opcua.client.model;

import lombok.Data;

@Data
public class MonitoredDataItem {
    private final com.prosysopc.ua.client.MonitoredDataItem target;
}
