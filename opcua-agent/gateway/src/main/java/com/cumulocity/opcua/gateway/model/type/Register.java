package com.cumulocity.opcua.gateway.model.type;

import com.cumulocity.opcua.gateway.model.core.HasBrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.cumulocity.opcua.gateway.model.type.core.Conversion;
import com.cumulocity.opcua.gateway.model.type.core.ConvertHelper;
import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.cumulocity.opcua.gateway.model.type.mapping.*;
import lombok.*;
import lombok.experimental.Wither;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = "name")
public class Register implements Conversion, HasBrowsePath {
    public static final String c8y_RegisterStatus = "c8y_RegisterStatus";

    @NotNull
    private String name;

    @Nullable
    private String category;

    @Wither
    @NotNull
    private BrowsePath browsePath;

    private String unit;

    @NotNull
    private AttributeType attributeType;

    @Nullable
    private StatusMapping statusMapping;

    @Nullable
    private AlarmMapping alarmMapping;

    @Nullable
    private MeasurementMapping measurementMapping;

    @Nullable
    private EventMapping eventMapping;

    @Nullable
    private ManagedObjectMapping managedObjectMapping;

    private Double divisor;

    private Double multiplier;

    private Double offset;

    private Integer decimalPlaces;

    private Double min;

    private Double max;

    public Iterable<Mapping> mappings() {
        final LinkedList<Mapping> result = new LinkedList<>();
        if (statusMapping != null) {
            result.add(statusMapping);
        }
        if (alarmMapping != null) {
            result.add(alarmMapping);
        }
        if (measurementMapping != null) {
            result.add(measurementMapping);
        }
        if (eventMapping != null) {
            result.add(eventMapping);
        }
        if (managedObjectMapping != null) {
            result.add(managedObjectMapping);
        }
        return result;
    }

    public Object convert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Number) {
            return ConvertHelper.convertToDouble(((Number) object).doubleValue(), this);
        }
        if (object instanceof Boolean) {
            if ((Boolean) object)  {
                return 1;
            } else {
                return 0;
            }
        }
        return object;
    }
}
