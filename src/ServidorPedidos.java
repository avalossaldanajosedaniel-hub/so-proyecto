import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServidorPedidos {

    static final String NEGOCIO = "DanyNetwork";

    public static void main(String[] args) throws InterruptedException {

        long pid = ProcessHandle.current().pid();

        registrar("Servidor de " + NEGOCIO + " listo. PID " + pid);

        String directorioActual = System.getProperty("user.dir");
        registrar("Directorio de trabajo: " + directorioActual);

        while (true) {
            Thread.sleep(5000);
            registrar("En espera de pedidos");
        }
    }

    static void registrar(String mensaje) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + hora + "] " + mensaje);
    }
}
