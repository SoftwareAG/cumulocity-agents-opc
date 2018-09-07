package com.cumulocity.opcua.gateway.model.device;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.HasBrowsePath;
import com.cumulocity.opcua.gateway.model.core.HasKey;
import com.cumulocity.opcua.gateway.model.core.HasTenant;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.repository.core.PersistableType;
import com.google.common.base.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

@Data
@Wither
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PersistableType(value = "Device", runWithinContext = HasTenant.class, inMemory = true)
public class Device implements HasKey, HasBrowsePath {
    public static final String c8y_OPCUADevice = "c8y_OPCUADevice";

    private GId id;
    private BrowsePath browsePath;
    private GId deviceType;

    @UtilityClass
    public static class Method {
        public static Function<Device, String> getId() {
            return new Function<Device, String>() {
                @Override
                public String apply(final Device device) {
                    return device.getId().getValue();
                }
            };
        }

        public static Function<GId, String> gidGetValue() {
            return new Function<GId, String>() {
                @Override
                public String apply(final GId id) {
                    return id.getValue();
                }
            };
        }
    }
}
