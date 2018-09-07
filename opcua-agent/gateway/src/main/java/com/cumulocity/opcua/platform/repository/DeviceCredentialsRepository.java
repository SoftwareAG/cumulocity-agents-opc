package com.cumulocity.opcua.platform.repository;

import com.cumulocity.rest.representation.devicebootstrap.DeviceCredentialsRepresentation;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.devicecontrol.DeviceCredentialsApi;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleException;
import static com.cumulocity.opcua.platform.repository.common.PlatformRepositoryUtils.handleSuccess;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeviceCredentialsRepository {

    private final Platform bootstrapPlatform;

    public Optional<DeviceCredentialsRepresentation> get(String identifier) {
        try {
            final DeviceCredentialsApi deviceCredentialsApi = bootstrapPlatform.getDeviceCredentialsApi();
            return handleSuccess(deviceCredentialsApi.pollCredentials(identifier));
        } catch (final Exception ex) {
            return handleException(ex);
        }
    }
}
