package com.cumulocity.opcua.platform.factory;

import com.cumulocity.opcua.gateway.factory.core.PlatformRepresentationFactory;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.mapping.EventMapping;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import static com.google.common.base.Optional.of;

@Component
public class EventFactory implements PlatformRepresentationFactory<EventMapping, EventRepresentation> {
    @Override
    public Optional<EventRepresentation> apply(DateTime time, Gateway gateway, Device device, Register register, final EventMapping mapping, final Object object) {
        final ManagedObjectRepresentation source = new ManagedObjectRepresentation();
        source.setId(device.getId());

        final EventRepresentation result = new EventRepresentation();
        result.setSource(source);
        result.setType(mapping.getType());
        result.setText(mapping.getText());
        result.setDateTime(time);
        result.setProperty(mapping.getType(), register.convert(object));
        return of(result);
    }
}
