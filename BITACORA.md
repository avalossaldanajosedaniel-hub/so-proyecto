s01 bloque 1: entorno y repositorio listos
Dominio elegido Tienda de equipos de redes
# Bitácora de Trabajo

## Semana 1

### Avance del proyecto
Ya tenia instalado una maquina virtual con linux, esperare a saber cuales son los dominios para elegir ya que llegue trde a esa clase






### Antes de la clase, responde con tus palabras:

Cuál es tu dominio.ESTACIONAMIENTO
Por qué tu servidor no puede tocar el disco directamente. Necesita llamar al SO mediante Syscalls                   
Un ejemplo tuyo de cada capa. PROGRAMA DE USUARIO(JAVA),SERVICIO QUE LE PIDIO EL SO(El SO hace un llamado con Syscall al programa dodne se consulta, que puede consultar,un espacio libre y devuelve el resultado), HARDWARE QUE TERMINA
MOVIENDOSE(DISCO DURO, CPU, LECTORES NFC)

### ANTES DE LA CLASE
| PID | PPID | STAT | Qué creo que es y por qué está en ese estado |
|---|---|---|---|
| 80862 | 80851 | Ss | Es la shell de comandos Zsh (`/usr/bin/zsh`). Está en estado **S** (Sleeping) a la espera de que introduzcas un nuevo comando, y tiene el modificador **s** por ser el líder de la sesión interactiva de la terminal. |
| 98575 | 80862 | R+ | Es el comando `ps` ejecutado para listar procesos. Está en estado **R** (Running) porque es la tarea que se está procesando activamente en la CPU en este preciso instante, y **+** por ejecutarse en primer plano (*foreground*). |
| 98576 | 80862 | S+ | Es el comando `head` que limita las líneas de la salida. Se encuentra en estado **S** (Sleeping / Ininterrumpible) esperando datos por la tubería (*pipe*) del comando `ps`, y **+** por estar en primer plano. |

### Preguntas sobre la raíz del árbol

1. El proceso con PID 1, ¿quién es y qué PPID tiene?
El proceso con PID 1 es `/sbin/init` (o `systemd`). Su columna **PPID** muestra el valor **0**. El número **0** representa al proceso kernel especial (a menudo llamado `swapper` o proceso del sistema), lo que significa que el proceso 1 no fue creado por otro proceso convencional de espacio de usuario.

2. Por qué ese proceso es el único que no pudo ser creado por otro proceso?
Durante el arranque del equipo, el kernel de Linux toma el control del hardware y se inicializa en memoria. Al terminar, el kernel monta el sistema de archivos raíz y ejecuta directamente el binario `/sbin/init` (PID 1). Como no existía ningún otro proceso previo en el espacio de usuario ejecutando una llamada `fork()`, el propio kernel lo engendra de forma directa como el origen de todo el árbol de procesos. 

holaaaaa

PID, PPID y proceso padre:
PID del servidor: 109395.
PPID: 80862.
Proceso padre: La shell interactiva de comandos Zsh (/usr/bin/zsh) de la primera terminal (pts/0), la cual actuó como proceso padre al invocar y ejecutar el comando java ServidorPedidos.

Estado del servidor (STAT):
Estado encontrado: S / Sl (Interruptible Sleep / Multi-threaded).
Por qué ese estado: El servidor pasa casi todo el tiempo bloqueado dentro de la instrucción Thread.sleep(5000). En este estado de reposo o suspensión interruptible, no requiere tiempo activo de CPU y permanece dormido a la espera de un evento del temporizador (los 5 segundos) o de recibir pedidos/señales de E/S.

Porcentaje de CPU (%CPU):
Consumo observado: 0.0%.
Por qué es el número correcto: Es el valor esperado porque el proceso no está realizando cómputo ni operaciones matemáticas intensivas en el procesador. Mientras está en la llamada bloqueante sleep(), el planificador (scheduler) del sistema operativo lo remueve de la cola de ejecución activa de la CPU, consumiendo cero ciclos de procesamiento mientras espera.

## AVANCE DEL PROYECTO
Qué es un proceso. UN proceso es un software o programa que se esta ejecutando
Cómo vive tu servidor en el SO.Aquí tienes una versión más natural, corta y directa, redactada como si fuera una respuesta propia de un estudiante en un laboratorio:

1. PID (Process ID): Es el número único que el sistema le asigna al servidor para identificarlo mientras está corriendo (por ejemplo, el 314126).

