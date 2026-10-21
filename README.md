# SISTEMAS OPERATIVOS
JOSE DAANIEL AVALOS SALDANA
DOMINIO tienda de equipos de redes
## Prevención de interbloqueos

DanyNetwork establece el siguiente orden global para adquirir candados:

1. **INVENTARIO:** protege las existencias y las operaciones de venta.
2. **REGISTRO_VENTAS:** protege el registro compartido de pedidos confirmados.

Si una operación necesita ambos candados, debe adquirir primero `INVENTARIO` y después `REGISTRO_VENTAS`.

Nunca debe adquirir `INVENTARIO` mientras mantiene bloqueado `REGISTRO_VENTAS`.

Esta política evita la espera circular y permite prevenir interbloqueos asociados a esos dos recursos.

El archivo `src/PruebaBloqueo.java` viola intencionalmente esta regla como demostración académica.

El servidor real utiliza sincronización para proteger las operaciones de inventario y el registro de pedidos. La revisión del orden de adquisición se documenta en `BITACORA.md`.


