CREATE TABLE IF NOT EXISTS iot.panel (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre text NOT NULL,
  descripcion text,
  creado_en timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS iot.panel_widget (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  panel_id uuid NOT NULL REFERENCES iot.panel(id) ON DELETE CASCADE,
  dispositivo_id uuid NOT NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
  variable_plantilla_id uuid NOT NULL REFERENCES iot.variable_plantilla(id) ON DELETE CASCADE,
  titulo text,
  chart_type text NOT NULL DEFAULT 'line',
  rango text NOT NULL DEFAULT '24h',
  color text DEFAULT '#0d6efd',
  pos integer DEFAULT 0
);
