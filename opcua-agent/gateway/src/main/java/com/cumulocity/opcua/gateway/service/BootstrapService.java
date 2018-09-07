package com.cumulocity.opcua.gateway.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.factory.GatewayFactory;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.gateway.GatewayAddedEvent;
import com.cumulocity.opcua.gateway.model.gateway.GatewayRemovedEvent;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.cumulocity.opcua.gateway.service.configuration.GatewayConfigurationProperties;
import com.cumulocity.opcua.gateway.service.utils.Scheduler;
import com.cumulocity.opcua.platform.factory.IdentityFactory;
import com.cumulocity.opcua.platform.factory.ManagedObjectFactory;
import com.cumulocity.opcua.platform.repository.DeviceCredentialsRepository;
import com.cumulocity.opcua.platform.repository.IdentityRepository;
import com.cumulocity.opcua.platform.repository.ManagedObjectRepository;
import com.cumulocity.rest.representation.devicebootstrap.DeviceCredentialsRepresentation;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BootstrapService {

    private final ApplicationEventPublisher eventPublisher;
    private final DeviceCredentialsRepository deviceCredentialsRepository;
    private final Repository<Gateway> gatewayRepository;
    private final GatewayFactory gatewayFactory;
    private final IdentityRepository identityRepository;
    private final IdentityFactory identityFactory;
    private final ManagedObjectRepository inventoryRepository;
    private final ManagedObjectFactory managedObjectFactory;
    private final GatewayConfigurationProperties properties;
    private final Scheduler scheduler;

    private List<GId> initialized = new ArrayList<>();

    @PostConstruct
    public void init() {
        scheduler.scheduleOnce(new Runnable() {
            public void run() {
                scheduler.scheduleWithFixedDelay(new Runnable() {
                    public void run() {
                        refreshGateways();
                    }
                });
            }
        });
    }

    @Async
    public synchronized void refreshGateways() {
        doRefreshGateways();
        doCreateGateway();
    }

    private void doCreateGateway() {
//        todo wojtek add test
        final Collection<Gateway> all = getAllGateways();
        if (all.isEmpty()) {
            final String identifier = properties.getIdentifier();
            final Optional<DeviceCredentialsRepresentation> gatewayCredentialsOptional = deviceCredentialsRepository.get(identifier);
            if (gatewayCredentialsOptional.isPresent()) {
                final DeviceCredentialsRepresentation credentials = gatewayCredentialsOptional.get();

//                todo wojtek add test
                final Optional<ExternalIDRepresentation> existingExternalIdOptional = identityRepository.get(credentials, identityFactory.createID(properties.getIdentifier()));
                if (existingExternalIdOptional.isPresent()) {
                    final Optional<ManagedObjectRepresentation> managedObjectOptional = inventoryRepository.get(credentials, existingExternalIdOptional.get().getManagedObject().getId());
                    create(credentials, managedObjectOptional, existingExternalIdOptional);
                } else {
                    final Optional<ManagedObjectRepresentation> managedObjectOptional = inventoryRepository.save(credentials, managedObjectFactory.create(identifier));
                    create(credentials, managedObjectOptional, existingExternalIdOptional);
                }
            }
        }
    }

    private void create(DeviceCredentialsRepresentation credentials, Optional<ManagedObjectRepresentation> managedObjectOptional, Optional<ExternalIDRepresentation> existingExternalIdOptional) {
        if (managedObjectOptional.isPresent()) {
            final ManagedObjectRepresentation managedObject = managedObjectOptional.get();
            final Optional<Gateway> newGatewayOptional = gatewayFactory.create(credentials, managedObject);
            if (newGatewayOptional.isPresent()) {
                final Gateway gateway = newGatewayOptional.get();
                final ExternalIDRepresentation externalId = identityFactory.create(properties.getIdentifier(), managedObject);
                if (!existingExternalIdOptional.isPresent()) {
                    identityRepository.save(gateway, externalId);
                }
                gatewayRepository.save(gateway);
                eventPublisher.publishEvent(new GatewayAddedEvent(gateway));
                initialized.add(gateway.getId());
            }
        }
    }

    private void doRefreshGateways() {
        for (final Gateway gateway : getAllGateways()) {
            final Optional<ManagedObjectRepresentation> managedObjectOptional = inventoryRepository.get(gateway);
            if (managedObjectOptional.isPresent()) {
                final ManagedObjectRepresentation managedObject = managedObjectOptional.get();
                final Optional<Gateway> newGatewayOptional = gatewayFactory.create(gateway, managedObject);
                if (newGatewayOptional.isPresent()) {
                    final Gateway newGateway = newGatewayOptional.get();
                    if (!gateway.equals(newGateway)) {
                        gatewayRepository.save(newGateway);
                    }
                    if (!initialized.contains(newGateway.getId())) {
                        eventPublisher.publishEvent(new GatewayAddedEvent(newGateway));
                        initialized.add(newGateway.getId());
                    }

                    final Optional<ExternalIDRepresentation> externalIdOptional = identityRepository.get(newGateway, identityFactory.createID(properties.getIdentifier()));
                    if (!externalIdOptional.isPresent()) {
                        doRemoveGateway(newGateway);
                    }
                } else {
                    doRemoveGateway(gateway);
                }
            } else {
                doRemoveGateway(gateway);
            }
        }
    }

//    todo wojtek test many identifiers
    private Collection<Gateway> getAllGateways() {
        return FluentIterable.from(gatewayRepository.findAll()).filter(new Predicate<Gateway>() {
            public boolean apply(Gateway gateway) {
                return ("device_" + properties.getIdentifier()).equalsIgnoreCase(gateway.getName());
            }
        }).toList();
    }

    private void doRemoveGateway(Gateway gateway) {
        initialized.remove(gateway.getId());
        final int numberOfRetries = gateway.increaseNumberOfRetries();
        log.warn("Gateway is not responding {}/{} ({})", gateway.getTenant(), gateway.getName(), numberOfRetries);
        if (numberOfRetries < 10) {
            gatewayRepository.save(gateway);
        } else {
            log.warn("Removing  gateway {}/{}", gateway.getTenant(), gateway.getName());
            gatewayRepository.delete(gateway.getId());
            eventPublisher.publishEvent(new GatewayRemovedEvent(gateway));
        }
    }
}
