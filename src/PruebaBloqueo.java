
import java.util.concurrent.CountDownLatch;

// SEMANA 7 - DanyNetwork
// Interbloqueo provocado intencionalmente.
// Este programa es evidencia academica, NO un error
// pendiente de corregir ni codigo de produccion.

public class PruebaBloqueo {

    static final Object INVENTARIO = new Object();
    static final Object REGISTRO_VENTAS = new Object();

    static final CountDownLatch preparados =
            new CountDownLatch(2);

    public static void main(String[] args) {

        System.out.println(
            "PID: " + ProcessHandle.current().pid()
        );

        Thread hilo1 = new Thread(() -> {

            synchronized (INVENTARIO) {

                System.out.println(
                    "Cajero 1: tomo INVENTARIO"
                );

                esperarAlOtro();

                System.out.println(
                    "Cajero 1: quiero REGISTRO_VENTAS"
                );

                synchronized (REGISTRO_VENTAS) {
                    System.out.println(
                        "Cajero 1: tengo los dos"
                    );
                }
            }

        }, "Cajero-Inventario");

        Thread hilo2 = new Thread(() -> {

            // Orden invertido A PROPOSITO para crear deadlock.
            synchronized (REGISTRO_VENTAS) {

                System.out.println(
                    "Cajero 2: tomo REGISTRO_VENTAS"
                );

                esperarAlOtro();

                System.out.println(
                    "Cajero 2: quiero INVENTARIO"
                );

                synchronized (INVENTARIO) {
                    System.out.println(
                        "Cajero 2: tengo los dos"
                    );
                }
            }

        }, "Cajero-Registro");

        hilo1.start();
        hilo2.start();

        System.out.println(
            "Hilos iniciados. Deadlock intencional."
        );
    }

    static void esperarAlOtro() {

        preparados.countDown();

        try {
            preparados.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

