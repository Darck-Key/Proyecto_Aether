# Registros de Decisiones de Arquitectura (ADR)

## ADR-001 - Estrategia híbrida para obtener la trayectoria de la misión

**Estado:** Aceptada

### Contexto

AETHER debe representar el perfil nominal de Artemis II y, al mismo tiempo, permitir simulaciones con parámetros personalizados. La versión Aether2.2.zip incluye una efeméride de referencia en formato CCSDS OEM y también un planificador de trayectoria basado en Orekit.

Una trayectoria de referencia permite reproducir el caso nominal, pero por sí sola no responde a cambios introducidos por el usuario. Una propagación numérica, en cambio, permite generar escenarios personalizados, aunque depende de los modelos de fuerza y simplificaciones implementados.

En el código revisado, MissionSimulator decide la fuente de la trayectoria mediante ArtemisReferenceTrajectoryLoader y OrekitTrajectoryPlanner. ArtemisReferenceTrajectoryLoader.supports(config) delega en MissionPresets.isArtemisIIReference(config).

### Decisión

Se adopta una estrategia híbrida. Cuando la configuración corresponde al perfil de referencia de Artemis II, MissionSimulator utiliza ArtemisReferenceTrajectoryLoader para cargar la efeméride OEM incluida como recurso del proyecto.

Cuando la configuración no corresponde al perfil de referencia, MissionSimulator utiliza OrekitTrajectoryPlanner.precompute(config) para generar una trayectoria numérica.

Ambas rutas entregan una MissionTrajectory compuesta por MissionState, de modo que telemetría, reproducción, visualización y reportes puedan consumir un modelo interno común sin depender del origen de la trayectoria.

### Alternativas consideradas

1. **Usar propagación numérica para todos los escenarios:** Se descartó como única estrategia porque el caso nominal dejaría de aprovechar directamente la efeméride de referencia incluida y dependería por completo de las simplificaciones del modelo propagado.

2. **Usar únicamente la efeméride OEM:** Se descartó porque una trayectoria fija no puede responder adecuadamente a configuraciones personalizadas de la misión.

3. **Implementar un motor orbital propio:** Se descartó por el mayor esfuerzo de desarrollo, riesgo de errores y complejidad de validación frente al uso de una biblioteca especializada como Orekit.

### Consecuencias

**Consecuencias positivas:**

- El escenario nominal puede utilizar una trayectoria de referencia incluida en la aplicación.
- Las configuraciones personalizadas pueden generar una trayectoria propia.
- La telemetría, reproducción, mapa y reportes consumen un mismo modelo de trayectoria.
- Se combina referencia nominal con flexibilidad de simulación.

**Consecuencias negativas y compromisos:**

- Deben mantenerse y probarse dos mecanismos diferentes para obtener la trayectoria.
- El sistema debe reconocer correctamente cuándo una configuración corresponde al perfil de referencia.
- Las trayectorias personalizadas dependen de las simplificaciones y modelos físicos configurados en el propagador.

---

## ADR-002 - Separación entre el cálculo orbital y la reproducción gráfica

**Estado:** Aceptada

### Contexto

La propagación orbital puede requerir cálculos costosos y generar numerosos estados. JavaFX concentra la actualización visual en su hilo de aplicación, por lo que realizar el cálculo físico directamente allí podría afectar la capacidad de respuesta de la interfaz.

AETHER también necesita controles de reproducción como iniciar, pausar, reanudar y cambiar la escala temporal. Esos controles pertenecen a la presentación y no deben modificar la trayectoria física calculada.

En Aether2.2.zip, HelloController, MissionSimulator, TrajectoryPlayback y SimulationListener distribuyen estas responsabilidades.

### Decisión

Se separa el precálculo físico de la reproducción visual. HelloController ejecuta MissionSimulator.prepareTrajectory() dentro de un hilo denominado aether-orekit-precalculation.

Al terminar el cálculo, la interfaz utiliza Platform.runLater() para iniciar la reproducción en JavaFX. TrajectoryPlayback extiende AnimationTimer y recorre los estados ya calculados, aplicando la velocidad de reproducción sin volver a propagar la órbita.

