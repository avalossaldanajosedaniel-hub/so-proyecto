# Bitácora de Trabajo
**Dominio elegido:** Tienda de Equipos de Redes

---

## Semana 1

### Avance del proyecto
Ya tenía instalada una máquina virtual con Linux. Esperaré a saber cuáles son los dominios para elegir ya que llegué tarde a esa clase.

### Antes de la clase, responde con tus palabras:
* **Cuál es tu dominio:** Tienda de Equipos de Redes
* **Por qué tu servidor no puede tocar el disco directamente:** Necesita llamar al SO mediante Syscalls.
* **Un ejemplo tuyo de cada capa:**
  1. *Programa de usuario* (Java).
  2. *Servicio que le pidió al SO* (El SO hace un llamado con Syscall al programa donde se consulta un espacio libre y devuelve el resultado).
  3. *Hardware que termina moviéndose* (Disco duro, CPU, lectores NFC).

### ANTES DE LA CLASE: Análisis de Procesos (`ps`)
| PID | PPID | STAT | Qué creo que es y por qué está en ese estado |
|---|---|---|---|
| 80862 | 80851 | Ss | Es la shell de comandos Zsh (`/usr/bin/zsh`). Está en estado **S** (Sleeping) a la espera de un nuevo comando, y tiene el modificador **s** por ser el líder de la sesión interactiva de la terminal. |
| 98575 | 80862 | R+ | Es el comando `ps` ejecutado para listar procesos. Está en estado **R** (Running) porque es la tarea que se está procesando activamente en la CPU en este preciso instante, y **+** por ejecutarse en primer plano (*foreground*). |
| 98576 | 80862 | S+ | Es el comando `head` que limita las líneas de la salida. Se encuentra en estado **S** (Sleeping) esperando datos por la tubería (*pipe*) del comando `ps`, y **+** por estar en primer plano. |

### Preguntas sobre la raíz del árbol
1. **El proceso con PID 1, ¿quién es y qué PPID tiene?**
   El proceso con PID 1 es `/sbin/init` (o `systemd`). Su columna **PPID** muestra el valor **0**. El número **0** representa al proceso kernel especial (a menudo llamado `swapper` o proceso del sistema), lo que significa que el proceso 1 no fue creado por otro proceso convencional de espacio de usuario.
2. **Por qué ese proceso es el único que no pudo ser creado por otro proceso:**
   Durante el arranque del equipo, el kernel de Linux toma el control del hardware y se inicializa en memoria. Al terminar, el kernel monta el sistema de archivos raíz y ejecuta directamente el binario `/sbin/init` (PID 1). Como no existía ningún otro proceso previo en el espacio de usuario ejecutando una llamada `fork()`, el propio kernel lo engendra de forma directa como el origen de todo el árbol de procesos.

### Comportamiento del Servidor en el SO
* **PID, PPID y proceso padre:**
  * PID del servidor: `109395`.
  * PPID: `80862`.
  * Proceso padre: La shell interactiva de comandos Zsh (`/usr/bin/zsh`) de la primera terminal (`pts/0`), la cual actuó como proceso padre al invocar y ejecutar el comando `java ServidorPedidos`.
* **Estado del servidor (STAT):**
  * Estado encontrado: `S` / `Sl` (Interruptible Sleep / Multi-threaded).
  * Por qué ese estado: El servidor pasa casi todo el tiempo bloqueado dentro de la instrucción `Thread.sleep(5000)`. En este estado de reposo, no requiere tiempo activo de CPU y permanece dormido a la espera de un evento del temporizador o de recibir pedidos/señales de E/S.
* **Porcentaje de CPU (%CPU):**
  * Consumo observado: `0.0%`.
  * Por qué es el número correcto: Es el valor esperado porque el proceso no está realizando cómputo ni operaciones matemáticas intensivas en el procesador. El planificador (*scheduler*) lo remueve de la cola de ejecución activa mientras espera en el `sleep()`.

---

## Semana 2

