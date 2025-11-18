# Gemelo Digital (Device Twin)

El gemelo digital mantiene dos documentos JSON por dispositivo:
- desired: configuración deseada por la nube (por ejemplo, `samplingMs`, `thresholdTemp`).
- reported: estado reportado por el dispositivo (por ejemplo, `fwVersion`, `wifi.rssi`).

## Esquema de BD
Tabla `iot.device_twin` (creada en V6):
```
dispositivo_id uuid PK
desired        jsonb NOT NULL DEFAULT '{}'
reported       jsonb NOT NULL DEFAULT '{}'
version        int   NOT NULL DEFAULT 0
updated_at     timestamptz NOT NULL DEFAULT now()
```
Un trigger incrementa `version` y actualiza `updated_at` cuando cambian desired/reported.

## MQTT Topics
- Reported (device → cloud): `devices/{idExterno}/twin/reported`
  - Body (merge parcial): `{ "fwVersion":"1.2.3", "wifi": {"rssi": -61} }`
- Desired (cloud → device): `devices/{idExterno}/twin/desired`
  - Body (full o parcial): `{ "thresholdTemp": 28, "samplingMs": 5000 }`

## API (a implementar)
- `GET /api/twin/{dispositivoId}` → devuelve `{ desired, reported, version, updatedAt }`.
- `PATCH /api/twin/{dispositivoId}/desired` → body JSON; merge parcial de desired.
- `POST /api/twin/{dispositivoId}/reported` → body JSON; merge parcial de reported.

Notas:
- Para merges JSONB, usar `jsonb_deep_merge` (si disponible) o merge en aplicación (ObjectMapper) y persistir.
- Notificar cambios desired por MQTT a `devices/{idExterno}/twin/desired`.

## Ejemplos
Reported (ESP32 → nube):
```
Topic: devices/ESP32-001/twin/reported
Body:  { "fwVersion": "1.2.3", "wifi": {"rssi": -61} }
```

Desired (nube → ESP32):
```
Topic: devices/ESP32-001/twin/desired
Body:  { "thresholdTemp": 28, "samplingMs": 5000 }
```
