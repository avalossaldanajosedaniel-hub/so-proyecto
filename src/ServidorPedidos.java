
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServidorPedidos {

    static final String NEGOCIO = "DanyNetwork";

    static volatile boolean apagando = false;

    static int contador = 0;
    static int piezasVendidas = 0;

    static final List<Thread> hilos = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            apagando = true;
            registrar("Senal de apagado recibida.");
            registrar("Resumen final:");
            registrar("  Pedidos recibidos: " + contador);
            registrar("  Piezas vendidas segun el contador: "
                    + piezasVendidas);
            registrar("Servidor apagado.");
        }, "apagado"));

        long pid = ProcessHandle.current().pid();

        registrar("Servidor de " + NEGOCIO + " listo. PID " + pid);
        registrar("Formato: ID;PRODUCTO;CANTIDAD");

        // Leer pedidos desde la consola o una tuberia anonima.
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(System.in))) {

            String linea;

            while (!apagando && (linea = entrada.readLine()) != null) {
                if (linea.trim().equalsIgnoreCase("salir")) {
                    break;
                }

                if (!linea.trim().isEmpty()) {
                    recibirPedido(linea);
                }
            }
        }

        // Leer pedidos desde el archivo/FIFO si existe.
        File archivoFifo = new File("datos/cajero.pipe");

        if (!apagando
        && args.length > 0
        && args[0].equals("--fifo")
        && archivoFifo.exists()) {
            try (BufferedReader fifo = new BufferedReader(
                    new FileReader(archivoFifo))) {

                String linea;

                while (!apagando && (linea = fifo.readLine()) != null) {
                    if (!linea.trim().isEmpty()) {
                        recibirPedido(linea);
                    }
                }

            } catch (Exception e) {
                registrar("Error leyendo FIFO: " + e.getMessage());
            }
        }

        // Esperar a que terminen todos los hilos antes de salir.
        for (Thread hilo : hilos) {
            hilo.join();
        }

        registrar("Todos los pedidos recibidos terminaron.");
    }

    static void recibirPedido(String linea) {

        contador++;

        final String pedido = linea.trim();
        final int numero = contador;

        Thread hilo = new Thread(
                () -> atender(pedido, numero),
                "pedido-" + numero
        );

        hilos.add(hilo);
        hilo.start();

        registrar("Recibido pedido " + numero
                + ". Hilos vivos: " + Thread.activeCount());
    }

    static void atender(String linea, int numero) {

        String nombreHilo = Thread.currentThread().getName();

        registrar(nombreHilo + " empieza a atender: " + linea);

        try {
            String[] campos = linea.split(";", -1);

            if (campos.length != 3) {
                registrar(nombreHilo + ": formato incorrecto");
                return;
            }

            int id = Integer.parseInt(campos[0].trim());
            String producto = campos[1].trim();
            int cantidad = Integer.parseInt(campos[2].trim());

            if (producto.isEmpty() || cantidad <= 0) {
                registrar(nombreHilo + ": datos incorrectos");
                return;
            }

            Pedido pedido = new Pedido(id, producto, cantidad);

            // Simular el tiempo de atencion del pedido.
            Thread.sleep(1);

            // Intencionalmente sin sincronizacion para la practica.
            piezasVendidas = piezasVendidas + 1;

            registrar(nombreHilo + " proceso el pedido: " + pedido);

        } catch (NumberFormatException e) {
            registrar(nombreHilo + ": ID o cantidad no numericos");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            registrar(nombreHilo + " fue interrumpido");
            return;
        }

        registrar(nombreHilo + " termino");
    }

    static void registrar(String mensaje) {
        String hora = LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        System.out.println("[" + hora + "] " + mensaje);
    }

    // Permite lanzar el generador como proceso hijo.
    static void recibirDelGenerador(int cuantos) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "java", "GeneradorPedidos", String.valueOf(cuantos));

        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process hijo = pb.start();

        registrar("Generador lanzado con PID " + hijo.pid());

        try (BufferedReader tuberia = new BufferedReader(
                new InputStreamReader(hijo.getInputStream()))) {

            String linea;

            while ((linea = tuberia.readLine()) != null) {
                recibirPedido(linea);
            }
        }

        int salida = hijo.waitFor();
        registrar("El generador termino con codigo " + salida);
    }
}
