-- Agregados (rollups) por hora y por día, y soporte a retención por antigüedad

CREATE TABLE IF NOT EXISTS iot.telemetria_rollup_hourly (
    dispositivo_id        uuid NOT NULL,
    variable_plantilla_id uuid NOT NULL,
    bucket_ts             timestamptz NOT NULL,
    cnt                   bigint NOT NULL,
    avg                   double precision,
    min                   double precision,
    max                   double precision,
    PRIMARY KEY (dispositivo_id, variable_plantilla_id, bucket_ts)
);

CREATE INDEX IF NOT EXISTS idx_tel_hourly_bucket ON iot.telemetria_rollup_hourly (bucket_ts);

CREATE TABLE IF NOT EXISTS iot.telemetria_rollup_daily (
    dispositivo_id        uuid NOT NULL,
    variable_plantilla_id uuid NOT NULL,
    bucket_ts             timestamptz NOT NULL,
    cnt                   bigint NOT NULL,
    avg                   double precision,
    min                   double precision,
    max                   double precision,
    PRIMARY KEY (dispositivo_id, variable_plantilla_id, bucket_ts)
);

CREATE INDEX IF NOT EXISTS idx_tel_daily_bucket ON iot.telemetria_rollup_daily (bucket_ts);

