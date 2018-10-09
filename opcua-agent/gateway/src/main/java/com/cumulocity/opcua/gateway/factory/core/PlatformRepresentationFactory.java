package com.cumulocity.opcua.gateway.factory.core;

import com.cumulocity.opcua.gateway.model.device.Device;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.model.type.Register;
import com.cumulocity.opcua.gateway.model.type.core.Mapping;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

public interface PlatformRepresentationFactory<M extends Mapping, R> {
    Optional<R> apply(DateTime date, Gateway gateway, Device device, Register register, M var1, Object value);
}