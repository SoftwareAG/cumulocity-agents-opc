package com.cumulocity.opcua.client.model;

import lombok.Data;

@Data
public class ExpandedNodeId {
    private final org.opcfoundation.ua.builtintypes.ExpandedNodeId target;
}
