s01 bloque 1: entorno y repositorio listos
no he elegido el dominio
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

