package com.cumulocity.opcua.platform.model;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.model.core.HasKey;
import com.cumulocity.opcua.gateway.model.core.HasTenant;
import com.cumulocity.opcua.gateway.repository.core.PersistableType;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.cumulocity.sdk.client.measurement.MeasurementApi;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Data
@ToString(exclude = "measurementApi")
@NoArgsConstructor
@PersistableType(value = "Command", discriminator = "CreateMeasurementCommand", autowire = true, runWithinContext = HasTenant.class, inMemory = true)
public class Command implements HasKey {

    @Autowired
    @JsonIgnore
    private MeasurementApi measurementApi;
    private String key;
    private MeasurementRepresentation representation;

    public Command(final MeasurementRepresentation representation) {
        this.representation = representation;
        this.key = new HashCodeBuilder()
                .append(representation.getType())
                .append(representation.getAttrs())
                .append(representation.getDateTime())
                .append(representation.getSource())
                .build().toString();
    }

    public GId getId() {
        return GId.asGId(key);
    }

    public void execute() {
        measurementApi.create(representation);
    }
}
