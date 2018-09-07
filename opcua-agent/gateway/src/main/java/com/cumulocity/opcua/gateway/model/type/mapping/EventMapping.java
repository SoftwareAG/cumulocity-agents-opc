package com.cumulocity.opcua.gateway.model.type.mapping;

import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventMapping implements Mapping {
    private String type;
    private String text;
}
