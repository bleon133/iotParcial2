# Roadmap (sin login de usuario)

Este roadmap guía la evolución del proyecto hacia una plataforma tipo Azure IoT, manteniendo la UI/API sin autenticación de usuario (educativo) y agregando seguridad opcional a nivel dispositivo.

## Fase 1 — Gemelo digital + Tokens por dispositivo
- Device Twin (tabla `iot.device_twin`): desired/reported JSONB, `version`, `updated_at`.
- API Twin:
  - `GET /api/twin/{dispositivoId}` → twin completo.
  - `PATCH /api/twin/{dispositivoId}/desired` → merge parcial de desired.
  - `POST /api/twin/{dispositivoId}/reported` → merge parcial de reported.
- MQTT Twin:
  - Reported (device→cloud): `devices/{idExterno}/twin/reported`.
  - Desired (cloud→device): `devices/{idExterno}/twin/desired`.
- Credenciales de dispositivo (tabla `iot.device_credential`) y tokens SAS simples.
- Propiedades:
  - `app.security.devices.required=false` (por defecto). Si `true`, exigir SAS/JWT en HTTP/MQTT.

## Fase 2 — Retención/Particionado y agregados
- Timeseries: TimescaleDB (recomendado) o particionado nativo por fecha en `iot.telemetria`.
- Retención: política de 90 días y downsampling (agregados por hora/día).
- Índices para consultas por `dispositivo_id`, `variable_plantilla_id`, rango `ts`.

## Fase 3 — Routing y Notificaciones
- Reglas de enrutamiento por dispositivo/variable/tipo a múltiples sinks (Postgres, Webhook, Archivo).
- Notificaciones de alertas: Slack/Email/Webhook (por severidad/reglas).
- SSE para gráficos en vivo en paneles (ya hay SSE; exponer suscripciones en UI de paneles).

## Entregables por fase
- F1: migración V6 (lista), `TwinService`, `TwinController`, handler MQTT reported, props de seguridad y validación SAS opcional.
- F2: migraciones de particionado/retención, jobs de rollups, endpoints para series agregadas.
- F3: entidades de routing, `TelemetrySink` + `WebhookSink`, UI CRUD, conectores de notificación.

## Consideraciones
- Mantener simple: sin login de usuario, pero con seguridad opcional por dispositivo.
- Documentación DX: OpenAPI/Swagger, ejemplos de payloads, guías de twin y tokens.
