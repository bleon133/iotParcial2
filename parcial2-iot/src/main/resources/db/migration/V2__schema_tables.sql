-- Extensión para gen_random_uuid (si no existe)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tipos ENUM (idempotentes)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'tipo_dato' AND n.nspname = 'iot'
    ) THEN
        CREATE TYPE iot.tipo_dato AS ENUM ('numero','booleano','cadena','json');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'tipo_dispositivo' AND n.nspname = 'iot'
    ) THEN
        CREATE TYPE iot.tipo_dispositivo AS ENUM ('real','gemelo_digital','api','conjunto_datos');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'protocolo' AND n.nspname = 'iot'
    ) THEN
        CREATE TYPE iot.protocolo AS ENUM ('mqtt','http','kafka','amqp');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'estado_comando' AND n.nspname = 'iot'
    ) THEN
        CREATE TYPE iot.estado_comando AS ENUM ('ENVIADO','ACK','ERROR','TIEMPO_AGOTADO','CANCELADO');
    END IF;
END$$;

-- Tablas base
CREATE TABLE IF NOT EXISTS iot.plantilla (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                   text NOT NULL UNIQUE,
    descripcion              text,
    protocolo_predeterminado iot.protocolo NOT NULL,
    creado_en                timestamptz NOT NULL
);

CREATE TABLE IF NOT EXISTS iot.variable_plantilla (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    plantilla_id    uuid NOT NULL REFERENCES iot.plantilla(id) ON DELETE CASCADE,
    nombre          text NOT NULL,
    etiqueta        text,
    tipo_dato       iot.tipo_dato NOT NULL,
    unidad          text,
    escribible      boolean NOT NULL DEFAULT false,
    minimo          numeric,
    maximo          numeric,
    precision       integer,
    muestreo_ms     integer,
    CONSTRAINT uq_variable_nombre UNIQUE (plantilla_id, nombre)
);

CREATE TABLE IF NOT EXISTS iot.dispositivo (
    id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    plantilla_id           uuid NOT NULL REFERENCES iot.plantilla(id) ON DELETE RESTRICT,
    id_externo             text NOT NULL UNIQUE,
    nombre                 text NOT NULL,
    tipo                   iot.tipo_dispositivo NOT NULL,
    estado                 text,
    protocolo              iot.protocolo NOT NULL,
    topico_mqtt_telemetria text,
    topico_mqtt_comando    text,
    latitud                double precision,
    longitud               double precision,
    etiquetas              jsonb,
    creado_en              timestamptz NOT NULL
);

CREATE TABLE IF NOT EXISTS iot.telemetria (
    id                    bigserial PRIMARY KEY,
    dispositivo_id        uuid NOT NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    variable_plantilla_id uuid NOT NULL REFERENCES iot.variable_plantilla(id) ON DELETE CASCADE,
    ts                    timestamptz NOT NULL,
    valor_numero          double precision,
    valor_booleano        boolean,
    valor_texto           text,
    valor_json            jsonb,
    etiquetas             jsonb
);

CREATE INDEX IF NOT EXISTS idx_tel_dev_var_ts
    ON iot.telemetria (dispositivo_id, variable_plantilla_id, ts DESC);

CREATE TABLE IF NOT EXISTS iot.estado_dispositivo (
    dispositivo_id        uuid NOT NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    variable_plantilla_id uuid NOT NULL REFERENCES iot.variable_plantilla(id) ON DELETE CASCADE,
    ultimo_ts             timestamptz NOT NULL,
    ultimo_numero         double precision,
    ultimo_booleano       boolean,
    ultimo_texto          text,
    ultimo_json           jsonb,
    PRIMARY KEY (dispositivo_id, variable_plantilla_id)
);

CREATE TABLE IF NOT EXISTS iot.regla (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    variable_plantilla_id uuid NOT NULL REFERENCES iot.variable_plantilla(id) ON DELETE CASCADE,
    nombre               text NOT NULL,
    expresion            text NOT NULL,
    severidad            text,
    habilitada           boolean NOT NULL DEFAULT true,
    ventana_segundos     integer,
    creada_en            timestamptz NOT NULL
);

CREATE TABLE IF NOT EXISTS iot.alerta (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    dispositivo_id uuid NOT NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    regla_id       uuid NOT NULL REFERENCES iot.regla(id) ON DELETE CASCADE,
    ts             timestamptz NOT NULL,
    detalles       jsonb
);

CREATE INDEX IF NOT EXISTS idx_alerta_dev_ts
  ON iot.alerta (dispositivo_id, ts DESC);

CREATE TABLE IF NOT EXISTS iot.comando_dispositivo (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    dispositivo_id uuid NOT NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    comando        text NOT NULL,
    datos          jsonb,
    estado         iot.estado_comando NOT NULL,
    solicitado_por text,
    solicitado_en  timestamptz NOT NULL,
    confirmado_en  timestamptz,
    mensaje_error  text
);

