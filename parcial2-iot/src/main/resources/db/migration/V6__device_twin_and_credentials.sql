-- Device Twin y Credenciales de Dispositivo (educativo)
-- Esquema: iot

-- Tabla de gemelo digital (desired/reported) por dispositivo
CREATE TABLE IF NOT EXISTS iot.device_twin (
    dispositivo_id uuid PRIMARY KEY REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    desired        jsonb NOT NULL DEFAULT '{}'::jsonb,
    reported       jsonb NOT NULL DEFAULT '{}'::jsonb,
    version        integer NOT NULL DEFAULT 0,
    updated_at     timestamptz NOT NULL DEFAULT now()
);

-- Crear o reemplazar la función de trigger (idempotente)
CREATE OR REPLACE FUNCTION iot.fn_twin_bu() RETURNS trigger AS $$
BEGIN
    IF ROW(NEW.desired, NEW.reported) IS DISTINCT FROM ROW(OLD.desired, OLD.reported) THEN
        NEW.version := COALESCE(OLD.version, 0) + 1;
        NEW.updated_at := now();
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Asegurar trigger (drop si existe y recrear)
DROP TRIGGER IF EXISTS tg_twin_bu ON iot.device_twin;
CREATE TRIGGER tg_twin_bu
BEFORE UPDATE ON iot.device_twin
FOR EACH ROW EXECUTE FUNCTION iot.fn_twin_bu();

-- Tabla de credenciales por dispositivo (para SAS/X.509 simplificado)
CREATE TABLE IF NOT EXISTS iot.device_credential (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    dispositivo_id  uuid NOT NULL UNIQUE REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    tipo            text NOT NULL CHECK (tipo IN ('symmetric','x509')),
    clave_simetrica text,
    huella_x509     text,
    activo          boolean NOT NULL DEFAULT true,
    creado_en       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_cred_payload CHECK (
        (tipo = 'symmetric' AND clave_simetrica IS NOT NULL) OR
        (tipo = 'x509' AND huella_x509 IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_device_credential_activo ON iot.device_credential (activo);
