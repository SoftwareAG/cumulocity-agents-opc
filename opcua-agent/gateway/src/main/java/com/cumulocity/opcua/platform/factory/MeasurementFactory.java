package com.cumulocity.opcua.platform.factory;

import com.cumulocity.opcua.gateway.factory.core.PlatformRepresentationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.mapping.MeasurementMapping;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Optional.of;

@Component
public class MeasurementFactory implements PlatformRepresentationFactory<MeasurementMapping, MeasurementRepresentation> {
    @Override
    public Optional<MeasurementRepresentation> apply(DateTime dateTime, Gateway gateway, Device device, Register register, MeasurementMapping mapping, Object value) {
        final ManagedObjectRepresentation source = new ManagedObjectRepresentation();
        source.setId(device.getId());

        final MeasurementRepresentation result = new MeasurementRepresentation();
        result.setSource(source);
        result.setDateTime(dateTime);
        storeValue(register, mapping, value, result);

        return of(result);
    }

    private void storeValue(Register register, MeasurementMapping mapping, Object value, MeasurementRepresentation result) {
        result.setType(mapping.getType());

        final Map<String, Object> series = Maps.newHashMap();
        series.put("value", register.convert(value));
        series.put("unit", register.getUnit());

        final Map<String, Object> type = Maps.newHashMap();
        type.put(mapping.getSeries().replace(" ", "_"), series);
        result.setProperty(mapping.getType().replace(" ", "_"), type);
    }

}