### AVANCE DEL PROYECTO
* **Qué es un proceso:** Un proceso es un software o programa que se está ejecutando en el sistema operativo.
* **Cómo vive tu servidor en el SO:**
  1. **PID (Process ID):** Número único que el sistema le asigna al servidor mientras corre.
  2. **PPID (Padre):** Es el PID del proceso que lanzó al servidor (la terminal/shell donde se ejecutó Java).
  3. **Estado (STAT):**
     * `S` (Sleeping): Su estado normal, esperando peticiones sin gastar CPU.
     * `R` (Running): Cuando está procesando activamente un pedido en la CPU.
     * `T` (Stopped): Cuando se pausa con `Ctrl + Z`. La `l` al lado (como `Sl`) indica uso de múltiples hilos (*threads*).
  4. **Consumo:**
     * CPU: Casi 0% la mayor parte del tiempo por el estado de reposo.
     * Memoria: Ocupa un bloque fijo de RAM (unos 30-80 MB) por la máquina virtual de Java (JVM).

### ANTES DE LA CLASE: Cambio de Contexto y Contenedores
* **Qué es el cambio de contexto:** Es el proceso que realiza el sistema operativo cada milisegundos al detener un proceso para darle la CPU a otro, guardando el estado actual para poder reanudarlo después sin pérdida de información.

#### Servidor dentro de un contenedor
| Componente | Tipo de control | ¿Qué hace? | Impacto en el servidor dentro del contenedor |
|---|---|---|---|
| **Namespace de PID** | Aislamiento visual de procesos | Oculta la lista global de procesos de la máquina real. | El servidor corre creyendo que es el proceso principal del sistema (**PID 1**). No puede ver ni interferir con otros procesos del host. |
| **Namespace de Red (Net)** | Aislamiento de pila de red | Otorga una interfaz de red, tabla de rutas y puertos independientes. | Tiene sus propias interfaces de red virtuales y puertos libres para abrir sin conflictos con la máquina host. |
| **Cgroup (256 MB)** | Limitación de recursos de hardware | Define la cuota máxima de memoria RAM utilizable. | El sistema limita el consumo a 256 MB. Si el servidor sobrepasa esta cuota, el kernel ejecuta el **OOM Killer** y detiene el proceso inmediatamente. |

---

## Semana 3

### ANTES DE LA CLASE: Procesos Padre e Hijo
* **Relación Padre e Hijo (`evidencias/hijo.txt`):**
  Al analizar los procesos capturados mediante `ps -ef | grep java`:
  * **Proceso Padre (`ServidorPedidos`):** Se ejecuta bajo el **PID `347571`** (creado desde la terminal con PPID `324317`).
  * **Proceso Hijo (`GeneradorPedidos`):** Se ejecuta con su propio **PID `352960`**.
  En la jerarquía de Linux, cuando el Servidor lanza al Generador usando `ProcessBuilder`, el kernel le asigna al proceso hijo el **PPID** correspondiente al PID del Servidor.

* **¿Qué pasaría si quitaras el `waitFor()`?**
  Si se elimina la línea `hijo.waitFor()`, el proceso padre (`ServidorPedidos`) no esperará a que el hijo termine. El padre continuará, imprimirá su mensaje de cierre y terminará de inmediato. Por su parte, el hijo (`GeneradorPedidos`) seguirá corriendo en segundo plano y, al quedarse sin un proceso padre activo, se convertirá en un **proceso huérfano** (*orphan*), siendo adoptado automáticamente por `systemd` (PID 1).

* **¿Por qué el hijo no puede modificar una variable del servidor?**
  Esto se debe al **aislamiento del espacio de memoria virtual** del sistema operativo. Cuando un proceso padre crea un proceso hijo, el kernel le asigna una tabla de páginas de memoria totalmente independiente. La Unidad de Gestión de Memoria (MMU) impide estrictamente que un proceso escriba en la memoria asignada a otro proceso.

---

## Semana 5

### Bloque 1: Condición de Carrera en Tienda de Equipos de Red

#### 1. Tabla de intercalado con sobreventa
* **Stock inicial en memoria:** `3` unidades de antena NanoStation.
* **Pedidos concurrentes:** Hilo A (pide 2 unidades) e Hilo B (pide 2 unidades).

| Instante | Hilo A (Pedido de 2 Nanos) | Hilo B (Pedido de 2 Nanos) | Stock en Memoria |
|---|---|---|---|
| **1** | Consulta si `3 >= 2` -> **Verdadero** | | **3** |
| **2** | | Consulta si `3 >= 2` -> **Verdadero** | **3** |
| **3** | Descuenta `3 - 2` -> Guarda **1** en su registro | | **3** |
| **4** | | Descuenta `3 - 2` -> Guarda **1** en su registro | **3** |
| **5** | Confirma la venta A | | **1** |
| **6** | | Confirma la venta B | **1** *(¡Número imposible!)* |

