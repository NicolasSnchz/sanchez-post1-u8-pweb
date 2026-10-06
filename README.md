# Post-contenido Unidad 8: Persistencia con JPA/Hibernate

**Estudiante:** Nicolás Andrés Sánchez Villamizar
**Materia:** Programación Web, Universidad de Santander (UDES)
**Repositorio:** sanchez-post1-u8-pweb

## Descripción

Este repositorio tiene un único proyecto Maven Spring Boot, [`catalogo-jpa/`](catalogo-jpa), que modela el catálogo de un pequeño comercio. Cada `Categoria` (por ejemplo Electrónica o Papelería) agrupa muchos `Producto`, y cada `Producto` pertenece exactamente a una `Categoria`.

En la Parte 1 hice el CRUD completo de categorías con Spring Data JPA e Hibernate contra MySQL, con vistas Thymeleaf. En la Parte 2 extendí el mismo proyecto con la entidad `Producto`, la relación `@ManyToOne`/`@OneToMany` hacia `Categoria` y una consulta JPQL personalizada que trae los productos de una categoría con precio mayor a un valor dado en una sola sentencia SQL.

## Funcionalidades implementadas

- Listar, crear, editar y eliminar categorías (`/categorias`).
- Validación de campos con Bean Validation (`@NotBlank`, `@Size`) y mensajes al lado de cada campo sin perder lo que el usuario escribió.
- Nombre de categoría único, validado en el servicio antes de llegar a la base de datos.
- Confirmación antes de eliminar una categoría.
- Listar, crear, editar y eliminar productos (`/productos`), mostrando el nombre de la categoría de cada uno.
- Validación de producto (`@NotBlank`, `@NotNull`, `@Positive`, `@Min`) y de que se haya escogido una categoría.
- Consulta filtrada `/productos/categoria/{id}/precio-mayor?minimo=X`, ordenada de mayor a menor precio.
- Rechazo de la eliminación de una categoría que todavía tiene productos, con un mensaje claro en pantalla.

## Estructura del proyecto

```
sanchez-post1-u8-pweb/
├── README.md
├── docs/capturas/
└── catalogo-jpa/
    ├── pom.xml
    ├── mvnw, mvnw.cmd, .mvn/wrapper/
    └── src/main/
        ├── java/com/universidad/catalogo/
        │   ├── CatalogoApplication.java
        │   ├── controller/  CategoriaController, ProductoController
        │   ├── model/       Categoria, Producto
        │   ├── repository/  CategoriaRepository, ProductoRepository
        │   └── service/     CategoriaService, ProductoService
        └── resources/
            ├── application.properties
            └── templates/
                ├── categorias/  lista, formulario, confirmar-eliminar
                └── productos/   lista, formulario, filtrados
```

## Relación entre Categoria y Producto

```
+------------------------------+          +--------------------------------+
|          categorias          |          |           productos            |
+------------------------------+          +--------------------------------+
| PK id           BIGINT       |<----+    | PK id            BIGINT        |
|    nombre       VARCHAR(80)  |     |    |    nombre        VARCHAR(120)  |
|                 UNIQUE       |     |    |    precio        DECIMAL(10,2) |
|                 NOT NULL     |     |    |    stock         INTEGER       |
|    descripcion  VARCHAR(250) |     +----| FK categoria_id  BIGINT        |
+------------------------------+  1    N  |                  NOT NULL      |
                                          +--------------------------------+

Categoria (1) ----------------------------< (N) Producto
@OneToMany(mappedBy = "categoria")              @ManyToOne(fetch = LAZY)
lado inverso, sin @JoinColumn                   @JoinColumn(name = "categoria_id")
                                                lado propietario, guarda la FK
```

