<h2 alinear="centro">
    Registro de decisiones del proyecto
</h2>

<h3>
    Uso de la inteligencia artificial
</h3>

<p>
    En este apartado se detallara como se integraron las herramientas de inteligencia artificial en nuestro proyecto.
</p>

<br>

<h3>
    Partes Asistidas por IA
</h3>

<p>
    La inteligencia artificial fue utilizada como copiloto de desarrollo y redacción en las siguientes áreas:
</p>

<p>
        <strong>- Explicaciones de diversas partes del documento SRS:</strong> La IA nos asistió explicando partes o apartados del documento los cuales no estaban dentro de nuestros conocimientos.
</p>
<br>

<p>
        <strong>- Optimización y revisión de códigos:</strong> La IA nos ayudo con algunos problemas de optimización y con la revicion del codigo, gracias a eso nos ahorro mucho tiempo.
</p>
<br>
<p>
        <strong>- Redacción y documentación:</strong> en diversos contextos la IA fue utilizada para darle un mejor formato y claridad a lo que queríamos expresar.
</p>

<br>

<h3>
    Refinamientos requeridos
</h3>

<p>
    Si bien, la IA nos proporcionó una base sólida, realizaron las siguientes intervenciones humanas criticas:
</p>
<br>

<p>
        - Los códigos fueron revisados por el equipo de desarrollo antes de ser utilizado, evitando que este no se alineara con lo establecido.
</p>
<br>

<p>
        - La documentación fue leída y corregida, evitando redundancias y tecnicismos muy complejos.
</p>
<br>

<p>
       - Corrección de bibliotecas que pudieran estar obsoletas o que presentaban algún peligro para la seguridad de nuestro programa y equipos.
</p>

<br>

<h3>
    Herramientas utilizadas:
</h3>

<p> ChatGpt</p><br>
<p> Gemini</p><br>
<p> Codex</p><br>

<p>
    La inteligencia artificial fue utilizada como una herramienta de apoyo durante el desarrollo del proyecto. Todas las decisiones finales, validaciones técnicas y aprobaciones fueron realizadas por los integrantes del equipo, garantizando la calidad y confiabilidad de los resultados obtenidos.
</p>

<h2 align="center">
    Registro de decisiones del proyecto
</h2>

<p>
    En este apartado se registran las principales decisiones de arquitectura tomadas
    durante el desarrollo del simulador AETHER para la misión Artemis II.
    Estas decisiones permiten documentar el problema que motivó cada elección,
    la solución adoptada, las alternativas consideradas y las consecuencias
    resultantes de cada decisión.
</p>

<br>

<h3>
    Uso de la inteligencia artificial
</h3>

<p>
    Durante la elaboración de los registros de decisiones de arquitectura se utilizó
    inteligencia artificial como herramienta de apoyo para analizar la estructura
    del proyecto, organizar la información técnica y redactar los ADR.
</p>

<p>
    Las decisiones documentadas fueron contrastadas con la implementación real del
    proyecto AETHER, revisando las clases, componentes y relaciones presentes en el
    código fuente. El contenido generado fue sometido a revisión humana antes de ser
    incorporado al repositorio.
</p>

<br>

<h3>
    Partes asistidas por IA
</h3>

<table>
    <tr>
        <th>Herramienta</th>
        <th>Propósito</th>
        <th>Revisión humana</th>
    </tr>

    <tr>
        <td>ChatGPT</td>
        <td>
            Apoyo en el análisis del código fuente del proyecto, identificación de
            decisiones arquitectónicas relevantes y organización de los registros ADR.
        </td>
        <td>
            Se verificó que las decisiones descritas correspondieran con componentes
            realmente implementados dentro del proyecto AETHER antes de incorporarlas
            al documento.
        </td>
    </tr>

    <tr>
        <td>ChatGPT</td>
        <td>
            Apoyo en la redacción y estructuración del contenido de los ADR de acuerdo
            con los campos requeridos para el Entregable #6.
        </td>
        <td>
            El contenido fue revisado y adaptado antes de su inclusión definitiva
            en el archivo <code>decisiones.md</code>.
        </td>
    </tr>
