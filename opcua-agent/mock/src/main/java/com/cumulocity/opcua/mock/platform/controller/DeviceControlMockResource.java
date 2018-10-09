package com.cumulocity.opcua.mock.platform.controller;

import com.cumulocity.opcua.platform.repository.configuration.PlatformProperties;
import com.cumulocity.opcua.mock.platform.service.DeviceControlService;
import com.cumulocity.rest.representation.devicebootstrap.DeviceCredentialsRepresentation;
import com.cumulocity.rest.representation.devicebootstrap.NewDeviceRequestRepresentation;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Lazy
@RestController
@RequestMapping(DeviceControlMockResource.PATH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeviceControlMockResource {

    public static final String PATH = "/devicecontrol";

    private final DeviceControlService deviceControlService;
    private final PlatformProperties platformProperties;

    @RequestMapping(method = POST, value = "/deviceCredentials")
    public ResponseEntity<?> pollCredentials(@RequestBody final DeviceCredentialsRepresentation deviceId) throws URISyntaxException {
        Optional<DeviceCredentialsRepresentation> representation = deviceControlService.pollCredentials(deviceId.getId());
        if (representation.isPresent()) {
            representation.get().setSelf(platformProperties.getUrl() + DeviceControlMockResource.PATH);
            return ResponseEntity.created(new URI(representation.get().getSelf())).body(representation.get());
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(method = POST, value = "/newDeviceRequests")
    public ResponseEntity<NewDeviceRequestRepresentation> registerDevice(String id) throws URISyntaxException {
        NewDeviceRequestRepresentation representation = new NewDeviceRequestRepresentation();
        representation.setId(id);
        representation.setSelf(platformProperties.getUrl() + DeviceControlMockResource.PATH);
        return ResponseEntity.created(new URI(representation.getSelf())).body(representation);
    }

}

