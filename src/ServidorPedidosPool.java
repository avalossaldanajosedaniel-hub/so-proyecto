
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServidorPedidosPool {

    static GestorInventario inventario = new GestorInventario();

    static int contador = 0;

    public static void main(String[] args) throws Exception {

        inventario.cargarDesde("datos/catalogo.txt");
        registrar("Inventario de DanyNetwork cargado.");

        ExecutorService pool = Executors.newFixedThreadPool(4);

        long inicio = System.currentTimeMillis();

        registrar("DanyNetwork - Pool de 4 hilos");

        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(System.in))) {

            String linea;

            while ((linea = entrada.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                contador++;

                final int numero = contador;
                final String datos = linea;

                pool.submit(() -> atender(datos, numero));
            }

        } finally {
            pool.shutdown();
        }

        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        long duracion = System.currentTimeMillis() - inicio;

        registrar("===== RESUMEN DANYNETWORK =====");
        registrar("Pedidos recibidos: " + contador);
        registrar("Pedidos confirmados: "
                + inventario.getVentasConfirmadas());
        registrar("Pedidos rechazados: "
                + inventario.getVentasRechazadas());

        registrar("===== INVENTARIO FINAL =====");
        inventario.mostrarInventario();

        registrar("Tiempo total: " + duracion + " ms");
        registrar("Pool terminado.");
    }

    static void atender(String linea, int numero) {

        try {
            String[] campos = linea.split(";", -1);

            if (campos.length != 3) {
                registrar("Formato incorrecto: " + linea);
                return;
            }

            Pedido pedido = new Pedido(
                Integer.parseInt(campos[0].trim()),
                campos[1].trim(),
                Integer.parseInt(campos[2].trim())
            );

            String nombreHilo = Thread.currentThread().getName();

            boolean vendido = inventario.vender(
                pedido.getProducto(),
                pedido.getCantidad()
            );

            if (vendido) {
                pedido.setEstado("CONFIRMADO");
            } else {
                pedido.setEstado("RECHAZADO");
            }

            registrar(nombreHilo
                    + " proceso pedido " + numero
                    + ": " + pedido);

            Thread.sleep(2000);

            registrar(nombreHilo
                    + " termino pedido " + numero);

        } catch (NumberFormatException e) {
            registrar("Datos numericos incorrectos: " + linea);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void registrar(String mensaje) {

        String hora = LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        System.out.println("[" + hora + "] " + mensaje);
    }
}

