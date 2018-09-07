package com.cumulocity.opcua.gateway.model.type.mapping;

import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementMapping implements Mapping {
    private String type;
    private String series;
}
