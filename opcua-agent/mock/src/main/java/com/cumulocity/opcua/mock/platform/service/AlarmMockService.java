package com.cumulocity.opcua.mock.platform.service;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.platform.repository.configuration.PlatformProperties;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlarmMockService {

    public static final String ALARM_PATH = "/alarm/alarms";

    private final PlatformProperties platformProperties;

    private Map<GId, AlarmRepresentation> store = Maps.newHashMap();

    public AlarmRepresentation save(AlarmRepresentation alarm) {
        final int id = RandomUtils.nextInt(1, 10000);
        alarm.setId(GId.asGId(id));
        alarm.setSelf(platformProperties.getUrl() + ALARM_PATH + "/" + alarm.getId().getValue());

        this.store.put(alarm.getId(), alarm);
        return alarm;
    }

    public AlarmRepresentation update(GId id, AlarmRepresentation object) {
        this.store.put(id, object);
        return object;
    }
}