* **Número imposible resultante:** El stock final en memoria queda en **`1`**.
* **Por qué es imposible:** Había físicamente 3 antenas Nano en el almacén y salieron 4 en total por las dos ventas, pero el inventario matemático final reflejó 1 unidad fantasma que nunca existió.

#### 2. Sección crítica
* **Dónde empieza y dónde termina:** Empieza estrictamente en el momento en el que el servidor realiza la consulta o validación de la existencia y termina justo al finalizar la operación de descuento del inventario. La consulta y el descuento forman un bloque indivisible.

#### 3. El costo en el negocio
* **Si el inventario dice que hay stock (ej. 3) y no hay ninguno:** Genera una sobreventa crítica, clientes furiosos, cancelación de instalaciones de red ya programadas y pérdida de reputación.
* **Si el inventario dice que hay 0 y en realidad hay 3:** Genera pérdida de ventas y clientes frustrados porque la tienda web rechaza pedidos cuando físicamente sí hay mercancía en bodega.

---

### Bloque 2: Diagnóstico Experimental de Concurrencia

#### Tabla de corridas experimentales
| Corrida | Confirmadas | Rechazadas | Existencia final | Cuadra? |
|---|---|---|---|---|
| **1** | 10 | 0 | 9 | `false` |
| **2** | 10 | 0 | 8 | `false` |
| **3** | 10 | 0 | 7 | `false` |
| **4** | 10 | 0 | 8 | `false` |
| **5** | 10 | 0 | 7 | `false` |

#### Análisis de resultados
1. **Síntoma obtenido:** Sobreventa por pérdida de actualizaciones debido a condiciones de carrera. Se observa con mayor claridad en la **Corrida 3 y 5**, donde a pesar de confirmarse 10 ventas, la existencia final quedó en `7` en lugar de `0`.
2. **Costo en el negocio:** Incumplimiento de contratos de instalación, pérdida de confianza y la obligación de reembolsar o reabastecer de emergencia equipo escaso.

---

## Semana 6

### Bloque 1: Mecanismos de Sincronización
1. **Por qué la bandera `boolean ocupado` no funciona:**
   El intento de usar una variable booleana como candado no garantiza exclusión mutua porque la verificación y la actualización no ocurren como una operación atómica. Entre la lectura del `while (ocupado)` y la asignación `ocupado = true`, el SO puede intercalar hilos.
2. **Relación con la semana 2:**
   Un ciclo vacío consume ~100% de CPU (Estado R), mientras que un `Thread.sleep` o espera bloqueada consume ~0% (Estado S).
3. **Decisión de mecanismo:**
   * Usaré `synchronized` en el método `vender` de `GestorInventario.java` para garantizar exclusión mutua estricta y liberación automática ante excepciones.
   * Usaré `Semaphore(N)` cuando se requiera controlar el acceso a un número limitado de recursos concurrentes (ej. limitar a 4 las conexiones simultáneas de atención de solicitudes).

---

### Bloque 2: Implementación y Validación de Sincronización

#### Tabla de Comparación (Antes vs Después)
| Corrida | Antes: confirmadas | Antes: final | Después: confirmadas | Después: final | ¿CUADRA? |
|---|---|---|---|---|---|
| **1** | 18 (Varía) | 0 (Error) | 10 | 0 | **true** |
| **2** | 25 (Varía) | -15 (Inconsistente) | 10 | 0 | **true** |
| **3** | 12 (Varía) | -2 (Inconsistente) | 10 | 0 | **true** |
| **4** | 30 (Varía) | -20 (Inconsistente) | 10 | 0 | **true** |
| **5** | 19 (Varía) | -9 (Inconsistente) | 10 | 0 | **true** |

#### Delimitación de la Sección Crítica (`synchronized`)
java
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
} 

# Semana 7 - Bloqueos y Sincronización (U2)

## Bloque 1: Las cuatro condiciones de Coffman y Deadlock

### 1. Escenario de Interbloqueo en DanyNetwork
* En el servidor de ventas de videovigilancia (DanyNetwork), se procesan transacciones que impactan existencias en inventario (`GestorInventario` - Candado 1) y registros de auditoría (`RegistroLog` - Candado 2). Si dos hilos solicitan acceso en orden inverso, ocurre un interbloqueo (*deadlock*):
  * **Hilo A:** Adquiere Inventario -> Pide Log (Se bloquea esperando a B).
  * **Hilo B:** Adquiere Log -> Pide Inventario (Se bloquea esperando a A).

