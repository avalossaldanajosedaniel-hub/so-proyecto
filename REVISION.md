# Retroalimentacion

Esto lo llena tu asesor despues de cada revision de avances (semanas 9, 14 y
17). Lo ves haciendo un `git pull` normal de tu repositorio. No lo edites:
crece con una seccion nueva en cada revision, las anteriores no se borran.

Los niveles son los de la rubrica de `evaluacion/practicas`: **Excelente**,
**Bueno**, **Suficiente**, **Insuficiente**.

---

### Nota rapida (2026-09-08)
Ya pude clonar tu repo con tu usuario de GitHub. Buena noticia: revisando tus commits, en realidad no vas atrasado en contenido, solo estabas invisible porque faltaba tu usuario en el CSV. Tienes bloque 1 y bloque 2 de la semana 2 completos, y bloque 1, bloque 2 y hasta el extra (`Dockerfile`) de la semana 3 completos, con buena explicacion en los tres (namespaces/cgroups, la relacion PID/PPID entre `ServidorPedidos` y `GeneradorPedidos`, huerfano vs `waitFor()`). Cuatro cosas que te conviene corregir: (1) tu `README.md` sigue diciendo "DOMINIO no elegido", pero en tu `BITACORA.md` ya elegiste Estacionamiento: actualiza el README para que no quede desactualizado. (2) De la semana 1 no encuentro un commit `s01 bloque 1: ...` (el entorno y el repo los entregaste en commits "s01 proyecto: ..."); no afecta el contenido, solo el formato del historial, ya no hace falta rehacerlo pero cuida el mensaje exacto de aqui en adelante. (3) Tienes varios `.java` sueltos en la raiz del repo (`LeerCatalogo.java`, `LeerProcesos.java`, `Vacio.java`) que deberian vivir dentro de `src/` junto con los demas. (4) Quedo un archivo de respaldo de vim en la raiz (`.readme.md.swp`, del proyecto viejo "virussss"); borralo, no es parte del proyecto. Nada de esto es grave, con que lo ajustes para la revision formal esta bien.

### Nota rapida semana 4 (2026-09-21)
No veo commits de la semana 4 (`s04 bloque 1`/`s04 bloque 2`). Veo que ya subiste la semana 5; la 4 es la que le pone hilos a tu servidor, asi que conviene ponerla al dia. Si tuviste un problema, cuentamelo para que quede en la bitacora.

### Nota rapida semana 5 (2026-09-21)
Bloque 1 y bloque 2 presentes, y tus cinco corridas (existencia final 9, 8, 7, 8, 7) son coherentes con tu evidencia. Tres detalles. (1) El sintoma que te salio es el segundo, descuentos perdidos (10 confirmadas y quedan 7), no una sobreventa: nunca se vendio mas de lo que habia. (2) La tabla de intercalado del bloque 1 quedo mezclada con otra (a la mitad del texto del costo aparecen renglones con stock 5 y 4); deja una sola, limpia. (3) En la seccion critica describes donde empieza y termina, pero falta pegar el fragmento de codigo de tu `vender`. Ademas, borra `src/GestorInventario.java.save` (es respaldo de nano).

### Nota rápida (2026-10-06)
Tu `evidencias/carrera_resuelta.txt` tiene diez líneas de `true` y cinco de `false`: parece que quedaron mezcladas dos salidas (la de antes y la de después). Deja solo las cinco corridas de la versión protegida, y borra `BITACORA.md.save` y `src/GestorInventario.java.save` si existen (son respaldos de nano).

## Revision 1 (semana 9)

| Instrumento | Peso | Nivel |
|---|---|---|
| Evidencias | 50% | Suficiente |
| BITACORA.md | 30% | Suficiente |
| Preguntas | 20% | Excelente |

### Evidencias

Catálogo con 9 productos y carrera demostrada y resuelta. Pero
`evidencias/deadlock.txt` y `evidencias/senales.txt` no existen: el punto 8
(interbloqueo) y parte del punto 9 (señales) no tienen evidencia. El README
nunca llegó a fijar el dominio ("DOMINIO no elegido" sigue en el encabezado,
aunque el resto del proyecto ya tiene catálogo y código funcionando con un
dominio real). Varios commits de "sNN proyecto" de semanas 3 a 6 están todos
fechados el mismo día (2026-10-21), lo que sugiere que se escribieron de
golpe cerca de la fecha de entrega en vez de repartidos.

### BITACORA.md

El contenido está (646 líneas) pero la estructura es desordenada: hay
encabezados de semana duplicados y bloques de la semana 5 y 6 repetidos con
distinto formato, como si se hubiera reescrito varias veces sin limpiar.

### Preguntas

Las dos excelentes: explica bien por qué `SIGKILL` no se puede atrapar y
cómo se comunican su generador y su servidor sin memoria compartida.

### Para la siguiente revisión

Fijar el dominio en el README, agregar `evidencias/deadlock.txt` y
`evidencias/senales.txt`, y ordenar la bitácora en una sola sección por
semana sin duplicados.
