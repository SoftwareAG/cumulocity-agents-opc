package com.cumulocity.opcua.gateway.model.type.mapping;

import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder(builderMethodName = "alarmMapping")
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMapping implements Mapping {
    public static final String c8y_ValidationError = "c8y_ValidationError";

    @NotNull
    private String type;
    private String text;
    private AlarmSeverity severity;
//    ACTIVE, ACKNOWLEDGED or CLEARED
    private String status;
}
