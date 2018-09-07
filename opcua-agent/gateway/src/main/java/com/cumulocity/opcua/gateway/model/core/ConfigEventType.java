package com.cumulocity.opcua.gateway.model.core;

import com.cumulocity.opcua.gateway.model.type.core.BrowsePath;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
@ToString
public class ConfigEventType<T> {
    public static final ConfigEventType URL = new ConfigEventType("Connection url not set");

    public static final ConfigEventType<BrowsePath> BROWSE_PATH = new ConfigEventType<BrowsePath>("Browse path not found") {
        public String formatMessage(BrowsePath browsePath) {
            if (browsePath == null) {
                return super.formatMessage(browsePath);
            }
            return super.formatMessage(browsePath) + " " + browsePath.asString();
        }
    };
    public static final ConfigEventType NO_REGISTERS = new ConfigEventType("No registers defined");

    @Getter(onMethod = @__(@JsonValue))
    private String value;

    @JsonCreator
    public ConfigEventType(Object value) {
        try {
            this.value = value.toString();
        } catch (final Exception ex) {
            log.error(ex.getMessage());
            this.value = ex.getMessage() == null ? "Error" : ex.getMessage();
        }
    }

    public String formatMessage(@Nullable T browsePath) {
        return getValue();
    }

    public static String getMessage(ConfigEventType type) {
        return getMessage(type, null);
    }

    public static <T> String getMessage(ConfigEventType<T> type, final T browsePath) {
        return Optional.fromNullable(type).transform(new Function<ConfigEventType, String>() {
            public String apply(ConfigEventType type) {
                return type.formatMessage(browsePath);
            }
        }).orNull();
    }
}
