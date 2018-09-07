package com.cumulocity.opcua.gateway.model.type;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.HasKey;
import com.cumulocity.opcua.gateway.model.core.HasTenant;
import com.cumulocity.opcua.gateway.repository.core.PersistableType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Wither;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Builder
@Wither
@AllArgsConstructor
@NoArgsConstructor
@PersistableType(value = "DeviceType", runWithinContext = HasTenant.class, inMemory = true)
public class DeviceType implements HasKey {

    public static final String FIELDBUS_TYPE = "fieldbusType";
    public static final String OPCUA_TYPE = "opcua";

    private GId id;

    private String name;

    private String type;

    @Nullable
    @JsonProperty(FIELDBUS_TYPE)
    private String fieldbusType;

    @Nullable
    @Singular
    @JsonProperty("c8y_Registers")
    private List<Register> registers;

    @Nullable
    @JsonProperty("c8y_useServerTime")
    private Boolean useServerTime;
}
