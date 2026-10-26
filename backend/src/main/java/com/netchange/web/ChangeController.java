package com.netchange.web;

import com.netchange.domain.AuditEvent;
import com.netchange.domain.ChangeRequest;
import com.netchange.service.AuditService;
import com.netchange.service.ChangeService;
import com.netchange.service.DryRunResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The acting user is passed in the X-User header (a stand-in for real authentication). */
@RestController
@RequestMapping("/api/changes")
public class ChangeController {

    private final ChangeService service;
    private final AuditService audit;

    public ChangeController(ChangeService service, AuditService audit) {
        this.service = service;
        this.audit = audit;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeRequest create(@RequestHeader("X-User") String user, @Valid @RequestBody CreateChangeRequest req) {
        return service.create(req.title(), req.reason(), req.targetGroup(), req.configLines(),
                req.minOsVersion(), req.batchSize(), user);
    }

    @GetMapping
    public List<ChangeRequest> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ChangeDetails get(@PathVariable Long id) {
        return new ChangeDetails(service.get(id), service.executionsFor(id));
    }

    @PostMapping("/{id}/approve")
    public ChangeRequest approve(@PathVariable Long id, @RequestHeader("X-User") String user) {
        return service.approve(id, user);
    }

    @PostMapping("/{id}/reject")
    public ChangeRequest reject(@PathVariable Long id, @RequestHeader("X-User") String user,
                                @RequestParam(required = false) String reason) {
        return service.reject(id, user, reason);
    }

    @GetMapping("/{id}/dry-run")
    public DryRunResult dryRun(@PathVariable Long id) {
        return service.dryRun(id);
    }

    @PostMapping("/{id}/deploy")
    public ChangeDetails deploy(@PathVariable Long id, @RequestHeader("X-User") String user) {
        ChangeRequest change = service.deploy(id, user);
        return new ChangeDetails(change, service.executionsFor(id));
    }

    @PostMapping("/{id}/rollback")
    public ChangeDetails rollback(@PathVariable Long id, @RequestHeader("X-User") String user) {
        ChangeRequest change = service.rollback(id, user);
        return new ChangeDetails(change, service.executionsFor(id));
    }

    @GetMapping("/{id}/audit")
    public List<AuditEvent> auditTrail(@PathVariable Long id) {
        service.get(id);
        return audit.forChange(id);
    }
}