</table>

<br>
<hr>
<br>

<h2 align="center">
    Registros de Decisiones de Arquitectura (ADR)
</h2>

<p>
    Los siguientes registros documentan tres de las decisiones de diseño
    arquitectónico más importantes implementadas en AETHER.
</p>

<p>
    Cada registro contiene el contexto de la decisión, la opción seleccionada,
    las alternativas consideradas y las consecuencias positivas y negativas
    asociadas con su implementación.
</p>

<br>

<h3>
    ADR-001 — Estrategia híbrida para obtener la trayectoria de la misión
</h3>

<table>
    <tr>
        <th>Campo</th>
        <th>Contenido</th>
    </tr>

    <tr>
        <td><b>Título y estado</b></td>
        <td>
            <b>ADR-001 — Estrategia híbrida para obtener la trayectoria de la misión.</b>
            <br><br>
            <b>Estado:</b> Aceptada.
        </td>
    </tr>

    <tr>
        <td><b>Contexto</b></td>
        <td>
            El simulador AETHER debe ser capaz de representar la trayectoria nominal
            de la misión Artemis II y, al mismo tiempo, permitir la simulación de
            configuraciones personalizadas introducidas por el usuario.
            <br><br>

            El proyecto dispone de una efeméride de referencia de Artemis II almacenada
            en formato CCSDS OEM dentro de los recursos de la aplicación. Esta información
            permite reproducir una trayectoria de referencia para la misión.
            <br><br>

            Sin embargo, una trayectoria almacenada previamente no puede reaccionar
            directamente cuando el usuario modifica parámetros como la altitud inicial,
            velocidad, inclinación, excentricidad, masa de la nave o los parámetros
            relacionados con la maniobra TLI.
            <br><br>

            Para resolver esta situación, la clase <code>MissionSimulator</code>
            centraliza la selección de la trayectoria y utiliza
            <code>ArtemisReferenceTrajectoryLoader</code> para el perfil de referencia
            de Artemis II y <code>OrekitTrajectoryPlanner</code> para configuraciones
            personalizadas.
        </td>
    </tr>

    <tr>
        <td><b>Decisión</b></td>
        <td>
            Se adopta una <b>estrategia híbrida para la obtención de trayectorias</b>.
            <br><br>

            Cuando la configuración utilizada corresponde al perfil nominal de
            Artemis II, el sistema utiliza
            <code>ArtemisReferenceTrajectoryLoader</code>.
            Esta clase carga la efeméride incluida en
            <code>artemis-ii-flight-oem.asc</code> mediante las herramientas de
            lectura CCSDS OEM proporcionadas por Orekit.
            <br><br>

            La clase <code>MissionPresets</code> permite determinar si la configuración
            corresponde al perfil de referencia de Artemis II.
            <br><br>

            Cuando la configuración no corresponde al perfil de referencia,
            <code>MissionSimulator</code> utiliza
            <code>OrekitTrajectoryPlanner.precompute()</code> para generar una nueva
            trayectoria mediante propagación numérica.
            <br><br>

            Para las trayectorias personalizadas,
            <code>OrekitTrajectoryPlanner</code> utiliza
            <code>NumericalPropagator</code> junto con el integrador
            <code>DormandPrince853Integrator</code>.
            <br><br>

            El modelo incluye gravedad terrestre mediante armónicos esféricos
            de grado y orden 8x8, atracción gravitatoria de tercer cuerpo de
            la Luna y el Sol, y una maniobra TLI impulsiva.
            <br><br>

            Independientemente de cuál mecanismo genere la trayectoria,
            los resultados son transformados al modelo común
            <code>MissionTrajectory</code> y a una colección de
            <code>MissionState</code>, permitiendo que la interfaz, telemetría,
            mapa y reportes utilicen la información sin depender de la fuente
            que generó la trayectoria.
        </td>
    </tr>

    <tr>
        <td><b>Alternativas consideradas</b></td>
        <td>
            <b>1. Utilizar propagación numérica para todas las simulaciones:</b>
            <br>
            Esta alternativa permitiría utilizar un solo mecanismo para generar
            todas las trayectorias. Sin embargo, el perfil nominal dejaría de
            aprovechar directamente la efeméride de referencia incluida en el proyecto
            y dependería completamente de las simplificaciones y modelos físicos
            utilizados por el propagador.
            <br><br>

            <b>2. Utilizar únicamente la efeméride OEM:</b>
            <br>
            Esta alternativa simplificaría la generación de la trayectoria nominal,
            pero impediría que el simulador respondiera correctamente cuando el usuario
            modifica parámetros de la misión.
            <br><br>

            <b>3. Implementar un motor orbital completamente propio:</b>
            <br>
            Esta alternativa fue descartada debido al aumento del esfuerzo de desarrollo,
            mayor riesgo de errores matemáticos y mayor dificultad para validar los
            resultados frente a una biblioteca especializada como Orekit.
        </td>
    </tr>

    <tr>
        <td><b>Consecuencias</b></td>
        <td>
            <b>Consecuencias positivas:</b>
            <ul>
                <li>
                    El perfil nominal de Artemis II puede utilizar una trayectoria
                    de referencia incluida dentro de la aplicación.
                </li>
                <li>
                    Los escenarios personalizados pueden responder a modificaciones
                    realizadas por el usuario.
                </li>
                <li>
                    Se aprovechan las capacidades de propagación y cálculo orbital
                    proporcionadas por Orekit.
                </li>
                <li>
                    La interfaz, telemetría, mapa y reportes utilizan un mismo modelo
                    interno de trayectoria.
                </li>
                <li>
                    Se combina la referencia del escenario nominal con la flexibilidad
                    necesaria para realizar simulaciones personalizadas.
                </li>
            </ul>

            <b>Consecuencias negativas y compromisos:</b>
            <ul>
                <li>
                    Deben mantenerse y probarse dos mecanismos diferentes para obtener
                    las trayectorias.
                </li>
                <li>
                    El sistema debe determinar correctamente cuándo una configuración
                    corresponde al perfil nominal de Artemis II.
                </li>
                <li>
                    Las trayectorias personalizadas dependen de los modelos de fuerza
                    y simplificaciones configurados en el propagador numérico.
                </li>
                <li>
                    Los resultados personalizados no necesariamente representan con
                    exactitud todos los elementos de una misión espacial real.
                </li>
            </ul>
        </td>
    </tr>
