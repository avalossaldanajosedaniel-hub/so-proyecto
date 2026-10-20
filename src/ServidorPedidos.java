import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServidorPedidos {

    static final String NEGOCIO = "DanyNetwork";

    // Bandera compartida: los hilos la consultan para saber si deben seguir.
    static volatile boolean apagando = false;

    // Contador de pedidos recibidos
    static int contador = 0;

    public static void main(String[] args) throws Exception {

        // Registrar el Shutdown Hook al inicio del main, antes del ciclo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            apagando = true;
            registrar("Senal de apagado recibida. No se aceptan pedidos nuevos.");

            registrar("Resumen final:");
            registrar("  Pedidos recibidos:  " + contador);

            registrar("Servidor apagado ordenadamente.");
        }, "apagado"));

        long pid = ProcessHandle.current().pid();

        registrar("Servidor de " + NEGOCIO + " listo. PID " + pid);

        String directorioActual = System.getProperty("user.dir");
        registrar("Directorio de trabajo: " + directorioActual);

        // Lectura de la entrada estándar (Tubería anónima: java GeneradorPedidos | java ServidorPedidos)
        try (BufferedReader entradaEstandard = new BufferedReader(new InputStreamReader(System.in))) {
            String linea;
            while (!apagando && entradaEstandard.ready() && (linea = entradaEstandard.readLine()) != null) {
                contador++;
                registrar("Por la tuberia llego: " + linea);
            }
        } catch (Exception e) {
            // Continuar si no hay tubería anónima conectada
        }

        // Lectura de Tubería con nombre (FIFO) si existe el archivo
        File archivoFifo = new File("datos/cajero.pipe");
        if (archivoFifo.exists()) {
            try (BufferedReader fifo = new BufferedReader(new FileReader(archivoFifo))) {
                String linea;
                while (!apagando && (linea = fifo.readLine()) != null) {
                    contador++;
                    registrar("Del FIFO: " + linea);
                }
            } catch (Exception e) {
                registrar("Error leyendo FIFO: " + e.getMessage());
            }
        }

        while (!apagando) {
            Thread.sleep(5000);
            registrar("En espera de pedidos");
        }
    }

    static void registrar(String mensaje) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + hora + "] " + mensaje);
    }

    // Lanza al generador como hijo y lee sus pedidos por la tuberia.
    static void recibirDelGenerador(int cuantos) throws Exception {

        ProcessBuilder pb = new ProcessBuilder("java", "GeneradorPedidos",
                                               String.valueOf(cuantos));
        pb.redirectErrorStream(true);

        Process hijo = pb.start();
        registrar("Generador lanzado con PID " + hijo.pid());

        // ESTO es la tuberia: la salida del hijo es un flujo que yo leo.
        try (BufferedReader tuberia = new BufferedReader(
                 new InputStreamReader(hijo.getInputStream()))) {

            String linea;
            while ((linea = tuberia.readLine()) != null) {
                contador++;
                registrar("Por la tuberia llego: " + linea);
            }
        }

        int salida = hijo.waitFor();
        registrar("El generador termino con codigo " + salida);
    }
}
