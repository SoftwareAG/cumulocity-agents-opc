package com.cumulocity.opcua.gateway.factory;

import com.cumulocity.opcua.gateway.model.operation.Operation;
import com.cumulocity.opcua.platform.factory.ManagedObjectMapper;
import com.cumulocity.rest.representation.AbstractExtensibleRepresentation;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OperationFactory {
    private final ManagedObjectMapper managedObjectMapper;

    public Optional<Operation> create(AbstractExtensibleRepresentation managedObject) {
        return managedObjectMapper.convert(Operation.class, managedObject);
    }
}
