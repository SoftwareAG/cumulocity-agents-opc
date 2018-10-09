package com.cumulocity.opcua.gateway.service;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.factory.core.PlatformRepresentationFactory;
import com.cumulocity.opcua.gateway.model.client.ClientDataChangedEvent;
import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.device.DeviceRemovedEvent;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.cumulocity.opcua.gateway.repository.core.PlatformRepresentationRepository;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

import static com.cumulocity.opcua.gateway.service.utils.BeanUtils.findBeanByGenericType;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientDataService {

    private final ApplicationContext applicationContext;
    private final ApplicationEventPublisher eventPublisher;
    private final Executor worker;

    @EventListener
    @RunWithinContext
    public void storeDataChange(final ClientDataChangedEvent event) {
        worker.execute(new Runnable() {
            public void run() {
                execute(event);
            }
        });
    }

    @RunWithinContext
    public void execute(ClientDataChangedEvent event) {
        for (final Mapping mapping : event.getRegister().mappings()) {
            storeDataChange(event, mapping);
        }
    }

    public void storeDataChange(ClientDataChangedEvent event, Mapping mapping) {
        final Device device = event.getDevice();
        final Gateway gateway = event.getGateway();
        final Register register = event.getRegister();

        DateTime date = new DateTime(event.getTime().getLocalCalendar().getTime());
        if (date.isAfter(DateTime.now())) {
            date = DateTime.now();
        }

        final PlatformRepresentationFactory representationFactory = findRepresentationFactory(mapping);
        final Optional representationOptional = representationFactory.apply(date, event.getGateway(), device, register, mapping, event.getValue());
        if (representationOptional.isPresent()) {
            final Object representation = representationOptional.get();
            final PlatformRepresentationRepository repository = findRepresentationRepository(representation);
            final Optional saved = repository.apply(gateway, representation);
            if (saved.isPresent()) {
//                eventPublisher.publishEvent(new ManagedObjectUpdatedEvent(saved.get()));
            } else {
                eventPublisher.publishEvent(new DeviceRemovedEvent(gateway, device));
            }
        }
    }

    private PlatformRepresentationFactory findRepresentationFactory(Mapping mapping) {
        return findBeanByGenericType(applicationContext, PlatformRepresentationFactory.class, mapping.getClass());
    }

    private PlatformRepresentationRepository findRepresentationRepository(Object representation) {
        return findBeanByGenericType(applicationContext, PlatformRepresentationRepository.class, representation.getClass());
    }
}
