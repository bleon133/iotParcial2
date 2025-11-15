CREATE TABLE IF NOT EXISTS iot.panel_widget_serie (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  widget_id uuid NOT NULL REFERENCES iot.panel_widget(id) ON DELETE CASCADE,
  variable_plantilla_id uuid NOT NULL REFERENCES iot.variable_plantilla(id) ON DELETE CASCADE,
  color text,
  label text
);
