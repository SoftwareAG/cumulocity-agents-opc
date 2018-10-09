package com.cumulocity.opcua.platform.repository;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.repository.core.PlatformRepresentationRepository;
import com.cumulocity.opcua.platform.model.Command;
import com.cumulocity.opcua.platform.service.CommandService;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleException;
import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleSuccess;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MeasurementRepository implements PlatformRepresentationRepository<MeasurementRepresentation> {

    private final CommandService commandService;

    @RunWithinContext
    public Optional<MeasurementRepresentation> apply(final Credentials gateway, final MeasurementRepresentation measurementRepresentation) {
        try {
            commandService.queueForExecution(gateway, new Command(measurementRepresentation));
            return handleSuccess(measurementRepresentation);
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }
}
