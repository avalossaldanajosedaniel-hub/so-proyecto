
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class EstacionesPreparacion {

    // DanyNetwork tiene 3 estaciones disponibles.
    private final Semaphore estaciones = new Semaphore(3);

    private final AtomicInteger ocupadas = new AtomicInteger(0);
    private final AtomicInteger maxOcupadas = new AtomicInteger(0);

    public void preparar(int idPedido) {

        boolean permisoAdquirido = false;

        try {
            estaciones.acquire();
            permisoAdquirido = true;

            int actuales = ocupadas.incrementAndGet();

            maxOcupadas.accumulateAndGet(
                actuales, Math::max
            );

            System.out.println(
                "Pedido " + idPedido
                + " entra. Estaciones ocupadas: " + actuales
            );

            Thread.sleep(500);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } finally {
            if (permisoAdquirido) {

                int restantes = ocupadas.decrementAndGet();

                System.out.println(
                    "Pedido " + idPedido
                    + " sale. Estaciones ocupadas: " + restantes
                );

                estaciones.release();
            }
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        EstacionesPreparacion preparacion =
                new EstacionesPreparacion();

        Thread[] pedidos = new Thread[10];

        for (int i = 0; i < pedidos.length; i++) {

            final int id = i + 1;

            pedidos[i] = new Thread(
                () -> preparacion.preparar(id)
            );

            pedidos[i].start();
        }

        for (Thread pedido : pedidos) {
            pedido.join();
        }

        System.out.println("-----------------------------");
        System.out.println(
            "Maximo simultaneo: "
            + preparacion.maxOcupadas.get()
        );
        System.out.println(
            "Estaciones libres al final: "
            + (3 - preparacion.ocupadas.get())
        );
    }
}
