package com.cumulocity.opcua.platform.notification.model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManagedObjectListener {
    public void onUpdate(Object value) {

    }

    public void onDelete() {

    }

    public void onError(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
    }
}
