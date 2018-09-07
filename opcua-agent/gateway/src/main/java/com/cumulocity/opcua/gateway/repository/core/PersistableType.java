package com.cumulocity.opcua.gateway.repository.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface PersistableType {
    /**
     * Unique type name used in serialization/deserialization process.
     */
    String value();

    String discriminator() default "";

    /**
     * Indicates whether type has context (transient fields which should be initialized when retrieving it from repository).
     * <p>The default is {@code false}.
     */
    boolean autowire() default false;

    boolean inMemory() default false;

    Class<?> runWithinContext() default Object.class;
}
