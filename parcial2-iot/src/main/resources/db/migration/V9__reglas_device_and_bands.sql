-- Extiende reglas: asociación opcional a dispositivo, tipo de regla y config JSON (bandas)
ALTER TABLE iot.regla
    ADD COLUMN IF NOT EXISTS dispositivo_id uuid NULL REFERENCES iot.dispositivo(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS tipo text NOT NULL DEFAULT 'expr',
    ADD COLUMN IF NOT EXISTS config jsonb NOT NULL DEFAULT '{}'::jsonb;

-- Índice auxiliar si se usa con dispositivo
CREATE INDEX IF NOT EXISTS idx_regla_disp ON iot.regla(dispositivo_id);

