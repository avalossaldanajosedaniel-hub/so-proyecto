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
