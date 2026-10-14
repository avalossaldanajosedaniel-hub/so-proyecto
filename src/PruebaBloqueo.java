// PruebaBloqueo.java - simulación de interbloqueo entre BD y Log
public class PruebaBloqueo {

    static final Object BASE_DATOS = new Object();
    static final Object LOG_SISTEMA = new Object();

    public static void main(String[] args) {
        System.out.println("PID " + ProcessHandle.current().pid());

        Thread hilo1 = new Thread(() -> {
            synchronized (BASE_DATOS) {
                System.out.println("hilo-1: tomo BASE_DATOS");
                dormir(500);
                System.out.println("hilo-1: quiero LOG_SISTEMA");
                synchronized (LOG_SISTEMA) {
                    System.out.println("hilo-1: tengo los dos");
                }
            }
        }, "hilo-1");

        Thread hilo2 = new Thread(() -> {
            synchronized (LOG_SISTEMA) { // Orden invertido a propósito
                System.out.println("hilo-2: tomo LOG_SISTEMA");
                dormir(500);
                System.out.println("hilo-2: quiero BASE_DATOS");
                synchronized (BASE_DATOS) {
                    System.out.println("hilo-2: tengo los dos");
                }
            }
        }, "hilo-2");

        hilo1.start();
        hilo2.start();

        System.out.println("Hilos iniciados. Si no ves 'tengo los dos', hay bloqueo.");
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}