`Producto` es el dueño de la relación porque es la tabla que guarda la clave foránea `categoria_id` ([Producto.java#L32-L35](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Producto.java#L32-L35)). `Categoria` solo tiene el lado inverso con `mappedBy` ([Categoria.java#L27-L28](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Categoria.java#L27-L28)).

## Configuración de la base de datos

1. Crear la base de datos y el usuario en MySQL 8:

```sql
CREATE DATABASE catalogo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER "appuser"@"localhost" IDENTIFIED BY "apppass";
GRANT ALL PRIVILEGES ON catalogo_db.* TO "appuser"@"localhost";
FLUSH PRIVILEGES;
```

Si MySQL corre en Docker sirve el comando del enunciado: `docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8`.

2. Revisar [`application.properties`](catalogo-jpa/src/main/resources/application.properties). Ya viene con la URL, el usuario `appuser` y la contraseña `apppass`. Si se usan otras credenciales se cambian las líneas `spring.datasource.username` y `spring.datasource.password`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/catalogo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=appuser
spring.datasource.password=apppass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
server.port=8080
```

No hace falta crear las tablas a mano: Hibernate crea `categorias` y `productos` con su clave foránea `FK_categoria` al arrancar.

## Cómo compilar y ejecutar

Requisitos: JDK 17 o superior y MySQL 8 en ejecución. Maven no es obligatorio porque el proyecto trae el wrapper.

```bash
git clone https://github.com/NicolasSnchz/sanchez-post1-u8-pweb.git
cd sanchez-post1-u8-pweb/catalogo-jpa
./mvnw spring-boot:run
```

Con Maven instalado también funciona `mvn spring-boot:run`. Después:

- Parte 1: http://localhost:8080/categorias
- Parte 2: http://localhost:8080/productos
- Consulta filtrada: http://localhost:8080/productos/categoria/1/precio-mayor?minimo=50000

## Parte 1: CRUD de Categoría con JPA/Hibernate y MySQL

- [`Categoria`](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Categoria.java) es una entidad con `@Entity`, `@Table`, `@Id`, `@GeneratedValue(IDENTITY)`, `@Column` y validaciones `@NotBlank` y `@Size`.
- [`CategoriaRepository`](catalogo-jpa/src/main/java/com/universidad/catalogo/repository/CategoriaRepository.java) extiende `JpaRepository<Categoria, Long>` y agrega las consultas derivadas `findByNombreIgnoreCase` y `findByNombreContainingIgnoreCase`.
- [`CategoriaService`](catalogo-jpa/src/main/java/com/universidad/catalogo/service/CategoriaService.java) tiene la lógica de negocio con `@Transactional` en las operaciones que escriben.
- [`CategoriaController`](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/CategoriaController.java) maneja las rutas del CRUD con `@Valid` y `BindingResult`, y usa inyección por constructor.
- Las vistas están en [`templates/categorias`](catalogo-jpa/src/main/resources/templates/categorias): `lista.html`, `formulario.html` (sirve para crear y editar) y `confirmar-eliminar.html`.

## Parte 2: Relación @ManyToOne/@OneToMany con Producto

- [`Producto`](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Producto.java) agrega `@ManyToOne(fetch = FetchType.LAZY)` con `@JoinColumn(name = "categoria_id", nullable = false)`.
- [`ProductoRepository`](catalogo-jpa/src/main/java/com/universidad/catalogo/repository/ProductoRepository.java) expone tres consultas con `@Query` y `JOIN FETCH`. La que pide el laboratorio es `buscarPorCategoriaConPrecioMayorA` ([L26-L31](catalogo-jpa/src/main/java/com/universidad/catalogo/repository/ProductoRepository.java#L26-L31)):

```java
@Query("SELECT p FROM Producto p JOIN FETCH p.categoria c " +
       "WHERE c.id = :categoriaId AND p.precio > :precioMinimo " +
       "ORDER BY p.precio DESC")
```

Trabaja sobre las entidades y no sobre las tablas, filtra por categoría y por precio a la vez y trae la categoría en la misma sentencia, así que la consola muestra un solo `select ... join categorias ...` y no una consulta por cada producto.

- [`ProductoService`](catalogo-jpa/src/main/java/com/universidad/catalogo/service/ProductoService.java) y [`ProductoController`](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/ProductoController.java) completan el CRUD y el endpoint `/productos/categoria/{categoriaId}/precio-mayor` ([L66-L75](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/ProductoController.java#L66-L75)).
- Las vistas están en [`templates/productos`](catalogo-jpa/src/main/resources/templates/productos): `lista.html`, `formulario.html` y `filtrados.html`.

## Decisiones de diseño

**ddl-auto=update en lugar de create** ([application.properties#L10](catalogo-jpa/src/main/resources/application.properties#L10)). Con `create` Hibernate borra y vuelve a crear las tablas en cada arranque, y se perderían las categorías y productos que fui cargando para probar. Con `update` solo agrega lo nuevo: al pasar a la Parte 2 apareció la tabla `productos` y su clave foránea sin tocar las categorías que ya existían. En producción usaría `validate` o `none`, porque `update` nunca borra columnas y puede dejar el esquema desalineado con las entidades.

**Nombre único en Categoria, validado en dos niveles.** La columna tiene `unique = true` ([Categoria.java#L18](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Categoria.java#L18)) para que la base nunca acepte dos "Electrónica". Además el servicio busca con `findByNombreIgnoreCase` antes de guardar ([CategoriaService.java#L29-L33](catalogo-jpa/src/main/java/com/universidad/catalogo/service/CategoriaService.java#L29-L33)) y lanza un error legible. Si solo dependiera de la restricción de la base, el usuario vería una `DataIntegrityViolationException` en vez de un mensaje en el formulario. La comparación ignora mayúsculas y además la intercalación `utf8mb4_unicode_ci` también ignora tildes, así que "electronica" y "Electrónica" cuentan como el mismo nombre en los dos niveles. Cuando se edita una categoría sin cambiarle el nombre, la búsqueda encuentra la misma categoría (mismo id) y no lo toma como duplicado.

**FetchType.LAZY explícito en Producto.categoria** ([Producto.java#L30-L35](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Producto.java#L30-L35)). Por defecto JPA carga `@ManyToOne` de forma EAGER, o sea que cada vez que se lee un producto también se trae su categoría aunque no se use (por ejemplo al validar o actualizar stock). Al dejarlo en LAZY esa carga solo pasa cuando se necesita. El riesgo de LAZY es el problema N+1: si la lista de productos accediera a `p.categoria.nombre` producto por producto, Hibernate haría un `select` adicional por cada fila. Por eso todas las consultas que alimentan vistas donde se muestra la categoría usan `JOIN FETCH` ([ProductoRepository.java#L17-L31](catalogo-jpa/src/main/java/com/universidad/catalogo/repository/ProductoRepository.java#L17-L31)): la lista general, la carga para editar y el filtro por precio. El lado inverso `@OneToMany` también es LAZY (que ya es su valor por defecto), porque la lista de productos de una categoría solo se necesita al validar el borrado.

**Sin cascade = REMOVE de Categoria hacia Producto.** Si el borrado de una categoría se propagara a sus productos, un clic equivocado en "Eliminar" se llevaría todo el inventario de esa categoría sin aviso. En cambio `CategoriaService.eliminar` revisa primero si la categoría tiene productos y, si los tiene, rechaza la operación con el mensaje "No se puede eliminar la categoria: tiene N producto(s) asociado(s)." ([CategoriaService.java#L37-L47](catalogo-jpa/src/main/java/com/universidad/catalogo/service/CategoriaService.java#L37-L47)). Así el usuario tiene que reasignar o borrar esos productos a propósito. La clave foránea `FK_categoria` en MySQL es una segunda barrera: aunque alguien se saltara el servicio, la base rechaza borrar una categoría con productos.

**Método helper en la entidad propietaria.** En vez de llamar `setCategoria` desde distintas capas, `Producto` tiene `asignarCategoria(...)` ([Producto.java#L44-L52](catalogo-jpa/src/main/java/com/universidad/catalogo/model/Producto.java#L44-L52)), que asigna la categoría y deja sincronizada la lista inversa: saca el producto de la categoría anterior y lo agrega a la nueva. Quité el setter público de `categoria` para que este sea el único camino. Así no pasa que en memoria un producto diga que es de Papelería mientras Electrónica todavía lo tiene en su lista dentro de la misma transacción.

**Inyección por constructor en todas las capas.** Los servicios y controladores reciben sus dependencias en el constructor y las guardan en campos `final`. Así no pueden quedar en `null` y se pueden probar sin levantar Spring.

## Correcciones al enunciado

Revisé el código de la guía contra sus checkpoints y la rúbrica y encontré estas inconsistencias. Las corregí así:

1. **Mensaje de nombre duplicado.** El Checkpoint 3 de la Parte 1 pide ver el mensaje "Ya existe una categoría con ese nombre", pero el controlador de la guía no captura la `IllegalStateException` que lanza el servicio, así que el navegador mostraría la página de error 500. Agregué un `try/catch` que convierte la excepción en un error del campo `nombre` con `result.rejectValue(...)` ([CategoriaController.java#L37-L44](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/CategoriaController.java#L37-L44)). El formulario vuelve con el mensaje y con lo que el usuario había escrito, que es lo que pide la rúbrica en Funcionalidad.
2. **Eliminar una categoría con productos.** Pasa lo mismo con el Checkpoint 3 de la Parte 2: el servicio lanza la excepción, pero sin capturarla el usuario vería un error 500 en vez del mensaje. El controlador ahora la captura y vuelve a `confirmar-eliminar.html` mostrando el mensaje en rojo ([CategoriaController.java#L64-L75](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/CategoriaController.java#L64-L75)).
3. **Faltaba editar productos.** La rúbrica pide que todas las operaciones CRUD de categorías y productos funcionen, pero el `ProductoController` de la guía no tenía edición. Agregué `/productos/editar/{id}`, que carga el producto con `findByIdConCategoria` (también con `JOIN FETCH`) para preseleccionar su categoría en el formulario. En el servicio, la edición actualiza la entidad que ya está administrada por JPA en lugar de hacer `merge` de un objeto suelto ([ProductoService.java#L33-L48](catalogo-jpa/src/main/java/com/universidad/catalogo/service/ProductoService.java#L33-L48)).
4. **Producto sin categoría.** En la guía `categoriaId` es un `@RequestParam` obligatorio. Si el usuario no escoge categoría, Spring responde 400 y se pierde el formulario. Lo dejé opcional y el controlador devuelve el formulario con el mensaje "Debe seleccionar una categoria" ([ProductoController.java#L47-L54](catalogo-jpa/src/main/java/com/universidad/catalogo/controller/ProductoController.java#L47-L54)), así nunca se llega al servicio con un producto sin categoría válida.
5. **Comentario equivocado sobre LAZY.** El comentario de la guía en `ProductoRepository` decía que la categoría era LAZY "por defecto". Es al revés: el valor por defecto de `@ManyToOne` es EAGER y aquí es LAZY porque se declaró a mano. Corregí el comentario.
6. **Nombre de la clave foránea.** El Paso 6 muestra la restricción con el nombre `FK_categoria`, pero Hibernate por sí solo genera un nombre aleatorio tipo `FKxxxxxxxx`. Lo fijé con `@ForeignKey(name = "FK_categoria")` para que coincida con el checkpoint.
7. **Dialecto de MySQL.** `org.hibernate.dialect.MySQL8Dialect` está obsoleto en Hibernate 6 y genera una advertencia al arrancar. Usé `org.hibernate.dialect.MySQLDialect`, que detecta la versión 8 por sí mismo.
8. **allowPublicKeyRetrieval=true en la URL.** MySQL 8 crea los usuarios con `caching_sha2_password`. Con `useSSL=false`, el conector puede rechazar la conexión de `appuser` (sobre todo la primera después de reiniciar MySQL) con el error "Public Key Retrieval is not allowed". Agregar ese parámetro lo resuelve sin activar SSL, que no tiene sentido en local.
9. **Versión de Spring Boot.** La guía pide Spring Boot 3.2.x, pero en mi equipo uso JDK 23 y la línea 3.2 solo soporta oficialmente hasta Java 21 (además ya no recibe parches). Usé Spring Boot 3.5.6 con `release 17` en el `maven-compiler-plugin` 3.13.0, así el código sigue siendo Java 17 como pide el enunciado. Las dependencias son las mismas: Web, Data JPA, MySQL Driver, Thymeleaf y Validation.
10. **Validación del precio.** Agregué `@Digits(integer = 8, fraction = 2)` a `precio` porque la columna es `DECIMAL(10,2)`. Sin esa validación, un precio de más de 8 cifras enteras pasaba el formulario y fallaba recién en MySQL con un error 500.

## Capturas de pantalla

**Arranque y esquema generado por Hibernate**

![Consola al arrancar con el SQL de creación de tablas](docs/capturas/01-consola-arranque.png)

![SHOW TABLES y DESCRIBE productos en MySQL](docs/capturas/02-mysql-tablas.png)

**Parte 1: CRUD de categorías**

![Lista de categorías](docs/capturas/03-lista-categorias.png)

![Formulario de edición de categoría](docs/capturas/04-editar-categoria.png)

![Error de nombre duplicado](docs/capturas/05-categoria-duplicada.png)

![Confirmación antes de eliminar](docs/capturas/06-confirmar-eliminar.png)

**Parte 2: productos y consulta filtrada**

![Formulario de producto](docs/capturas/07-formulario-producto.png)

![Lista de productos con su categoría](docs/capturas/08-lista-productos.png)

![Productos filtrados por categoría y precio](docs/capturas/09-productos-filtrados.png)

![Única sentencia SQL con JOIN en la consola](docs/capturas/10-consola-sql-filtro.png)

![Rechazo al eliminar una categoría con productos](docs/capturas/11-categoria-con-productos.png)

## Commits

El historial sigue la secuencia del enunciado, con cuatro commits para la Parte 1 (configuración, entidad y repositorio, servicio, controlador y vistas) y cuatro para la Parte 2 (entidad Producto y relación, repositorio con JPQL, servicio y controlador, documentación).
