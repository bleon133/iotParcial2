# Tokens para Dispositivos (SAS Simplificado)

Para uso educativo, se propone un token SAS simple con clave simétrica por dispositivo.

## Esquema de BD
Tabla `iot.device_credential` (V6):
```
id              uuid PK
dispositivo_id  uuid UNIQUE
tipo            text  ('symmetric'|'x509')
clave_simetrica text  (base64)
huella_x509     text
activo          boolean
creado_en       timestamptz
```

## Formato del Token SAS
```
SharedAccessSignature sr={idExterno}&se={epochSeconds}&sig={base64(hmacSha256(idExterno + "\n" + se, clave))}
```
Ejemplo header HTTP:
```
Authorization: SharedAccessSignature sr=ESP32-001&se=1737072000&sig=AbCdEf...
```

## Validación (opcional)
- Toggle: `app.security.devices.required=false` (por defecto, sin validar).
- Si `true`:
  1) Obtener `idExterno` (del body o topic/path).
  2) Cargar `clave_simetrica` del dispositivo.
  3) Validar `se` (no expirado) y `sig` (HMAC SHA256).

## Generación de Token (pseudocódigo)
```
id = "ESP32-001"
se = nowEpoch + 3600
sig = base64(hmacSha256(id + "\n" + se, base64Decode(clave)))
token = "SharedAccessSignature sr="+id+"&se="+se+"&sig="+sig
```

Notas:
- Para MQTT, se puede enviar el token en username/password o en un campo del payload inicial.
- Este mecanismo es educativo; para producción, usar atestación X.509 o flujos DPS completos.
