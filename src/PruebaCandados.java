
import java.util.concurrent.CountDownLatch;

public class PruebaCandados {

    static class InventarioCandadoPequeno {

        private int stock = 10;
        private int confirmadas = 0;

        // ERROR: la lectura ocurre fuera del candado.
        boolean vender() {

            int disponible = stock;

            if (disponible <= 0) {
                return false;
            }

            // Se protege solamente la escritura.
            synchronized (this) {
                stock = disponible - 1;
                confirmadas++;
            }

            return true;
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        InventarioCandadoPequeno inventario =
                new InventarioCandadoPequeno();

        Thread[] cajeros = new Thread[50];

        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch lecturas = new CountDownLatch(50);

        for (int i = 0; i < cajeros.length; i++) {

            cajeros[i] = new Thread(() -> {

                try {
                    inicio.await();

                    // Demostracion controlada:
                    // todos consultan el mismo stock.
                    int disponible = inventario.stock;

                    lecturas.countDown();
                    lecturas.await();

                    if (disponible > 0) {
                        synchronized (inventario) {
                            inventario.stock = disponible - 1;
                            inventario.confirmadas++;
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            cajeros[i].start();
        }

        inicio.countDown();

        for (Thread cajero : cajeros) {
            cajero.join();
        }

        System.out.println("===== CANDADO PEQUENO =====");
        System.out.println("Stock inicial: 10");
        System.out.println(
            "Confirmadas: " + inventario.confirmadas
        );
        System.out.println(
            "Stock final: " + inventario.stock
        );
        System.out.println(
            "Stock esperado: " + (10 - inventario.confirmadas)
        );
        System.out.println(
            "Descuadre: "
            + (inventario.stock
                - (10 - inventario.confirmadas))
        );
    }
}
