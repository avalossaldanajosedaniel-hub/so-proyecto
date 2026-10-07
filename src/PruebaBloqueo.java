// PruebaBloqueo.java - dos hilos que se abrazan hasta morir
public class PruebaBloqueo {

    // Dos recursos distintos. En tu servidor serian el inventario y el log.
    static final Object INVENTARIO = new Object();
    static final Object LOG = new Object();

    public static void main(String[] args) {

        System.out.println("PID " + ProcessHandle.current().pid());

        Thread cajero1 = new Thread(() -> {
            synchronized (INVENTARIO) {
                System.out.println("cajero-1: tomo INVENTARIO");
                dormir(500);                       // le doy tiempo al otro
                System.out.println("cajero-1: quiero LOG");
                synchronized (LOG) {
                    System.out.println("cajero-1: tengo los dos");
                }
            }
        }, "cajero-1");

        Thread cajero2 = new Thread(() -> {
            synchronized (INVENTARIO) {
	 System.out.println("cajero-2: tomo INVENTARIO");
	 	dormir(500);
   		 synchronized (LOG) {
        		System.out.println("cajero-2: tengo los dos");
	}
		}

        }, "cajero-2");

        cajero1.start();
        cajero2.start();

        System.out.println("Los dos hilos arrancaron. Si no ves 'tengo los dos', hay bloqueo.");
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}