</table>

<br>
<hr>
<br>

<h3>
    ADR-002 — Separación entre el cálculo orbital y la reproducción gráfica
</h3>

<table>
    <tr>
        <th>Campo</th>
        <th>Contenido</th>
    </tr>

    <tr>
        <td><b>Título y estado</b></td>
        <td>
            <b>ADR-002 — Separación entre el cálculo orbital y la reproducción gráfica.</b>
            <br><br>
            <b>Estado:</b> Aceptada.
        </td>
    </tr>

    <tr>
        <td><b>Contexto</b></td>
        <td>
            La generación de una trayectoria orbital mediante Orekit puede requerir
            una cantidad considerable de cálculos antes de disponer de todos los estados
            necesarios para representar la misión.
            <br><br>

            JavaFX utiliza un hilo principal para procesar y actualizar la interfaz
            gráfica. Ejecutar directamente la propagación orbital dentro de ese hilo
            podría provocar que la aplicación dejara de responder durante el cálculo.
            <br><br>

            Además, AETHER posee controles de reproducción que permiten iniciar,
            pausar, reanudar y modificar la velocidad con la que el usuario observa
            el desarrollo de la misión.
            <br><br>

            La velocidad de reproducción pertenece a la capa de presentación y no debe
            cambiar la trayectoria física calculada.
            <br><br>

            Para separar ambas responsabilidades, la implementación utiliza principalmente
            las clases <code>HelloController</code>, <code>MissionSimulator</code>,
            <code>TrajectoryPlayback</code> y <code>SimulationListener</code>.
        </td>
    </tr>

    <tr>
        <td><b>Decisión</b></td>
        <td>
            Se decide separar el <b>cálculo físico de la trayectoria</b> de su
            <b>reproducción gráfica</b>.
            <br><br>

            La clase <code>HelloController</code> crea un hilo independiente llamado
            <code>aether-orekit-precalculation</code>, dentro del cual se ejecuta
            <code>MissionSimulator.prepareTrajectory()</code>.
            <br><br>

            De esta manera, el cálculo de la trayectoria se realiza fuera del hilo
            gráfico principal de JavaFX.
            <br><br>

            Una vez finalizado el cálculo, se utiliza
            <code>Platform.runLater()</code> para regresar al hilo JavaFX y comenzar
            la reproducción de la trayectoria.
            <br><br>

            La clase <code>TrajectoryPlayback</code>, basada en
            <code>AnimationTimer</code>, utiliza los estados previamente calculados
            para representar continuamente el desarrollo de la misión.
            <br><br>

            Cuando el tiempo solicitado se encuentra entre dos estados almacenados,
            <code>TrajectoryPlayback</code> realiza una interpolación entre ambos
            estados para obtener una representación continua.
            <br><br>

            Las acciones de pausa, reanudación y cambio de velocidad se aplican sobre
            la reproducción. Estas acciones no vuelven a ejecutar la propagación
            orbital ni modifican la trayectoria física.
            <br><br>

            Adicionalmente, <code>SimulationListener</code> funciona como contrato
            para comunicar eventos entre el motor de simulación y los componentes
            encargados de presentar los resultados.
        </td>
    </tr>

    <tr>
        <td><b>Alternativas consideradas</b></td>
        <td>
            <b>1. Ejecutar la propagación orbital directamente en el hilo de JavaFX:</b>
            <br>
            Esta alternativa fue descartada porque una operación de cálculo prolongada
            podría bloquear el hilo de la interfaz y hacer que la aplicación dejara
            temporalmente de responder.
            <br><br>

            <b>2. Calcular y mostrar cada estado orbital al mismo tiempo:</b>
            <br>
            Esta alternativa aumentaría el acoplamiento entre la velocidad del cálculo
            físico y la velocidad de representación de la interfaz.
            <br><br>

            <b>3. Recalcular la trayectoria al modificar la escala de tiempo:</b>
            <br>
            Esta alternativa fue descartada porque la escala de reproducción solamente
            define qué tan rápido se observa la misión. No representa un cambio de los
            parámetros físicos de la trayectoria y, por lo tanto, no requiere una nueva
            propagación.
            <br><br>

            <b>4. Utilizar exclusivamente pausas mediante <code>Thread.sleep()</code>
            para controlar la animación:</b>
            <br>
            El proyecto conserva un mecanismo de este tipo para compatibilidad con
            determinadas pruebas sin JavaFX, pero no se utiliza como mecanismo principal
            de reproducción de la interfaz porque acoplaría el avance visual al recorrido
            directo de las muestras calculadas.
        </td>
    </tr>

    <tr>
        <td><b>Consecuencias</b></td>
        <td>
            <b>Consecuencias positivas:</b>
            <ul>
                <li>
                    La interfaz gráfica no necesita realizar directamente los cálculos
                    de propagación orbital.
                </li>
                <li>
                    Se reduce el riesgo de bloquear el hilo principal de JavaFX.
                </li>
                <li>
                    La velocidad de reproducción no modifica los resultados físicos
                    de la misión.
                </li>
                <li>
                    La simulación puede pausarse y reanudarse sin volver a calcular
                    la trayectoria.
                </li>
                <li>
                    La interpolación permite representar de forma continua estados
                    ubicados entre las muestras calculadas.
                </li>
                <li>
                    Una trayectoria precalculada puede ser utilizada por la telemetría,
                    el mapa y otros componentes de la aplicación.
                </li>
                <li>
                    <code>SimulationListener</code> disminuye el acoplamiento directo
                    entre el motor de simulación y la interfaz.
                </li>
            </ul>

            <b>Consecuencias negativas y compromisos:</b>
            <ul>
                <li>
                    Antes de comenzar la reproducción existe un período de preparación
                    mientras se obtiene la trayectoria.
                </li>
                <li>
                    Los estados de la trayectoria deben mantenerse disponibles durante
                    la reproducción.
                </li>
                <li>
                    Es necesario coordinar correctamente el hilo de precálculo con
                    el hilo principal de JavaFX.
                </li>
                <li>
                    La separación introduce componentes adicionales para gestionar
                    cálculo, reproducción y comunicación de eventos.
                </li>
            </ul>
        </td>
    </tr>
