# TFG-EasyFactura_Alvaro

# EasyFactura

EasyFactura es una aplicacion web desarrollada como Trabajo de Fin de Grado para la gestion de empresas, clientes, productos y facturas. El proyecto incorpora autenticacion por roles, aislamiento de datos por empresa, generacion de PDFs, firma digital local de facturas y trazabilidad de rectificaciones.

## Funcionalidades principales

- Gestion de empresas con alta, edicion y eliminacion.
- Gestion de clientes y productos filtrada por empresa.
- Inicio de sesion con roles `ADMIN` y `EMPRESA`.
- Creacion de facturas con lineas dinamicas de detalle.
- Calculo de base imponible, IVA y total por factura.
- Rectificacion de facturas y consulta de historico.
- Generacion y descarga de facturas en PDF.
- Firma digital local del PDF usando un certificado `.p12` por empresa.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.5.12
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL
- Apache PDFBox
- Bouncy Castle
- Bootstrap 5

## Requisitos previos

Para ejecutar el proyecto en local necesitas:

- Java 21 instalado
- MySQL disponible
- Variables de entorno configuradas:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`

## Configuracion local

La aplicacion utiliza estas propiedades:

- `spring.datasource.url=${DB_URL}`
- `spring.datasource.username=${DB_USER}`
- `spring.datasource.password=${DB_PASSWORD}`
- `app.certificados.directorio=certificados`
- `app.certificados.master-secret=EasyFacturaSecret2026`

La carpeta `certificados/` se genera en local y almacena los certificados `.p12` de las empresas. No se sube al repositorio.

## Despliegue y ejecucion

1. Clona el repositorio.
2. Configura las variables de entorno de base de datos.
3. Crea la base de datos en MySQL.
4. Ejecuta la aplicacion con el wrapper de Maven:

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicacion quedara disponible por defecto en:

```text
http://localhost:8080
```

## Estructura general

El proyecto sigue una arquitectura por capas:

- `controller`: gestiona las peticiones web.
- `service`: concentra la logica de negocio.
- `repository`: acceso a base de datos con JPA.
- `model`: entidades del dominio.
- `dto`: objetos auxiliares para formularios.
- `templates`: vistas Thymeleaf.

## Capturas recomendadas

Para documentar el proyecto en GitHub o en la memoria, las capturas mas representativas son:

- Pantalla de login.
- Listado de empresas para administrador.
- Dashboard inicial con contadores de clientes, productos y facturas.
- Listado de facturas.
- Creacion de factura con lineas dinamicas.
- Historico de rectificaciones.
- Descarga de PDF firmado.

## Estado actual

Actualmente EasyFactura permite gestionar la informacion principal de cada empresa, limitar el acceso segun el usuario autenticado y trabajar con facturas mas cercanas a un entorno real, incluyendo detalle de lineas, rectificaciones, historico y descarga documental.

## Autor

- Alvaro Orgaz
