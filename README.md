# Sistema de Gestión de Biblioteca Universitaria - Kinal (PR 6 & PR 7)

Backend desarrollado en **Java 21**, **Spring Boot 4.1.1**, **Maven**, **MySQL** y **Spring Security con JWT**, cumpliendo estrictamente con el contrato de endpoints inmutable y las reglas de negocio de préstamos, stock, límite de 3 préstamos y sanción automática.

## Instrucciones de Ejecución
1. Configurar la base de datos MySQL en `src/main/resources/application.properties`.
2. Ejecutar el script `database_seed.sql` para cargar los usuarios de prueba (`admin@biblioteca.com` y `lector@biblioteca.com`).
3. Ejecutar el proyecto mediante Maven:
   ```bash
   mvn clean spring-boot:run
   ```

## Endpoints Inmutables Soportados
- **Auth:** `/api/v1/auth/login`, `/api/v1/auth/register`
- **Libros:** `/libros` (GET, POST, PUT, DELETE)
- **Préstamos:** `/prestamos` (GET, POST), `/prestamos/{id}/devolucion` (POST)
