package com.cumulocity.opcua.gateway.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;

@Getter
@AllArgsConstructor
public enum AttributeType {
    VALUE(Attributes.Value),

    EVENT_NOTIFIER(Attributes.EventNotifier),

    BROWSE_NAME(Attributes.BrowseName);

    private final UnsignedInteger value;

    @JsonCreator
    public static AttributeType fromString(String string) {
        for (final AttributeType status : AttributeType.values()) {
            if (status.name().equalsIgnoreCase(string)) {
                return status;
            }
        }
        return null;
    }
}
