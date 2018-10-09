package com.cumulocity.opcua.platform.repository;

import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.repository.core.PlatformRepresentationRepository;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.sdk.client.alarm.AlarmApi;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleException;
import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleSuccess;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlarmRepository implements PlatformRepresentationRepository<AlarmRepresentation> {

    private final AlarmApi alarmApi;

    @RunWithinContext
    public Optional<AlarmRepresentation> apply(final Credentials gateway, final AlarmRepresentation alarmRepresentation) {
        return create(gateway, alarmRepresentation);
    }

    public Optional<AlarmRepresentation> create(final Credentials gateway, final AlarmRepresentation alarmRepresentation) {
        try {
            return handleSuccess(alarmApi.create(alarmRepresentation));
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }

    @RunWithinContext
    public Optional<AlarmRepresentation> update(Credentials gateway, final AlarmRepresentation alarmRepresentation) {
        try {
            return handleSuccess(alarmApi.update(alarmRepresentation));
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }

    @RunWithinContext
    public Optional<AlarmRepresentation> clear(Credentials gateway, final AlarmRepresentation alarmRepresentation) {
        try {
            alarmRepresentation.setStatus("CLEARED");
            return handleSuccess(alarmApi.update(alarmRepresentation));
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }
}