2. PPID (Padre): Es el PID del proceso que lanzó al servidor. En este caso es la terminal (la shell) donde ejecuté el comando java.

3. Estado (STAT):

S (Sleeping): Es su estado normal. Está dormido/bloqueado esperando que llegue una petición de un cliente, así que no gasta CPU.

R (Running): Solo pasa a este estado en los momentos exactos en los que está procesando un pedido en la CPU.

T (Stopped): Aparece cuando lo pausamos con Ctrl + Z. El sistema congela su ejecución por completo.

La l al lado (como Sl o Tl) solo significa que Java usa varios hilos de ejecución (threads).

4. Consumo:

CPU: Casi 0% la mayor parte del tiempo porque se queda en reposo (esperando conexiones) en lugar de hacer bucles infinitos.

Memoria: Ocupa un bloque fijo de RAM (unos 30-80 MB) solo por tener la máquina virtual de Java (JVM) cargada.


### ANTES DE LA CLASE
Qué es el cambio de contexto:Es el proceso que realiza el servidor cada milisegundos al detener el proceso y le da el CPE a otro proceso y despues de otro milisegundos este se reanundara, al dtenerlo este debe de guardar todo para despues volver como si nada

La tabla de tu servidor dentro de un contenedor:
#### Servidor dentro de un contenedor

| Componente | Tipo de control | ¿Qué hace? | Impacto en el servidor dentro del contenedor |
| :--- | :--- | :--- | :--- |
| **Namespace de PID** | Aislamiento visual de procesos | Oculta la lista global de procesos de la máquina real. | El servidor corre creyendo que es el proceso principal del sistema (**PID 1**). No puede ver, listar ni interferir con otros procesos del host. |
| **Namespace de Red (Net)** | Aislamiento de pila de red | Otorga una interfaz de red, tabla de rutas y puertos independientes. | Tiene sus propias interfaces de red virtuales y sus propios puertos. Puede abrir puertos (ej. 80 o 443) sin conflictos con la máquina host. |
| **Cgroup (256 MB)** | Limitación de recursos de hardware | Define la cuota máxima de memoria RAM utilizable. | El procesador y el sistema limitan el consumo a 256 MB. Si el servidor sobrepasa esta cuota de RAM, el kernel ejecuta el **OOM Killer** y detiene el proceso inmediatamente. |






### Antes de la clase:
Semana 3
 Relación Padre e Hijo en la evidencia (`evidencias/hijo.txt`)
Al analizar los procesos capturados mediante `ps -ef | grep java`:

* **Proceso Padre (`ServidorPedidos`):** Se encuentra ejecutándose bajo el **PID `347571`** (creado desde la terminal con PPID `324317`).
* **Proceso Hijo (`GeneradorPedidos`):** Se ejecuta con su propio **PID `352960`**. 

En la jerarquía de Linux, cuando el Servidor lanza al Generador usando `ProcessBuilder`, el kernel le asigna al proceso hijo el **PPID (Parent Process ID)** correspondiente al PID del Servidor. Esto establece formalmente la relación de dependencia entre ambos procesos en el árbol del sistema operativo.

 ¿Qué pasaría si quitaras el `waitFor()`?
Si se elimina la línea `hijo.waitFor()`, el proceso padre (`ServidorPedidos`) no esperará a que el hijo termine de procesar las 2,000,000 de iteraciones. El padre continuará ejecutando sus líneas restantes, imprimirá *"Servidor (Padre) finalizando"* y se cerrará de inmediato.

Por su parte, el hijo (`GeneradorPedidos`) seguirá corriendo en segundo plano hasta finalizar su tarea. Al quedarse sin un proceso padre activo, el hijo se convierte técnicamente en un **proceso huérfano** (*orphan*), por lo que es adoptado automáticamente por el proceso principal del sistema (`systemd` / PID 1) hasta que termine de ejecutarse.

 ¿Por qué el hijo no puede modificar una variable del servidor?
Esto se debe al **aislamiento del espacio de memoria virtual** del sistema operativo (concepto visto en el Bloque 1).

Cuando un proceso padre crea un proceso hijo, el kernel de Linux le asigna al hijo su propia tabla de páginas de memoria totalmente independiente. Aunque ambos compartan código similar, la memoria física subyacente está segmentada y protegida. Cualquier modificación que el hijo haga sobre una variable solo afectará a su copia local en su propio espacio de usuario; la Unidad de Gestión de Memoria (MMU) impide estrictamente que un proceso escriba en la memoria asignada a otro proceso.