### Diagrama de Intercalación Fallida

Hilo A (Pedido NVR)                    Hilo B (Reporte Auditoría)
--------------------                   --------------------------
Toma candado INVENTARIO                Toma candado LOG
   │                                      │
   ▼                                      ▼
Pide candado LOG                       Pide candado INVENTARIO
   │                                      │
   └──► BLOQUEADO (espera a B)            └──► BLOQUEADO (espera a A)

---

## Bloque 2: Diagnóstico de Interbloqueo (jstack)

Fragmento clave extraído de `evidencias/deadlock.txt` que demuestra el interbloqueo real:

"hilo-1" #20 [18671] prio=5 os_prio=0 ... java.lang.Thread.State: BLOCKED (on object monitor)
        at PruebaBloqueo.lambda$main$0(PruebaBloqueo.java:16)
- waiting to lock <0x000000077a4142e0> (a java.lang.Object)
- locked <0x000000077a4142d0> (a java.lang.Object)

"hilo-2" #21 [18672] prio=5 os_prio=0 ... java.lang.Thread.State: BLOCKED (on object monitor)
        at PruebaBloqueo.lambda$main$1(PruebaBloqueo.java:27)
- waiting to lock <0x000000077a4142d0> (a java.lang.Object)
- locked <0x000000077a4142e0> (a java.lang.Object)

---

## Bloque Extra: Comparativa de Prevención de Deadlocks

| Criterio | Orden global de candados | `tryLock` con timeout |
| :--- | :--- | :--- |
| **Previene o detecta?** | Previene (Evita la espera circular desde el diseño). | Evita/Recupera (Reintenta si el recurso está ocupado mediante tiempo límite). |
| **Qué pasa cuando hay competencia** | Los hilos se bloquean ordenadamente esperando su turno sin consumir CPU. | Los hilos sueltan lo que tienen, esperan un lapso y vuelven a competir. |
| **Costo en rendimiento** | Mínimo (bloqueo nativo eficiente a nivel de monitor). | Mayor (genera sobrecarga si hay muchos hilos reintentando constantemente). |
| **Funciona con código de terceros?** | No (requiere control absoluto sobre el orden de los candados). | Sí (permite abortar la espera si una biblioteca externa retiene el recurso). |
| **Qué problema nuevo introduce** | Ninguno (siempre que se respete estrictamente la jerarquía). | Livelock (si los hilos chocan y reintentan en bucle) y complejidad en los bloques `finally`. |

---

# Semana 8

## Antes de la clase:
SIGKILL: no se puede atrapar por que SIGKILL lo ejecuta el kernel sobre el preceso practicamente lo mata, lo deja sin recursos y muere
ORDEN CORRECTO PARA APAGAR:
1. **Bloquear la entrada de nuevas solicitudes:** Cerrar el flujo de lectura (o el socket de red) para rechazar conexiones o pedidos entrantes adicionales.
2. **Concluir las ventas en curso:** Permitir que los hilos activos procesen y validen los pedidos actualmente en memoria sin interrumpir transacciones a la mitad.
3. **Flush de logs y persistencia de estado:** Vaciar los buffers en disco escribiendo todas las transacciones confirmadas en el archivo de auditoría (`RegistroLog`), actualizar los archivos de existencias e inventarios, y cerrar formalmente los descriptores de archivos.
4. **Emisión de informe final y salida:** Imprimir en la consola o log el resumen de ventas procesadas, rechazadas y el estado final del sistema antes de invocar el cierre definitivo del proceso.

TABLA DE MECANISMOS:
| Necesidad | Qué mecanismo uso | Por qué |
| :--- | :--- | :--- |
| **Avisarle a mi servidor que se apague** | Señales (`SIGTERM` / `SIGINT`) | Es una interrupción por software rápida que el kernel entrega al proceso, activando el *shutdown hook* para un cierre ordenado. |
| **Mandarle pedidos desde el generador** | Tuberías anónimas (Pipes `\|`) | Conectan la salida estándar de un proceso padre con la entrada estándar de un proceso hijo en un flujo continuo de bytes en un solo sentido con sincronización gestionada por el kernel. |
| **Que un cajero en otra máquina me mande pedidos** | Sockets de red | Las tuberías están limitadas a procesos emparentados en la misma máquina local; para enviar información a través de la red entre diferentes dispositivos se requieren sockets. |
