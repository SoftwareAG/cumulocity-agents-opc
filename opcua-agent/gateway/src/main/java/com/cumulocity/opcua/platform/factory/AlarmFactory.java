package com.cumulocity.opcua.platform.factory;

import com.cumulocity.opcua.gateway.factory.core.PlatformRepresentationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmMapping;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmSeverity;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

@Slf4j
@Component
public class AlarmFactory implements PlatformRepresentationFactory<AlarmMapping, AlarmRepresentation> {
    @Override
    public Optional<AlarmRepresentation> apply(DateTime date, Gateway gateway, Device device, Register register, AlarmMapping mapping, Object value) {
        if (isValueOk(register, value)) {
            return absent();
        }
        final ManagedObjectRepresentation source = new ManagedObjectRepresentation();
        if (device == null) {
            source.setId(gateway.getId());
        } else {
            source.setId(device.getId());
        }

        final AlarmRepresentation result = new AlarmRepresentation();
        result.setSource(source);
        result.setSeverity(AlarmSeverity.asString(mapping.getSeverity()));
        result.setType(mapping.getType());
        result.setText(mapping.getText());
        result.setDateTime(date);
        result.setStatus(mapping.getStatus());

        return of(result);
    }

    private boolean isValueOk(Register register, Object value) {
        try {
            if (register == null) {
                return false;
            }
            final Object converted = register.convert(value);
            if (converted == null) {
                return true;
            }
            if (converted.toString().equals("0")) {
                return true;
            }
            return Integer.parseInt(converted.toString()) == 0;
        } catch (final Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }
}