### Semana 5
## BLOQUE1
Tabla de intercalado
# Condición de Carrera: Venta de Equipos de Red

| Instante | Vendedor A (Switch) | Vendedor B (Switch) | Stock en Memoria### Antes de la clase

#### 1. Tabla de intercalado con sobreventa (Dominio: Tienda de Equipos de Red)
* **Stock inicial en memoria:** `3` unidades de NanoStation.
* **Pedidos concurrentes:** Hilo A (Cliente/Vendedor 1 pide 2 unidades) e Hilo B (Cliente/Vendedor 2 pide 2 unidades).

| Instante | Hilo A (Pedido de 2 Nanos) | Hilo B (Pedido de 2 Nanos) | Stock en Memoria |
| :---: | :--- | :--- | :---: |
| **1** | Consulta si `3 >= 2` $\rightarrow$ **Verdadero** | | **3** |
| **2** | | Consulta si `3 >= 2` $\rightarrow$ **Verdadero** | **3** |
| **3** | Descuenta `3 - 2` $\rightarrow$ Guarda **1** en su registro | | **3** |
| **4** | | Descuenta `3 - 2` $\rightarrow$ Guarda **1** en su registro | **3** |
| **5** | Confirma la venta A | | **1** |
| **6** | | Confirma la venta B | **1** *(¡Número imposible!)* |

* **Número imposible resultante:** El stock final en memoria queda en **`1`**.
* **Por qué es imposible:** Había físicamente 3 antenas Nano en el almacén. Se despacharon dos pedidos de 2 unidades cada uno (lo que significa que salieron 4 antenas físicamente del local), pero al restarse de forma intercalada partiendo de una lectura vieja, el sistema matemáticamente sustrajo dos veces sobre un stock de 3, dejando un inventario fantasma de 1 unidad que en realidad nunca existió.

#### 2. Sección crítica
* **Dónde empieza y dónde termina:** La sección crítica empieza estrictamente en el momento en el que el servidor realiza la consulta o validación de la existencia y termina justo al finalizar la operación de descuento del inventario.
* **Justificación:** Si proteges únicamente el descuento dejando fuera la consulta, el error persiste porque ambos hilos habrán pasado la validación previa al mismo tiempo usando información vieja. La consulta y el descuento forman un bloque indivisible: si un hilo evalúa el stock, ningún otro hilo debe poder consultar ni modificar la existencia hasta que el primer hilo termine de descontar por completo.

#### 3. El costo en el negocio
* **Qué pasa si el inventario dice que tienes stock (ej. 3) y no tienes ninguno:** Genera una sobreventa crítica, lo que se traduce en clientes furiosos, cancelación de instalaciones de red ya programadas, pérdida de reputación para la tienda de equipos y posibles devoluciones de dinero con penalización.
* **Qué pasa si el inventario dice que tienes 0 y en realidad tienes 3:** Genera pérdida de ventas y clientes frustrados, ya que la tienda web les dice que el producto está agotado cuando físicamente sí hay mercadería en la bodega, dejando dinero estancado sin rotación. |
| :---: | :--- | :--- | :---: |
| **1** | LEE stock de Nano -> **5** | | **5** |
| **2** | | LEE stock de Nano -> **5** | **5** |
| **3** | RESTA 1 -> **4** (en su registro local) | | **5** |
| **4** | | RESTA 1 -> **4** (en su registro local) | **5** |
| **5** | ESCRIBE stock -> **4** | | **4** |
| **6** | | ESCRIBE stock -> **4** | **4** *(Error crítico: Sobreventa)* |
##BLOQUE 2
### Antes de la clase

#### Tabla de corridas experimentales (Tienda de Equipos de Red)

| Corrida | Confirmadas | Rechazadas | Existencia final | Cuadra? |
| :---: | :---: | :---: | :---: | :---: |
| **1** | 10 | 0 | 9 | `false` |
| **2** | 10 | 0 | 8 | `false` |
| **3** | 10 | 0 | 7 | `false` |
| **4** | 10 | 0 | 8 | `false` |
| **5** | 10 | 0 | 7 | `false` |

#### Análisis de resultados

