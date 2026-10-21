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

### 3. Predicción de una condición de carrera


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

# Avance del proyecto

## 1. ¿Qué es un contenedor y por qué no es una máquina virtual?

Un contenedor es una forma de ejecutar una aplicación de manera aislada de otros programas. Contiene la aplicación y los elementos que necesita para funcionar, como sus bibliotecas y dependencias. No es una máquina virtual porque no necesita ejecutar un sistema operativo completo independiente, sino que comparte el kernel del sistema operativo del equipo anfitrión. Por eso, normalmente los contenedores son más ligeros y rápidos de iniciar que las máquinas virtuales.

## 2. ¿Qué guarda el sistema operativo en el descriptor de un proceso y para qué sirve?

El sistema operativo mantiene información de cada proceso en una estructura llamada descriptor de proceso. En ella guarda datos como:

* El identificador del proceso (`PID`)
* Su estado
* Los registros de la CPU
* La información necesaria para administrar la memoria y los recursos que tiene abiertos

Esta información le permite al sistema operativo controlar la ejecución de los procesos, cambiar entre ellos y administrar los recursos del equipo.

## 3. ¿Qué datos tiene un pedido en mi dominio y por qué los elegí?

Mi proyecto está basado en una farmacia. Cada pedido tiene un identificador, el nombre del producto y la cantidad solicitada. Elegí estos datos porque permiten identificar cada pedido, conocer qué medicamento se necesita y determinar cuántas unidades se deben entregar.

Además, el formato `ID;PRODUCTO;CANTIDAD` facilita guardar los pedidos en archivos de texto y permite que posteriormente el servidor central pueda leerlos y convertirlos nuevamente en objetos de Java.

# Avance realizado

* Creé la clase `Pedido.java` con sus atributos, constructor, métodos getters y método `toString()`.
* Preparé el catálogo de productos en `datos/catalogo.txt`, con ocho productos y existencias bajas para las futuras pruebas del inventario.
* Adapté el generador para producir pedidos que utilicen productos incluidos en el catálogo.
---
# Bitácora — Semana 4: Procesos, hilos y memoria

## Antes de la clase

### 1. Estados de un hilo

En el sistema de pedidos de DanyNetwork, un hilo puede pasar por diferentes estados durante la atención de un pedido:

* **NEW (nuevo):** Se crea el hilo encargado de atender el pedido, pero todavía no se ha iniciado.
* **RUNNABLE (ejecutable):** El hilo se inicia con `start()` y puede estar ejecutándose o esperando tiempo de CPU.
* **BLOCKED (bloqueado):** Puede ocurrir si intenta entrar a una sección sincronizada cuyo bloqueo está ocupado por otro hilo.
* **WAITING (esperando):** Puede ocurrir cuando el hilo espera indefinidamente una acción de otro hilo, por ejemplo, mediante `join()`.
* **TIMED_WAITING (espera temporizada):** Ocurre cuando el hilo ejecuta `Thread.sleep()`, porque debe esperar un tiempo determinado.
* **TERMINATED (terminado):** El hilo acaba de procesar el pedido y termina su ejecución.

En DanyNetwork, un hilo puede comenzar en estado `NEW`, pasar a `RUNNABLE` al iniciarse, entrar en `TIMED_WAITING` durante la simulación de la atención y finalmente llegar a `TERMINATED`.

---

### 2. Stack, heap y recursos compartidos

| Elemento | Ubicación o tipo | Explicación |
| --- | --- | --- |
| **Variable local `cantidad`** | Stack del hilo | Cada ejecución del método que la declara tiene su propio contexto de variables locales. |
| **Objeto `Pedido`** | Heap | El objeto se crea dinámicamente y puede ser referenciado por el hilo que lo procesa. |
| **Variable estática `piezasVendidas`** | Heap, asociada a la clase | Es compartida por los hilos que acceden a ella. |
| **Archivo de registro abierto** | Recurso del sistema operativo | El programa puede mantener un descriptor de archivo para escribir información. |

Cada hilo tiene su propia pila de ejecución (*Stack*). En cambio, los objetos creados dinámicamente (*Heap*) y los campos estáticos compartidos pueden ser accedidos por distintos hilos, según las referencias que tengan.

---

### 3. Predicción de una condición de carrera

Si dos hilos ejecutan al mismo tiempo la instrucción:

java
piezasVendidas = piezasVendidas + 1;

Puede ocurrir una **condición de carrera** porque la operación incluye leer el valor, sumarle uno y escribir el resultado. Estas acciones no forman una operación atómica.

Por ejemplo, si el valor inicial es `10`, ambos hilos pueden leer `10` y calcular `11`. Si los dos escriben `11`, el contador termina en `11` en vez de `12`.

