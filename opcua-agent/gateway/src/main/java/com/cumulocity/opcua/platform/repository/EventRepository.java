package com.cumulocity.opcua.platform.repository;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.repository.core.PlatformRepresentationRepository;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.sdk.client.event.EventApi;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleException;
import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleSuccess;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventRepository implements PlatformRepresentationRepository<EventRepresentation> {

    private final EventApi eventApi;

    @RunWithinContext
    public Optional<EventRepresentation> apply(final Credentials gateway, final EventRepresentation eventRepresentation) {
        try {
            return handleSuccess( eventApi.create(eventRepresentation));
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }
}
