import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class PruebaTryLock {

    static final ReentrantLock INVENTARIO = new ReentrantLock();
    static final ReentrantLock LOG = new ReentrantLock();

    static boolean intentarLosDos(String quien) throws InterruptedException {
        // Intenta tomar INVENTARIO con un tiempo límite de 200ms
        if (!INVENTARIO.tryLock(200, TimeUnit.MILLISECONDS)) {
            return false;
        }
        try {
            // Intenta tomar LOG con un tiempo límite de 200ms
            if (!LOG.tryLock(200, TimeUnit.MILLISECONDS)) {
                return false; // El bloque finally liberará INVENTARIO automáticamente
            }
            try {
                System.out.println(quien + ": tengo los dos");
                return true;
            } finally {
                LOG.unlock();
            }
        } finally {
            INVENTARIO.unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println("PID " + ProcessHandle.current().pid());

        Thread cajero1 = new Thread(() -> {
            String nombre = "cajero-1";
            int intentos = 0;
            try {
                while (!intentarLosDos(nombre)) {
                    intentos++;
                    System.out.println(nombre + ": no pude, reintento " + intentos);
                    Thread.sleep((long) (Math.random() * 100)); // Espera aleatoria para evitar livelock
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cajero-1");

        Thread cajero2 = new Thread(() -> {
            String nombre = "cajero-2";
            int intentos = 0;
            try {
                while (!intentarLosDos(nombre)) {
                    intentos++;
                    System.out.println(nombre + ": no pude, reintento " + intentos);
                    Thread.sleep((long) (Math.random() * 100)); // Espera aleatoria
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cajero-2");

        cajero1.start();
        cajero2.start();
    }
}
