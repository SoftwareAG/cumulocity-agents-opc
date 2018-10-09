package com.cumulocity.opcua.gateway.model.operation;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.platform.model.annotation.ExtensibleRepresentationView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@ExtensibleRepresentationView(fragment = Gateway.c8y_SetRegister, type = Gateway.TYPE)
public class Operation {
    public static final String OPCUA_DEVICE = "c8y_OPCUADevice";

    private GId id;
    private BrowsePath address;
    private BrowsePath register;
    private Object value;

    public Double doubleValue() {
        if (value == null) {
            return null;
        }
        if (Number.class.isInstance(value)) {
            return ((Number) value).doubleValue();
        }
        return new Double(String.valueOf(value));
    }

    public Integer intValue() {
        if (value == null) {
            return null;
        }
        if (Number.class.isInstance(value)) {
            return ((Number) value).intValue();
        }
        return new Integer(String.valueOf(value));
    }

    public Long longValue() {
        if (value == null) {
            return null;
        }
        if (Number.class.isInstance(value)) {
            return ((Number) value).longValue();
        }
        return new Long(String.valueOf(value));
    }

    public String stringValue() {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @JsonIgnore
    public BrowsePath getBrowsePath() {
        return BrowsePath.concat(address, register);
    }
}
