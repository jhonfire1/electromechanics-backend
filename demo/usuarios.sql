-- Cuentas ficticias para la demostración en una base local.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, password, role)
VALUES
    ('123456785', crypt('12345', gen_salt('bf', 10)), 'TECNICO'),
    ('222222222', crypt('DemoTecnico22', gen_salt('bf', 10)), 'TECNICO'),
    ('333333333', crypt('DemoTecnico33', gen_salt('bf', 10)), 'TECNICO'),
    ('111111111', crypt('DemoSupervisor26', gen_salt('bf', 10)), 'SUPERVISOR')
ON CONFLICT (username) DO NOTHING;
