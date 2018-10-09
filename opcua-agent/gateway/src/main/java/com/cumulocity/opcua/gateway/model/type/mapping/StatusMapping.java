package com.cumulocity.opcua.gateway.model.type.mapping;

import com.cumulocity.opcua.gateway.model.type.Status;
import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusMapping implements Mapping {
    @JsonProperty("status")
    private Status type;
}
