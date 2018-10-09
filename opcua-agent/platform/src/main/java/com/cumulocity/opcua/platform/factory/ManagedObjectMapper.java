package com.cumulocity.opcua.platform.factory;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.platform.model.annotation.ExtensibleRepresentationView;
import com.cumulocity.rest.representation.AbstractExtensibleRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.operation.OperationRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Optional.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagedObjectMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public <T> Optional<T> convert(Class<T> clazz, AbstractExtensibleRepresentation representation) {
        final ExtensibleRepresentationView annotation = clazz.getAnnotation(ExtensibleRepresentationView.class);
        if (annotation != null) {
            final String fragmentType = annotation.fragment();
            final Object fragment = representation.get(fragmentType);
            if (fragment != null) {
                final T result = objectMapper.convertValue(fragment, clazz);
                if (representation instanceof ManagedObjectRepresentation) {
                    final Optional<Method> setName = findMethod(clazz, "setName", String.class);
                    if (setName.isPresent()) {
                        setName.get().invoke(result, ((ManagedObjectRepresentation) representation).getName());
                    }
                    final Optional<Method> setId = findMethod(clazz, "setId", GId.class);
                    if (setId.isPresent()) {
                        setId.get().invoke(result, ((ManagedObjectRepresentation) representation).getId());
                    }
                }
                if (representation instanceof OperationRepresentation) {
                    final Optional<Method> setId = findMethod(clazz, "setId", GId.class);
                    if (setId.isPresent()) {
                        setId.get().invoke(result, ((OperationRepresentation) representation).getId());
                    }
                }
                return of(result);
            }
        }
        return absent();
    }

    private static Optional<Method> findMethod(Class clazz, String methodName, Class... paramType) {
        try {
            return fromNullable(clazz.getDeclaredMethod(methodName, paramType));
        } catch (final NoSuchMethodException ex) {
            return absent();
        }
    }
}
