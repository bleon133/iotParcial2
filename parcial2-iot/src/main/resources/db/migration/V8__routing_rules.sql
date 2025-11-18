-- Reglas de enrutamiento de telemetría a sinks (webhook, file, etc.)

CREATE TABLE IF NOT EXISTS iot.routing_rule (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      text NOT NULL,
    enabled     boolean NOT NULL DEFAULT true,
    sink        text NOT NULL,
    match       jsonb NOT NULL DEFAULT '{}'::jsonb, -- criterios de coincidencia (dispositivoId, variableId, tipoDato)
    config      jsonb NOT NULL DEFAULT '{}'::jsonb, -- configuración específica del sink (p.ej., url, headers)
    creado_en   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_routing_rule_enabled ON iot.routing_rule(enabled);