MissionSimulator conserva un recorrido con Thread.sleep() para compatibilidad con pruebas sin JavaFX, pero la ruta de interfaz revisada utiliza TrajectoryPlayback como mecanismo de reproducción.

### Alternativas consideradas

1. **Propagar directamente en el hilo de JavaFX:** Se descartó porque una operación prolongada podría bloquear o volver poco responsiva la interfaz.

2. **Calcular y mostrar cada estado de forma acoplada:** Se descartó porque uniría la velocidad del cálculo físico con la velocidad de representación visual.

3. **Recalcular la trayectoria al cambiar la escala de tiempo:** Se descartó porque la escala temporal es una característica de reproducción y no un cambio en los parámetros físicos de la misión.

4. **Usar Thread.sleep() como mecanismo principal de animación:** Se descartó para la interfaz JavaFX porque acoplaría la experiencia visual al recorrido directo de las muestras; se conserva únicamente una ruta de compatibilidad para pruebas sin JavaFX.

### Consecuencias

**Consecuencias positivas:**

- El cálculo orbital se mantiene fuera del hilo gráfico principal.
- Pausar, reanudar o cambiar la velocidad no altera los resultados físicos.
- TrajectoryPlayback permite una reproducción continua de los estados precalculados.
- La misma trayectoria puede alimentar telemetría, mapa y otros componentes.

**Consecuencias negativas y compromisos:**

- Existe un tiempo inicial de espera mientras se prepara la trayectoria.
- Los estados deben mantenerse disponibles durante la reproducción.
- Se requiere coordinación entre el hilo de precálculo y el hilo de JavaFX.
- La separación introduce componentes adicionales para cálculo, reproducción y notificación.

---

## ADR-003 - Abstracción de persistencia con MySQL y repositorio de respaldo

**Estado:** Aceptada

### Contexto

AETHER registra información asociada a las ejecuciones del simulador. MySQL ofrece persistencia entre sesiones, pero las funciones principales de simulación no deberían quedar bloqueadas si la base de datos no está configurada o disponible.

Acoplar directamente la interfaz con JDBC/MySQL también mezclaría responsabilidades de presentación, infraestructura y persistencia.

La implementación revisada incorpora AetherRepository, RepositoryFactory, MySqlAetherRepository, PendingDatabaseRepository y DatabaseConfig para aislar esta responsabilidad.

### Decisión

Se define AetherRepository como contrato común de persistencia. RepositoryFactory selecciona la implementación que utilizará la aplicación.

Si MySQL está habilitado y disponible, se utiliza MySqlAetherRepository. Si la base de datos no está disponible o la configuración falla, se utiliza PendingDatabaseRepository como respaldo temporal en memoria.

DatabaseConfig centraliza los datos de conexión y permite leer valores desde variables de entorno o propiedades de la JVM, evitando fijar credenciales directamente en la lógica de la interfaz.

### Alternativas consideradas

1. **Acceder a MySQL directamente desde HelloController:** Se descartó porque aumentaría el acoplamiento entre interfaz, persistencia y manejo de conexiones.

2. **Hacer MySQL obligatorio para iniciar AETHER:** Se descartó porque una falla externa de infraestructura impediría usar funciones del simulador que pueden operar sin persistencia permanente.

3. **Usar únicamente almacenamiento en memoria:** Se descartó como solución principal porque los datos no se conservarían entre ejecuciones.

4. **Usar únicamente archivos locales:** Se descartó como estrategia principal porque no ofrece la misma organización estructurada y capacidad de consulta de una base de datos relacional para los registros manejados por el sistema.

### Consecuencias

**Consecuencias positivas:**

- La lógica principal depende de una interfaz de repositorio y no directamente de JDBC.
- AETHER puede continuar operando cuando MySQL no está disponible.
- La implementación de persistencia puede sustituirse con menor impacto en otros módulos.
- La configuración de la base de datos queda centralizada y las credenciales pueden mantenerse fuera del código.

**Consecuencias negativas y compromisos:**

- Los datos guardados solo en PendingDatabaseRepository se pierden al cerrar la aplicación.
- Deben mantenerse dos implementaciones del mismo contrato.
- El modo temporal no ofrece la misma durabilidad que MySQL.
- La interfaz debe distinguir claramente entre persistencia permanente y almacenamiento temporal.
