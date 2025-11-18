package com.unab.parcial2_iot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.models.RoutingRule;
import com.unab.parcial2_iot.repositories.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/routing")
public class RoutingRulePageController {

    private final RoutingRuleRepository repo;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("nav", "routing");
        model.addAttribute("rules", repo.findAll());
        model.addAttribute("error", null);
        return "routing/listar";
    }

    @PostMapping
    public String create(@RequestParam String nombre,
                         @RequestParam(defaultValue = "true") boolean enabled,
                         @RequestParam String sink,
                         @RequestParam(defaultValue = "{}") String matchJson,
                         @RequestParam(defaultValue = "{}") String configJson,
                         Model model) {
        try {
            Map<String,Object> match = objectMapper.readValue(matchJson, Map.class);
            Map<String,Object> config = objectMapper.readValue(configJson, Map.class);
            RoutingRule r = RoutingRule.builder()
                    .nombre((nombre==null||nombre.isBlank())?"rule":nombre.trim())
                    .enabled(enabled)
                    .sink(sink)
                    .match(match==null?new HashMap<>():match)
                    .config(config==null?new HashMap<>():config)
                    .creadoEn(OffsetDateTime.now())
                    .build();
            repo.save(r);
            return "redirect:/routing";
        } catch (Exception e) {
            model.addAttribute("nav", "routing");
            model.addAttribute("rules", repo.findAll());
            model.addAttribute("error", "JSON inválido en match/config");
            return "routing/listar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return "redirect:/routing";
    }
}

