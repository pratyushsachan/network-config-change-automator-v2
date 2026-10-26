package com.netchange.web;

import com.netchange.domain.Device;
import com.netchange.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Device register(@Valid @RequestBody RegisterDeviceRequest req) {
        return service.register(new Device(req.name(), req.ipAddress(), req.model(), req.osVersion(),
                req.site(), req.deviceGroup(), req.runningConfig(), req.faultMode()));
    }

    @GetMapping
    public List<Device> list(@RequestParam(required = false) String group) {
        return service.list(group);
    }

    @GetMapping("/{id}")
    public Device get(@PathVariable Long id) {
        return service.get(id);
    }
}
