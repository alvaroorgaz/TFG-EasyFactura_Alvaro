# Help

## Ejecucion en desarrollo

1. Configura las variables de entorno:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `CERT_MASTER_SECRET`
2. Arranca la aplicacion con:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Accede a `http://localhost:8080`.

## Perfiles disponibles

- `dev`: pensado para desarrollo local, muestra SQL y permite `ddl-auto=update`.
- `prod`: pensado para despliegue, no muestra SQL y deja `ddl-auto=validate`.

Para cambiar de perfil:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
```

## Credenciales iniciales

El administrador inicial se crea automaticamente si no existe, usando estas variables:

- `ADMIN_NOMBRE`
- `ADMIN_CIF`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

Si no defines valores, se usan los configurados por defecto en `application.properties`.

## Certificados locales

Cada empresa genera un certificado `.p12` local al darse de alta. Los certificados se guardan en la carpeta `certificados/` y se usan para firmar digitalmente los PDFs de factura.

## Modulo de facturas

El sistema permite:

- crear facturas con lineas dinamicas
- calcular base imponible, IVA y total
- generar hash encadenado
- descargar PDF
- firmar el PDF con el certificado de la empresa
- rectificar facturas y consultar historico
