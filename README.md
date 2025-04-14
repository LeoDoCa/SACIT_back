
# Backend  SACIT - Sistema de Administración de citas y Trámites






## Descripción
SACIT es un sistema de gestión de citas y trámites que permite la administración de ventanillas, usuarios y procedimientos administrativos.

## Requisitos Previos
- Java JDK 17 
- MySQL 8.0+
- IDE recomendado: IntelliJ IDEA 
- Versión del wrapper: 3.3.2
- Versión de Maven: 3.9.9

## Tecnologías Utilizadas
### Backend
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Lombok
- BCrypt para encriptación
- Log4j para logging

## Configuración del entorno 
**1.** Clonar el repositorio
git clone <[URL_DEL_REPOSITORIO](https://github.com/LeoDoCa/SACIT_back/tree/main)>  cd SACIT

**2.** Crea una base de datos en MySQL
```sql
CREATE DATABASE sacit_dev;
USE sacit_dev;
```
**3** agrega a la carpeta de resources los archivos :

- application.properties
- application-dev.properties
- application-prod.properties
- application-test.properties
- email.properties

**4** Ejecuta el proyecto antes de utilizar el frontend

**El archivo con la configuración del proyecto es application-dev.properties si es necesario configura la siguiente línea por si deseas guardar cambios:**
 ```properties
spring.jpa.hibernate.ddl-auto=update
```
