-- Trigger: valida un solo valor y upsert al estado_dispositivo
CREATE OR REPLACE FUNCTION iot.fn_telemetria_validate_and_upsert()
RETURNS trigger AS $$
DECLARE
  v_tipo iot.tipo_dato;
  v_count int;
BEGIN
  v_count :=
    (CASE WHEN NEW.valor_numero   IS NOT NULL THEN 1 ELSE 0 END) +
    (CASE WHEN NEW.valor_booleano IS NOT NULL THEN 1 ELSE 0 END) +
    (CASE WHEN NEW.valor_texto    IS NOT NULL THEN 1 ELSE 0 END) +
    (CASE WHEN NEW.valor_json     IS NOT NULL THEN 1 ELSE 0 END);

  IF v_count <> 1 THEN
    RAISE EXCEPTION 'Debe enviar exactamente un valor (numero|booleano|texto|json)';
  END IF;

  SELECT tipo_dato INTO v_tipo
  FROM iot.variable_plantilla
  WHERE id = NEW.variable_plantilla_id;

  IF v_tipo = 'numero'   AND NEW.valor_numero   IS NULL THEN RAISE EXCEPTION 'Se esperaba valor_numero'; END IF;
  IF v_tipo = 'booleano' AND NEW.valor_booleano IS NULL THEN RAISE EXCEPTION 'Se esperaba valor_booleano'; END IF;
  IF v_tipo = 'cadena'   AND NEW.valor_texto    IS NULL THEN RAISE EXCEPTION 'Se esperaba valor_texto'; END IF;
  IF v_tipo = 'json'     AND NEW.valor_json     IS NULL THEN RAISE EXCEPTION 'Se esperaba valor_json'; END IF;

  INSERT INTO iot.estado_dispositivo(
    dispositivo_id, variable_plantilla_id, ultimo_ts, ultimo_numero, ultimo_booleano, ultimo_texto, ultimo_json
  ) VALUES (
    NEW.dispositivo_id, NEW.variable_plantilla_id, NEW.ts, NEW.valor_numero, NEW.valor_booleano, NEW.valor_texto, NEW.valor_json
  )
  ON CONFLICT (dispositivo_id, variable_plantilla_id) DO UPDATE SET
    ultimo_ts = EXCLUDED.ultimo_ts,
    ultimo_numero = EXCLUDED.ultimo_numero,
    ultimo_booleano = EXCLUDED.ultimo_booleano,
    ultimo_texto = EXCLUDED.ultimo_texto,
    ultimo_json = EXCLUDED.ultimo_json;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_telemetria_upsert_estado ON iot.telemetria;
CREATE TRIGGER trg_telemetria_upsert_estado
BEFORE INSERT ON iot.telemetria
FOR EACH ROW
EXECUTE FUNCTION iot.fn_telemetria_validate_and_upsert();

