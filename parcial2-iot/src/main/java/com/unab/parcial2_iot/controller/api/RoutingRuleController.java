package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.models.RoutingRule;
import com.unab.parcial2_iot.repositories.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleRepository repo;

    @GetMapping
    public List<RoutingRule> list() { return repo.findAll(); }

    @PostMapping
    public RoutingRule create(@RequestBody RoutingRule in) {
        if (in.getNombre() == null || in.getNombre().isBlank()) in.setNombre("rule");
        if (in.getMatch() == null) in.setMatch(new java.util.HashMap<>());
        if (in.getConfig() == null) in.setConfig(new java.util.HashMap<>());
        in.setCreadoEn(OffsetDateTime.now());
        return repo.save(in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable java.util.UUID id) {
        repo.deleteById(id);
    }
}
