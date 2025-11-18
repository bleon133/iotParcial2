package com.unab.parcial2_iot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.models.RoutingRule;
import com.unab.parcial2_iot.models.Telemetria;
import com.unab.parcial2_iot.repositories.RoutingRuleRepository;
import com.unab.parcial2_iot.services.TelemetryRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryRouterImpl implements TelemetryRouter {

    private final RoutingRuleRepository ruleRepo;
    private final ObjectMapper objectMapper;
    private final RestClient rest = RestClient.create();

    @Override
    @Transactional(readOnly = true)
    public void route(Telemetria t) {
        List<RoutingRule> rules = ruleRepo.findByEnabledTrue();
        if (rules.isEmpty() || t == null) return;

        UUID dispId = t.getDispositivo() != null ? t.getDispositivo().getId() : null;
        UUID varId = t.getVariable() != null ? t.getVariable().getId() : null;
        String tipo = t.getValorNumero()!=null?"numero": t.getValorBooleano()!=null?"booleano": t.getValorTexto()!=null?"cadena": t.getValorJson()!=null?"json": null;

        Map<String,Object> payload = Map.of(
                "dispositivoId", dispId,
                "variableId", varId,
                "ts", t.getTs(),
                "numero", t.getValorNumero(),
                "booleano", t.getValorBooleano(),
                "texto", t.getValorTexto(),
                "json", t.getValorJson(),
                "etiquetas", t.getEtiquetas()
        );

        for (RoutingRule r : rules) {
            try {
                if (!matches(r.getMatch(), dispId, varId, tipo)) continue;
                if ("webhook".equalsIgnoreCase(r.getSink())) {
                    sendWebhook(r.getConfig(), payload);
                }
            } catch (Exception e) {
                log.warn("Routing rule failed: id={} error={}", r.getId(), e.toString());
            }
        }
    }

    private boolean matches(Map<String,Object> match, UUID dispId, UUID varId, String tipo) {
        if (match == null || match.isEmpty()) return true;
        Object mDisp = match.get("dispositivoId");
        if (mDisp != null && dispId != null && !dispId.toString().equalsIgnoreCase(String.valueOf(mDisp))) return false;
        Object mVar = match.get("variableId");
        if (mVar != null && varId != null && !varId.toString().equalsIgnoreCase(String.valueOf(mVar))) return false;
        Object mTipo = match.get("tipoDato");
        if (mTipo != null && tipo != null && !tipo.equalsIgnoreCase(String.valueOf(mTipo))) return false;
        return true;
    }

    private void sendWebhook(Map<String,Object> config, Map<String,Object> payload) {
        if (config == null) return;
        Object url = config.get("url");
        if (url == null || String.valueOf(url).isBlank()) return;
        RestClient.RequestBodySpec req = rest.post().uri(String.valueOf(url)).contentType(MediaType.APPLICATION_JSON);
        try {
            Object headers = config.get("headers");
            if (headers instanceof Map<?,?> hmap) {
                for (Map.Entry<?,?> e : hmap.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) req = req.header(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }
        } catch (Exception ignored) {}
        req.body(payload).retrieve().toBodilessEntity();
    }
}