</table>

<br>
<hr>
<br>

<h3>
    ADR-003 — Abstracción de persistencia con MySQL y repositorio de respaldo
</h3>

<table>
    <tr>
        <th>Campo</th>
        <th>Contenido</th>
    </tr>

    <tr>
        <td><b>Título y estado</b></td>
        <td>
            <b>ADR-003 — Abstracción de persistencia con MySQL y repositorio de respaldo.</b>
            <br><br>
            <b>Estado:</b> Aceptada.
        </td>
    </tr>

    <tr>
        <td><b>Contexto</b></td>
        <td>
            AETHER necesita almacenar información relacionada con las ejecuciones
            realizadas por el usuario.
            <br><br>

            Entre la información manejada por el sistema se encuentran cálculos orbitales,
            eventos de misión, configuraciones utilizadas y referencias de reportes
            generados.
            <br><br>

            MySQL permite almacenar esta información de manera persistente. Sin embargo,
            la ejecución principal del simulador no debe quedar completamente bloqueada
            cuando la base de datos no esté configurada, el servidor no responda,
            existan problemas con las credenciales o se produzca algún error durante
            la inicialización.
            <br><br>

            Acoplar directamente la interfaz gráfica con JDBC y MySQL también dificultaría
            el mantenimiento y aumentaría la dependencia entre la presentación y la
            infraestructura de almacenamiento.
            <br><br>

            Para resolver esta situación, la implementación utiliza
            <code>AetherRepository</code>, <code>RepositoryFactory</code>,
            <code>MySqlAetherRepository</code>,
            <code>PendingDatabaseRepository</code> y
            <code>DatabaseConfig</code>.
        </td>
    </tr>

    <tr>
        <td><b>Decisión</b></td>
        <td>
            Se adopta una <b>abstracción de persistencia mediante el patrón Repository</b>.
            <br><br>

            La interfaz <code>AetherRepository</code> define un contrato común para
            almacenar y consultar cálculos, eventos, reportes y configuraciones de misión.
            <br><br>

            De esta manera, los demás componentes del sistema trabajan con
            <code>AetherRepository</code> y no necesitan conocer directamente los detalles
            de JDBC o de la implementación de MySQL.
            <br><br>

            La clase <code>RepositoryFactory</code> es responsable de seleccionar
            la implementación utilizada durante la ejecución.
            <br><br>

            Si MySQL se encuentra habilitado y disponible, el sistema crea un
            <code>MySqlAetherRepository</code>.
            <br><br>

            Si la base de datos no está habilitada, no responde o se produce una
            excepción durante la inicialización, la fábrica utiliza
            <code>PendingDatabaseRepository</code>.
            <br><br>

            <code>PendingDatabaseRepository</code> mantiene temporalmente en memoria
            los cálculos, eventos, configuraciones y referencias de reportes generados,
            permitiendo continuar utilizando las principales funciones del simulador
            durante la sesión.
            <br><br>

            La configuración de MySQL se centraliza en <code>DatabaseConfig</code>,
            que puede obtener información como URL, usuario y contraseña desde
            variables de entorno o propiedades de la JVM.
        </td>
    </tr>

    <tr>
        <td><b>Alternativas consideradas</b></td>
        <td>
            <b>1. Acceder directamente a MySQL desde <code>HelloController</code>:</b>
            <br>
            Esta alternativa fue descartada porque mezclaría las responsabilidades
            de interfaz, lógica de aplicación, persistencia y manejo de conexiones.
            Esto aumentaría el acoplamiento y dificultaría futuras modificaciones.
            <br><br>

            <b>2. Hacer obligatoria la disponibilidad de MySQL para ejecutar AETHER:</b>
            <br>
            Esta alternativa fue descartada porque una falla de infraestructura
            impediría utilizar funcionalidades como la simulación, telemetría o
            visualización, aunque estas no dependan necesariamente de una base de datos.
            <br><br>

            <b>3. Utilizar solamente almacenamiento en memoria:</b>
            <br>
            Esta alternativa permitiría simplificar la aplicación, pero toda la
            información almacenada desaparecería al terminar la ejecución.
            <br><br>

            <b>4. Utilizar únicamente archivos locales para la persistencia:</b>
            <br>
            Esta alternativa reduciría la dependencia de un servidor de base de datos,
            pero no proporcionaría la misma organización estructurada y capacidad
            de consulta que una base de datos relacional utilizada para almacenar
            distintos tipos de registros.
        </td>
    </tr>

    <tr>
        <td><b>Consecuencias</b></td>
        <td>
            <b>Consecuencias positivas:</b>
            <ul>
                <li>
                    La lógica principal del sistema no depende directamente de JDBC
                    ni de una implementación específica de MySQL.
                </li>
                <li>
                    AETHER puede continuar funcionando cuando la base de datos no está
                    disponible.
                </li>
                <li>
                    La implementación de persistencia puede sustituirse con menor impacto
                    sobre los demás componentes.
                </li>
                <li>
                    Existe una separación más clara entre interfaz, simulación
                    y persistencia.
                </li>
                <li>
                    Los errores de conexión a MySQL no necesariamente impiden utilizar
                    las funciones principales del simulador.
                </li>
                <li>
                    La configuración de la base de datos se encuentra centralizada
                    en <code>DatabaseConfig</code>.
                </li>
                <li>
                    Las credenciales pueden obtenerse mediante variables de entorno
                    en lugar de quedar directamente escritas dentro de las clases
                    que utilizan el repositorio.
                </li>
            </ul>

            <b>Consecuencias negativas y compromisos:</b>
            <ul>
                <li>
                    La información almacenada únicamente por
                    <code>PendingDatabaseRepository</code> se conserva solamente
                    durante la ejecución actual.
                </li>
                <li>
                    Al cerrar la aplicación, los datos almacenados exclusivamente
                    en memoria pueden perderse.
                </li>
                <li>
                    El proyecto debe mantener diferentes implementaciones del mismo
                    contrato de persistencia.
                </li>
                <li>
                    Es necesario identificar claramente si la aplicación está utilizando
                    MySQL o el repositorio temporal.
                </li>
                <li>
                    El mecanismo de respaldo no proporciona la misma durabilidad que
                    una base de datos persistente.
                </li>
            </ul>
        </td>
    </tr>
</table>

<br>
<hr>

<p align="center">
    <b>Fin del Registro de Decisiones de Arquitectura</b>
</p>
