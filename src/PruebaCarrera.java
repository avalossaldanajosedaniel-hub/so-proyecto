
public class PruebaCarrera {

    public static void main(String[] args)
            throws InterruptedException {

        String producto = "Routers";
        int existenciaInicial = 10;
        int cajeros = 50;

        boolean todasCorrectas = true;

        for (int prueba = 1; prueba <= 5; prueba++) {
		final int numeroPrueba = prueba;

            GestorInventario inventario = new GestorInventario();
            inventario.agregar(producto, existenciaInicial);

            // Arreglo de 50 hilos.
            Thread[] hilos = new Thread[cajeros];

            for (int i = 0; i < cajeros; i++) {

                final int numero = i + 1;

                hilos[i] = new Thread(() -> {

                    boolean vendido = inventario.vender(
                            producto, 1
                    );

                    System.out.println(
                            "Prueba " + numeroPrueba
                            + " - Cajero " + numero + ": "
                            + (vendido ? "VENDIDO" : "RECHAZADO")
                    );
                });
            }

            // Iniciar los 50 cajeros.
            for (Thread h : hilos) {
                h.start();
            }

            // Esperar a que todos terminen.
            for (Thread h : hilos) {
                h.join();
            }

            int confirmadas = inventario.getVentasConfirmadas();
            int rechazadas = inventario.getVentasRechazadas();
            int existenciaFinal = inventario.existencia(producto);

            boolean correcto =
                    confirmadas == 10
                    && rechazadas == 40
                    && existenciaFinal == 0
                    && existenciaInicial - confirmadas
                            == existenciaFinal;

            if (!correcto) {
                todasCorrectas = false;
            }

            System.out.println("--------------------------------");
            System.out.println("Prueba: " + prueba);
            System.out.println("Existencia inicial: "
                    + existenciaInicial);
            System.out.println("Cajeros: " + cajeros);
            System.out.println("Confirmadas: " + confirmadas);
            System.out.println("Rechazadas: " + rechazadas);
            System.out.println("Existencia final: "
                    + existenciaFinal);
            System.out.println("CUADRA: "
                    + (correcto ? "SI" : "NO"));
            System.out.println("--------------------------------");
        }

        System.out.println(
                todasCorrectas
                ? "RESULTADO: 5 DE 5 PRUEBAS CORRECTAS"
                : "RESULTADO: SE DETECTARON ERRORES"
        );
    }

    // Numero de prueba para mostrarlo en los hilos.
    static final ThreadLocal<Integer> pruebaActual =
            ThreadLocal.withInitial(() -> 0);
}
