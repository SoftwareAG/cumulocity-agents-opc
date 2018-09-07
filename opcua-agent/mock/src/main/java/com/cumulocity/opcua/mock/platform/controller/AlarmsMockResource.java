package com.cumulocity.opcua.mock.platform.controller;

import com.cumulocity.opcua.mock.platform.service.AlarmMockService;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.cumulocity.model.idtype.GId.asGId;
import static com.cumulocity.opcua.mock.platform.service.AlarmMockService.ALARM_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Lazy
@RestController
@RequestMapping(value = ALARM_PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlarmsMockResource {

    private final AlarmMockService service;

    @SneakyThrows
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity store(@RequestBody AlarmRepresentation object) {
        final AlarmRepresentation stored = service.save(object);
        final ResponseEntity<AlarmRepresentation> result = ResponseEntity.created(new URI(stored.getSelf())).body(stored);
        return result;
    }

    @RequestMapping(method = PUT, value = "/{id}")
    public ResponseEntity update(@PathVariable("id") String id, @RequestBody AlarmRepresentation object) {
        final AlarmRepresentation updated = service.update(asGId(id), object);
        return ResponseEntity.ok(updated);
    }
}