Por esta razón, el contador puede mostrar menos incrementos que los esperados. Sin embargo, el resultado depende de la planificación de los hilos y no necesariamente fallará en todas las ejecuciones.

---

## Durante la clase

### 4. Implementación concurrente

El servidor de pedidos de DanyNetwork recibe líneas con el formato `ID;PRODUCTO;CANTIDAD`. Cada pedido válido se procesa en un hilo independiente. El servidor registra la hora de recepción, el inicio y el final de la atención.

### 5. Experimento de concurrencia

Se ejecuta el generador con 1000 pedidos y se conecta su salida al servidor mediante una tubería. La prueba se repite cinco veces para observar si el contador compartido pierde incrementos.

Se registran los resultados reales de cada ejecución, sin asumir que necesariamente habrá una diferencia.

### 6. Evidencia de procesos e hilos

Durante el procesamiento se ejecuta el comando:

bash
ps -o pid,nlwp,stat,cmd -C java
para observar los procesos Java, el número de hilos y su estado. La salida se guarda en `evidencias/hilos.txt`.

---

## Resultados de las cinco ejecuciones

Ejecuté cinco veces el generador de pedidos con 1000 pedidos por ejecución, conectándolo al servidor mediante una tubería en Kali Linux.

| Ejecución | Pedidos recibidos | Piezas según el contador |
| :---: | :---: | :---: |
| 1 | 1000 | 999 |
| 2 | 1000 | 996 |
| 3 | 1000 | 999 |
| 4 | 1000 | 998 |
| 5 | 1000 | 998 |

En todas las ejecuciones, el servidor recibió los 1000 pedidos. Sin embargo, el contador compartido terminó con valores entre 996 y 999.

Esto demuestra que se pueden perder incrementos cuando varios hilos modifican una variable compartida sin sincronización. La operación de incrementar el contador no es atómica y puede ser interrumpida por la ejecución de otro hilo.

---

## Conclusión del experimento

La concurrencia permite atender varios pedidos simultáneamente en DanyNetwork, pero también puede producir inconsistencias en los datos compartidos. Para evitar esta condición de carrera se podría utilizar `synchronized` o `AtomicInteger`. En esta práctica se mantuvo el contador sin protección para observar el problema.

# Avance del proyecto

**Proyecto:** DanyNetwork — Semana 4

Durante esta semana adapté el servidor de pedidos de DanyNetwork para atender solicitudes concurrentemente.

El sistema utiliza dos procesos Java: `GeneradorPedidos` y `ServidorPedidos`. Ambos se comunican mediante una tubería anónima. El generador produce pedidos con el formato `ID;PRODUCTO;CANTIDAD`, mientras que el servidor crea un hilo para procesar cada pedido.

Cada hilo convierte la información recibida en un objeto `Pedido`, simula su atención y muestra los datos mediante el método `toString()`.

---

## Diferencia entre proceso e hilo

Un **proceso** tiene su propio espacio de memoria y recursos administrados por el sistema operativo. Un **hilo** es una unidad de ejecución dentro de un proceso y comparte memoria y recursos con los demás hilos de ese mismo proceso.

---

## Razones para utilizar hilos

1. **Atención concurrente:** Los hilos permiten atender pedidos al mismo tiempo. En la prueba de diez pedidos con una espera de dos segundos, las atenciones comenzaron casi simultáneamente y finalizaron aproximadamente dos segundos después.
2. **Uso compartido de memoria:** Los hilos permiten compartir objetos y variables dentro del servidor sin establecer una nueva comunicación entre procesos para cada pedido. Esta característica facilita la atención concurrente, pero exige controlar el acceso a los datos compartidos.

---

## Resultados del contador compartido

| Ejecución | Pedidos recibidos | Contador |
| :---: | :---: | :---: |
| 1 | 1000 | 999 |
| 2 | 1000 | 996 |
| 3 | 1000 | 999 |
| 4 | 1000 | 998 |
| 5 | 1000 | 998 |

---

## Hipótesis

Los valores inferiores a 1000 se deben a que varios hilos pueden leer y modificar simultáneamente la variable `piezasVendidas`. Como la operación de incremento no está sincronizada, algunos resultados se sobrescriben y se pierden incrementos.

---

## Conclusión del avance

DanyNetwork pasó de recibir y registrar pedidos a procesarlos concurrentemente. Se conservaron las funcionalidades previas del proyecto y se incorporaron hilos, medición de tiempos y observación de condiciones de carrera.

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

# Semana 5 — Inventario compartido y condición de carrera

**Proyecto:** DanyNetwork

---

## ¿Qué es una condición de carrera?

Una **condición de carrera** ocurre cuando varios hilos acceden y modifican información compartida al mismo tiempo y el resultado depende del orden en que se ejecutan.

