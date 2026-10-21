
public class PruebaCandadoGrande {

    static class Servidor {

        // Candado demasiado grande:
        // bloquea tambien el trabajo independiente.
        public synchronized void atenderMal() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Trabajo independiente fuera del candado.
        public void atenderBien() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static long ejecutar(boolean candadoGrande)
            throws InterruptedException {

        Servidor servidor = new Servidor();
        Thread[] hilos = new Thread[20];

        long inicio = System.nanoTime();

        for (int i = 0; i < hilos.length; i++) {

            hilos[i] = new Thread(() -> {

                if (candadoGrande) {
                    servidor.atenderMal();
                } else {
                    servidor.atenderBien();
                }
            });

            hilos[i].start();
        }

        for (Thread hilo : hilos) {
            hilo.join();
        }

        return (System.nanoTime() - inicio) / 1_000_000;
    }

    public static void main(String[] args)
            throws InterruptedException {

        long grande = ejecutar(true);
        long reducido = ejecutar(false);

        System.out.println("===== CANDADO GRANDE =====");
        System.out.println(
            "Todo sincronizado: " + grande + " ms"
        );
        System.out.println(
            "Trabajo independiente: " + reducido + " ms"
        );
    }
}

