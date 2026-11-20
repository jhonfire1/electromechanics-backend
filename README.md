# Electromechanics Backend

Backend de la aplicación de mantención industrial. `auth-service` gestiona acceso y roles. `operations-service` guarda órdenes en PostgreSQL y autoriza al supervisor y al técnico con el JWT emitido por autenticación.

## Demostración local

Necesitas Docker Desktop abierto. Desde la raíz del backend:

```powershell
Copy-Item .env.example .env
```

Define `POSTGRES_PASSWORD` y `DB_PASSWORD` con la misma contraseña en `.env`. Establece `JWT_SECRET` con un valor aleatorio de al menos 32 bytes. Puedes generar un valor en PowerShell con `[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')`. Conserva `.env` como archivo local.

```powershell
docker compose up -d --build
docker compose logs -f auth-service operations-service
```

Cuando aparezca el mensaje `Started` para ambos servicios, sal de la vista de logs con Ctrl+C. Carga las cuentas ficticias en esta base local:

```powershell
Get-Content -Raw demo/usuarios.sql | docker compose exec -T postgres psql -U electro -d electromechanics -v ON_ERROR_STOP=1
```

| Rol | RUT | Contraseña de demostración |
| --- | --- | --- |
| Supervisor | 11.111.111-1 | `DemoSupervisor26` |
| Técnico | 12.345.678-5 | `12345` |
| Técnico | 22.222.222-2 | `DemoTecnico22` |
| Técnico | 33.333.333-3 | `DemoTecnico33` |

Para cargar las tres órdenes ficticias de la hoja de ruta, ejecuta desde el directorio del backend en Ubuntu (WSL):

```bash
sudo docker compose exec -T postgres psql -U electro -d electromechanics -v ON_ERROR_STOP=1 --single-transaction -f - < demo/ordenes.sql
```

Las órdenes de ejemplo están asignadas al técnico 12.345.678-5 para que una sola sesión muestre los estados pendiente, en proceso y completada. Si vuelves a ejecutar el archivo, las órdenes que ya existen conservan su estado.

Los servicios quedan disponibles en `127.0.0.1:8081` y `127.0.0.1:8082`. PostgreSQL se publica en `127.0.0.1:5434` para pruebas con Maven. Las cuentas anteriores son datos ficticios para esta demostración.

## Contratos REST

| Método y ruta | Rol | Resultado |
| --- | --- | --- |
| `POST /api/auth/login` | Público | Devuelve JWT, ID y rol. Android envía el RUT sin puntos ni guion. |
| `POST /api/auth/register` | Supervisor | Registra una cuenta con rol permitido. |
| `GET /api/auth/tecnicos` | Supervisor | Lista técnicos con su ID y RUT. |
| `GET /api/auth/tecnicos/{id}` | Supervisor | Confirma que el ID corresponde a un técnico. |
| `POST /api/ordenes` | Supervisor | Guarda una orden asignada con estado pendiente. |
| `GET /api/ordenes/asignadas` | Técnico | Lista las órdenes del usuario identificado por el JWT. |
| `PUT /api/ordenes/{id}/estado` | Técnico asignado | Avanza a en proceso y luego a completada. |

La creación recibe un UUID de Android. Reenviar la misma solicitud con el mismo UUID devuelve la orden existente. La fecha usa `AAAA-MM-DD`.

## Pruebas

Con Java 21, PostgreSQL local y ambos microservicios detenidos o aislados según corresponda:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5434/electromechanics'
$env:DB_USERNAME = 'electro'
$env:DB_PASSWORD = '<contraseña definida en .env>'
$env:JWT_SECRET = '<secreto definido en .env>'
Push-Location auth-service
.\mvnw.cmd test
Pop-Location
Push-Location operations-service
.\mvnw.cmd test
Pop-Location
```

Cada servicio incluye una prueba de arranque con PostgreSQL. Las pruebas unitarias cubren autenticación, roles permitidos, asignación, reintento, propiedad de la orden y cambios de estado. Para detener la demostración, usa `docker compose down` y conserva los datos en su volumen.