En DanyNetwork, varios pedidos pueden consultar la misma existencia disponible antes de que alguno actualice el inventario. Esto puede provocar que se confirmen más ventas de las que realmente permite el stock.

---

## Sección crítica del servidor

La sección crítica está dentro del método `vender()` de `GestorInventario.java`, que es llamado por los hilos de `ServidorPedidos.java`.

java
Integer disponible = existencias.get(producto);

if (disponible >= cantidad) {
    Thread.sleep(1);
    existencias.put(producto, disponible - cantidad);
    ventasConfirmadas++;
}

El problema es que la lectura, validación y actualización de las existencias no están protegidas como una sola operación.

---

## Tabla de intercalado

Ejemplo con seis routers disponibles:

| Paso | Hilo A (4 routers) | Hilo B (3 routers) |
| :---: | --- | --- |
| **1** | Lee 6 disponibles | |
| **2** | | Lee 6 disponibles |
| **3** | Verifica que puede vender 4 | |
| **4** | | Verifica que puede vender 3 |
| **5** | Actualiza existencia a 2 | |
| **6** | | Actualiza existencia a 3 |
| **7** | Confirma la venta | Confirma la venta |

Ambos hilos confirman siete unidades en total, aunque solamente había seis.

---

## Resultados experimentales

En la prueba de 20 pedidos no se observó ningún descuadre.

En la prueba de 1000 pedidos se obtuvieron los siguientes resultados:

* **Pedidos recibidos:** 1000
* **Confirmados según inventario:** 28
* **Rechazados según inventario:** 968
* **Confirmados registrados:** 28
* **Eliminadores:** 12 iniciales, 16 confirmados, existencia final 0 y descuadre de +4
* **Ponchadoras:** 15 iniciales, 17 confirmadas, existencia final 0 y descuadre de +2

Se observaron seis unidades de descuadre entre ambos productos. Además, los contadores registraron 996 pedidos en total, aunque el servidor recibió 1000.

---

## ¿Por qué es diferente de otros errores?

A diferencia de los errores de sintaxis o de lógica secuencial, una condición de carrera puede aparecer solamente en algunas ejecuciones. El resultado depende de cómo el sistema operativo distribuye el tiempo de CPU entre los hilos.

Esto hace que el error sea difícil de reproducir y depurar.

---

## Posibles soluciones

Tres alternativas técnicas para evitar la condición de carrera son:

1. **`synchronized`:** Permite proteger el método o bloque crítico mediante exclusión mutua.
2. **`ReentrantLock`:** Permite controlar explícitamente el acceso a la sección crítica.
3. **Actualizaciones atómicas:** Pueden proteger contadores; para coordinar correctamente la validación y descuento de inventario se necesitaría una estrategia atómica para toda la operación.

Considero que `synchronized` es la opción más adecuada para este proyecto porque permite proteger el método `vender()` con cambios mínimos.

---

## Conclusión

Logré integrar el inventario de DanyNetwork al servidor concurrente y demostrar que las ventas simultáneas pueden generar inconsistencias en las existencias y en los contadores.

La condición de carrera se conserva intencionalmente para realizar la comparación antes y después de implementar exclusión mutua en la siguiente semana.

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

# Avance del proyecto — Semana 6

**Proyecto:** DanyNetwork

---

## Exclusión mutua

La **exclusión mutua** permite que solamente un hilo a la vez ejecute una sección crítica. Una solución correcta debe garantizar tres condiciones fundamentales:
1. **Exclusión mutua**
2. **Progreso**
3. **Espera acotada**

---

## Mecanismo utilizado

Elegí `synchronized` en el método `vender()` de `GestorInventario.java`, para proteger conjuntamente:
* La lectura de existencias.
* La validación de disponibilidad.
* El descuento de productos.
* La actualización de los contadores.

---

## Comparación antes y después

### Semana 5 — Sin protección:
* **1000** pedidos recibidos.
* **28** ventas confirmadas.
* **968** rechazadas contabilizadas.
* **6** unidades de descuadre total.
* **4** operaciones perdidas en los contadores.

### Semana 6 — Con protección (`synchronized`):
* **Prueba de 50 cajeros y 10 piezas:** Cinco ejecuciones totalmente correctas.
* **Cada ejecución:** 10 ventas confirmadas, 40 rechazadas y existencia final en 0.
* **Servidor completo:** 200 pedidos recibidos.
* **28** pedidos confirmados y **172** rechazados.
* **Descuadre de inventario:** **0** en los ocho productos.

Los resultados muestran que proteger toda la operación de venta evita las inconsistencias observadas en las pruebas anteriores.

---

## Tamaño de la sección crítica

