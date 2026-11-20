-- Órdenes ficticias para que los tres filtros muestren resultados con el mismo técnico.
INSERT INTO ordenes_trabajo (id, version, supervisor_id, tecnico_id, cliente, direccion, descripcion, fecha, estado)
SELECT muestra.id::uuid, 0, supervisor.id, tecnico.id, muestra.cliente, muestra.direccion,
       muestra.descripcion, CURRENT_DATE + muestra.dias, muestra.estado
FROM (VALUES
    ('4ca19990-d752-418b-9bb2-d5fd3ac25a01', 'Demo Taller Norte', 'Sector de pruebas A',
     'Revisión del motor eléctrico y prueba de funcionamiento.', -2, 'COMPLETADA'),
    ('4ca19990-d752-418b-9bb2-d5fd3ac25a02', 'Demo Planta Central', 'Sector de pruebas B',
     'Diagnóstico de bomba. Falta la prueba final de presión.', 0, 'EN_PROCESO'),
    ('4ca19990-d752-418b-9bb2-d5fd3ac25a03', 'Demo Bodega Sur', 'Sector de pruebas C',
     'Inspección programada del tablero de control.', 2, 'PENDIENTE')
) AS muestra(id, cliente, direccion, descripcion, dias, estado)
JOIN users AS supervisor ON supervisor.username = '111111111' AND supervisor.role = 'SUPERVISOR'
JOIN users AS tecnico ON tecnico.username = '123456785' AND tecnico.role = 'TECNICO'
ON CONFLICT (id) DO NOTHING;
