package com.cumulocity.opcua.gateway.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * context object is taken from first parameter which should be of type Gateway, or HasGateway.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface RunWithinContext {
}
