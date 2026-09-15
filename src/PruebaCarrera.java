public class PruebaCarrera {

    public static void main(String[] args) throws InterruptedException {

        // TODO: cambia estos valores por un producto de TU catalogo
        String producto = "paracetamol";
        int existenciaInicial = 10;
        int cajeros = 10;

        GestorInventario inventario = new GestorInventario();
        inventario.agregar(producto, existenciaInicial);

        Thread[] hilos = new Thread[cajeros];

        // Cada cajero intenta comprar UNA pieza.
        for (int i = 0; i < cajeros; i++) {
            final int numero = i + 1;
            hilos[i] = new Thread(() -> {
                boolean vendido = inventario.vender(producto, 1);
                System.out.println("Cajero " + numero + ": "
                        + (vendido ? "VENDIDO" : "rechazado"));
            });
        }

        // Arrancan todos lo mas junto posible.
        for (Thread h : hilos) h.start();

        // Esperamos a que todos terminen antes de contar.
        for (Thread h : hilos) h.join();

        System.out.println("---------------------------------------");
        System.out.println("Existencia inicial:   " + existenciaInicial);
        System.out.println("Cajeros:              " + cajeros);
        System.out.println("Ventas confirmadas:   " + inventario.getVentasConfirmadas());
        System.out.println("Ventas rechazadas:    " + inventario.getVentasRechazadas());
        System.out.println("Existencia final:     " + inventario.existencia(producto));
        System.out.println("---------------------------------------");
        System.out.println("CUADRA? inicial - confirmadas == final ?  "
                + ((existenciaInicial - inventario.getVentasConfirmadas())
                    == inventario.existencia(producto)));
    }
}
