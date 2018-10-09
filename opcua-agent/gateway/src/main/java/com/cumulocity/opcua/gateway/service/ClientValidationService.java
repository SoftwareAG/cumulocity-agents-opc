package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.client.DeviceConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.client.DeviceConfigSuccessEvent;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigErrorEvent;
import com.cumulocity.opcua.gateway.model.client.GatewayConfigSuccessEvent;
import com.cumulocity.opcua.gateway.model.core.Alarms;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmClearedEvent;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmCreatedEvent;
import com.cumulocity.opcua.gateway.model.type.mapping.AlarmMapping;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.platform.factory.AlarmFactory;
import com.cumulocity.opcua.platform.repository.AlarmRepository;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.cumulocity.opcua.gateway.model.type.mapping.AlarmSeverity.CRITICAL;
import static com.cumulocity.opcua.gateway.model.type.mapping.AlarmMapping.c8y_ValidationError;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientValidationService {
    private final AlarmFactory alarmRepresentationFactory;
    private final AlarmRepository alarmRepository;
    private final Repository<Gateway> gatewayRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @RunWithinContext
    public void storeAlarm(DeviceConfigErrorEvent event) {
        final Gateway gateway = event.getGateway();
        final Device device = event.getDevice();

//        todo wojtek test for message
        final AlarmMapping alarmMapping = AlarmMapping.alarmMapping().type(c8y_ValidationError + "_" + event.getType()).text(event.getMessage()).severity(CRITICAL).build();
        final Optional<AlarmRepresentation> representation = alarmRepresentationFactory.apply(DateTime.now(), gateway, device, null, alarmMapping, null);
        if (representation.isPresent()) {
            final Optional<AlarmRepresentation> saved = alarmRepository.create(gateway, representation.get());
            if (saved.isPresent()) {
                gateway.getAlarms().add(event.getType(), saved.get());
                gatewayRepository.save(gateway);
                eventPublisher.publishEvent(new AlarmCreatedEvent(saved.get()));
            }
        }
    }

    @EventListener
    @RunWithinContext
    public void storeAlarm(GatewayConfigErrorEvent event) {
        final Gateway gateway = event.getGateway();
        final Alarms alarms = gateway.getAlarms();

        if (!alarms.existsBySourceAndType(gateway.getId(), event.getType())) {
            final AlarmMapping alarmMapping = AlarmMapping.alarmMapping().type(c8y_ValidationError).text(event.getType().getValue()).severity(CRITICAL).build();
            final Optional<AlarmRepresentation> representation = alarmRepresentationFactory.apply(DateTime.now(), gateway, null, null, alarmMapping, null);
            if (representation.isPresent()) {
                final Optional<AlarmRepresentation> saved = alarmRepository.create(gateway, representation.get());
                if (saved.isPresent()) {
                    alarms.add(event.getType(), saved.get());
                    gatewayRepository.save(gateway);
                    eventPublisher.publishEvent(new AlarmCreatedEvent(saved.get()));
                }
            }
        }
    }

    @EventListener
    @RunWithinContext
    public void clearAlarm(DeviceConfigSuccessEvent event) {
        final Gateway gateway = event.getGateway();
        final Device device = event.getDevice();
        final Alarms alarms = gateway.getAlarms();

        for (final AlarmRepresentation alarm : alarms.getBySourceAndType(device.getId(), event.getType())) {
            final Optional<AlarmRepresentation> updated = alarmRepository.clear(event.getGateway(), alarm);
            if (updated.isPresent()) {
                eventPublisher.publishEvent(new AlarmClearedEvent(updated.get()));
            }
        }
        alarms.clearBySourceAndType(device.getId(), event.getType());
        gatewayRepository.save(gateway);
    }

    @EventListener
    @RunWithinContext
    public void clearAlarms(final GatewayConfigSuccessEvent event) {
        final Gateway gateway = event.getGateway();
        final Alarms alarms = gateway.getAlarms();

        for (final AlarmRepresentation alarm : alarms.getBySourceAndType(gateway.getId(), event.getType())) {
            final Optional<AlarmRepresentation> updated = alarmRepository.clear(event.getGateway(), alarm);
            if (updated.isPresent()) {
                eventPublisher.publishEvent(new AlarmClearedEvent(updated.get()));
            }
        }
        alarms.clearBySourceAndType(gateway.getId(), event.getType());
        gatewayRepository.save(gateway);
    }
}
