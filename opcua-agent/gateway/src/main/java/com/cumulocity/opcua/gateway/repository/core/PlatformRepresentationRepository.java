package com.cumulocity.opcua.gateway.repository.core;

import com.cumulocity.opcua.gateway.model.core.Credentials;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.google.common.base.Optional;
import lombok.NonNull;

public interface PlatformRepresentationRepository<E> {
    Optional<E> apply(@NonNull Credentials gateway, @NonNull E value);
}