1. **Síntoma obtenido y corrida más clara:** 
   El síntoma que salió en todas las ejecuciones fue la **sobreventa por pérdida de actualizaciones debido a condiciones de carrera** (los hilos sobrescriben el stock de memoria con valores viejos). Se ve más claro en la **Corrida 3 y la Corrida 5**, donde a pesar de haberse confirmado 10 ventas exitosas de antenas NanoStation, la existencia final en el sistema quedó en `7` unidades en lugar de `0`, reflejando un desfase masivo por datos perdidos.

2. **Configuración necesaria para que fallara:** 
   Se utilizaron **10 cajeros (hilos concurrentes) y una existencia inicial de 10 unidades** de antenas NanoStation, con una pequeña pausa simulada (`Thread.sleep(1)`) dentro de la sección crítica. El error **falló desde la primera corrida** y en todas las siguientes de forma constante, demostrando que la ausencia de exclusión mutua corrompe el inventario de inmediato bajo concurrencia.

3. **Costo en el negocio (Peor corrida):** 
   En la **Corrida 3 y Corrida 5** (donde la existencia final quedó en 7 en lugar de 0), el sistema registró falsamente que todavía quedaban 7 antenas NanoStation disponibles en el almacén cuando en realidad el inventario físico ya estaba totalmente agotado (se vendieron las 10 piezas). Si un cliente nuevo entra a la tienda web y compra esas 7 "existencias fantasma", la tienda incurrirá en una **sobreventa crítica de 7 equipos de red**, lo que se traduciría en incumplimiento de contratos de instalación, pérdida de confianza y la obligación de reembolsar o absorber el costo de reabastecer de emergencia equipo escaso para satisfacer a los clientes afectados.


##semana 6
##BLOQUE 1
##### 1. Por qué la bandera boolean ocupado no funciona
El intento de usar una variable booleana como candado no garantiza la exclusión mutua porque la verificación y la actualización de la variable no ocurren como una operación atómica. Entre la lectura del `while (ocupado)` y la asignación `ocupado = true`, existe una ventana de tiempo donde el SO puede intercalar la ejecución de dos hilos.
##### 2. Respuesta a la pregunta de la semana 2 y relación con synchronized
En la semana 2 se observó que un ciclo vacío consumía ~100% de CPU (Estado R - Running), mientras que un `Thread.sleep` consumía ~0% (Estado S - Sleeping/Blocked).
##### 3.Desicion de mecanismo
1.Usare la palabra clave `synchronized` en el metodo `vender` de `GestorInventario.java`. Esto garantiza exclusión mutua estricta de un hilo a la vez para evitar condiciones de carrera en el stock. Ademas, libera automáticamente el monitor en caso de excepciones, evitando bloqueos permanentes. 
2.semaphore,Se usara un `Semaphore(N)` cuando se requiera controlar el acceso a un numero limitado de recursos concurrentes, como por ejemplo limitar a 4 las conexiones simultaneas o procesos de atencion de solicitudes en el servidor. Un `Semaphore(4)` permitira ingresar a los primeros 4 hilos en paralelo y bloqueara al 5to hasta que uno se libere.
##BLOQUE 2

##### Tabla de Comparación (Antes vs Después)

| Corrida | Antes: confirmadas | Antes: final | Después: confirmadas | Después: final | ¿CUADRA? |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | 18 (Varía) | 0 (Error) | 10 | 0 | **true** |
| **2** | 25 (Varía) | -15 (Inconsistente) | 10 | 0 | **true** |
| **3** | 12 (Varía) | -2 (Inconsistente) | 10 | 0 | **true** |
| **4** | 30 (Varía) | -20 (Inconsistente) | 10 | 0 | **true** |
| **5** | 19 (Varía) | -9 (Inconsistente) | 10 | 0 | **true** |

---

##### Delimitación de la Sección Crítica
La sección crítica en el método `vender` comprende **desde la consulta del mapa de existencias hasta la actualización del descuento y los contadores**:

```java
public synchronized boolean vender(String producto, int cantidad) {
    Integer disponible = existencias.get(producto); // Inicio: Lectura del estado
    if (disponible == null) return false;

    if (disponible >= cantidad) {
        try { Thread.sleep(1); } catch (InterruptedException e) { }

        existencias.put(producto, disponible - cantidad); // Escritura/Actualización
        ventasConfirmadas++;
        return true;
    }

    ventasRechazadas++;
    return false;
} // Fin: Liberación implícita del candado del objeto