* **Candado demasiado pequeño:** Si se protege únicamente la escritura, dos hilos todavía pueden consultar las mismas existencias al mismo tiempo y confirmar ventas incorrectas.
* **Candado demasiado grande:** Si se protege todo el procesamiento de un pedido (incluyendo operaciones independientes), los demás hilos pueden permanecer bloqueados innecesariamente perdiendo concurrencia.

> **Regla general:** Proteger toda la operación compartida que debe ser indivisible, evitando bloquear trabajo que no necesita exclusión mutua.

*Pendiente:* Ejecutar y registrar ambos casos para documentarlos como experimentos reales y no solamente como explicación teórica.

---

## Semáforo

DanyNetwork puede representar tres estaciones de preparación mediante `Semaphore(3)`. Cada pedido adquiere un permiso (`acquire()`) al ocupar una estación y lo libera (`release()`) cuando termina.

Esto limita la cantidad de pedidos preparados simultáneamente sin restringir todas las ventas a un solo hilo.

# Evidencias experimentales — Semana 6

**Proyecto:** DanyNetwork

---

## 1. Inventario protegido con exclusión mutua

Se utilizó `synchronized` en el método `vender()` de `GestorInventario.java`, protegiendo en un mismo bloque atómico la consulta, validación, descuento de existencias y actualización de contadores.

La solución se basa en tres propiedades de sincronización: **exclusión mutua**, **progreso** y **espera acotada**.

### Resultados antes de la protección (Semana 5):
* **1000** pedidos recibidos.
* **28** ventas confirmadas y **968** rechazadas contabilizadas.
* **6** unidades de descuadre total.
* **4** operaciones perdidas en los contadores.

### Resultados después de la protección (Semana 6):
* **Prueba de 50 cajeros y 10 piezas:** Cinco pruebas ejecutadas; todas terminaron totalmente correctas.
* **Resultado por prueba:** 10 ventas confirmadas, 40 rechazadas y existencia final en **0**.
* **Servidor completo:** 200 pedidos recibidos, 28 confirmados y 172 rechazados.
* **Consistencia:** Los ocho productos del catálogo terminaron con un descuadre de **0**.

---

## 2. Experimento: candado demasiado pequeño

Se protegió únicamente la escritura del stock, dejando la lectura fuera del candado. Como consecuencia, los 50 hilos consultaron las mismas 10 piezas disponibles antes de aplicar la actualización.

### Resultados del experimento:
* **Stock inicial:** 10
* **Ventas confirmadas:** 50
* **Stock final:** 9
* **Stock esperado:** −40
* **Descuadre total:** +49

> **Conclusión:** El candado debe proteger la operación completa de lectura, validación y modificación. Proteger únicamente la escritura no evita que los hilos tomen decisiones basadas en información desactualizada.

---

## 3. Experimento: candado demasiado grande

Se midió el rendimiento comparando 20 hilos ejecutando una tarea simulación de 100 ms de duración bajo dos enfoques:

| Modalidad | Tiempo de ejecución |
| :--- | :---: |
| **Todo el trabajo sincronizado** | 2026 ms |
| **Trabajo independiente sin bloqueo** | 105 ms |

La ejecución con el candado sobredimensionado tardó aproximadamente **19.3 veces más** que la ejecución concurrente óptima.

> **Conclusión:** No se deben bloquear operaciones independientes. La sección crítica debe ser lo suficientemente amplia para garantizar la consistencia de los datos, pero no más grande de lo estrictamente necesario.

---

## 4. Semáforo para estaciones de preparación

Se implementó `EstacionesPreparacion.java` utilizando un `Semaphore(3)` para simular la capacidad limitada de tres estaciones de preparación de pedidos.

### Resultados:
* **Pedidos procesados:** 10
* **Ocupación máxima simultánea:** 3 estaciones
* **Estaciones libres al finalizar:** 3

El semáforo permitió regular el acceso concorrente a un recurso físico limitado, mientras que `synchronized` protegió la integridad lógica del inventario.

---

## Conclusión general

La Semana 6 permitió corregir las condiciones de carrera detectadas anteriormente y comprobar de forma experimental la importancia de delimitar correctamente los mecanismos de sincronización:
1. El inventario protegido mantuvo una consistencia de **0 descuadres** en todas las ejecuciones.
2. El semáforo limitó con precisión la capacidad máxima de las estaciones de atención.

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

PEDIDOS PERDIDOS:
**Con SIGTERM:** Se perdieron `0` pedidos (o los estrictamente no iniciados), ya que el servidor completó el vaciado de su cola de trabajo de forma ordenada gracias al cierre limpio.
  * **Con SIGKILL:** Se perdieron una cantidad considerable de pedidos en curso, ya que el proceso fue interrumpido de golpe en plena transacción de inventario sin oportunidad de vaciar el búfer ni generar el reporte final.
