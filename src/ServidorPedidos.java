
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServidorPedidos {

    static final String NEGOCIO = "DanyNetwork";

    static volatile boolean apagando = false;

    static int contador = 0;

    // Inventario compartido entre todos los hilos.
    static GestorInventario inventario = new GestorInventario();

    static final List<Thread> hilos = new ArrayList<>();

    // Registro de pedidos confirmados para comprobar descuadres.
    static final List<Pedido> confirmados = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // Cargar el inventario antes de iniciar los hilos.
        inventario.cargarDesde("datos/catalogo.txt");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            apagando = true;
            registrar("Senal de apagado recibida.");
            registrar("Servidor apagado.");
        }, "apagado"));

        long pid = ProcessHandle.current().pid();

        registrar("Servidor de " + NEGOCIO + " listo. PID " + pid);
        registrar("Inventario cargado desde datos/catalogo.txt");
        registrar("Formato: ID;PRODUCTO;CANTIDAD");

        // Leer pedidos desde la consola o tuberia anonima.
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

        // Conservar la lectura opcional del FIFO.
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

        // Esperar a todos los hilos antes de mostrar el resumen.
        for (Thread hilo : hilos) {
            hilo.join();
        }

        registrar("Todos los pedidos terminaron.");
        mostrarResumen();
    }

    static void recibirPedido(String linea) {

        contador++;

        final String datos = linea.trim();
        final int numero = contador;

        Thread hilo = new Thread(
                () -> atender(datos, numero),
                "pedido-" + numero
        );

        hilos.add(hilo);
        hilo.start();

        registrar("Recibido pedido " + numero
                + ". Hilos vivos: " + Thread.activeCount());
    }

    static void atender(String linea, int numero) {

        String nombreHilo = Thread.currentThread().getName();

        registrar(nombreHilo + " empieza: " + linea);

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

            // SECCION CRITICA DE LA SEMANA 5:
            // Varios hilos llaman al mismo inventario.
            // No se sincroniza para demostrar el descuadre.
            boolean vendido = inventario.vender(
                    pedido.getProducto(),
                    pedido.getCantidad()
            );

            if (vendido) {
                pedido.setEstado("CONFIRMADO");

                // Solo sincronizamos el registro de evidencia.
                // NO sincronizamos la venta del inventario.
                synchronized (confirmados) {
                    confirmados.add(pedido);
                }

            } else {
                pedido.setEstado("RECHAZADO");
            }

            registrar(nombreHilo + " proceso: " + pedido);

        } catch (NumberFormatException e) {
            registrar(nombreHilo + ": datos numericos incorrectos");
        }

        registrar(nombreHilo + " termino");
    }

    static void mostrarResumen() {

        registrar("===== RESUMEN DANYNETWORK =====");
        registrar("Pedidos recibidos: " + contador);

        registrar("Confirmados segun inventario: "
                + inventario.getVentasConfirmadas());

        registrar("Rechazados segun inventario: "
                + inventario.getVentasRechazadas());

        registrar("Confirmados registrados: " + confirmados.size());

        // Sumar las unidades de cada producto confirmado.
        Map<String, Integer> unidadesConfirmadas = new HashMap<>();

        for (Pedido pedido : confirmados) {
            String producto = pedido.getProducto();
            int cantidad = pedido.getCantidad();

            unidadesConfirmadas.merge(producto, cantidad, Integer::sum);
        }

        registrar("===== INVENTARIO FINAL =====");

        // Comparar contra las existencias iniciales del catalogo.
        try (BufferedReader lector = new BufferedReader(
                new FileReader("datos/catalogo.txt"))) {

            String linea;

            while ((linea = lector.readLine()) != null) {

                String[] campos = linea.split(";");

                if (campos.length != 4) continue;

                String producto = campos[1].trim();
                int inicial = Integer.parseInt(campos[3].trim());
                int vendido = unidadesConfirmadas.getOrDefault(producto, 0);
                int esperado = inicial - vendido;
                int real = inventario.existencia(producto);

                registrar(producto
                        + " | Inicial: " + inicial
                        + " | Confirmado: " + vendido
                        + " | Esperado: " + esperado
                        + " | Real: " + real
                        + " | Descuadre: " + (real - esperado));
            }

        } catch (Exception e) {
            registrar("Error al generar resumen: " + e.getMessage());
        }
    }

    static void registrar(String mensaje) {
        String hora = LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        System.out.println("[" + hora + "] " + mensaje);
    }

    // Conserva la funcionalidad de lanzar un proceso hijo.
    static void recibirDelGenerador(int cuantos) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", "src",
                "GeneradorPedidos", String.valueOf(cuantos));

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
