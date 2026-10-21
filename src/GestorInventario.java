
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GestorInventario {

    private Map<String, Integer> existencias = new HashMap<>();

    private int ventasConfirmadas = 0;
    private int ventasRechazadas = 0;

    public void agregar(String producto, int cantidad) {
        existencias.put(producto, cantidad);
    }

    // Semana 5: SIN synchronized para demostrar
    // una condicion de carrera.
    public boolean vender(String producto, int cantidad) {

        Integer disponible = existencias.get(producto);

        if (disponible == null || cantidad <= 0) {
            ventasRechazadas++;
            return false;
        }

        if (disponible >= cantidad) {

            // Amplia la ventana de la condicion de carrera.
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            existencias.put(producto, disponible - cantidad);
            ventasConfirmadas++;
            return true;
        }

        ventasRechazadas++;
        return false;
    }

    public int existencia(String producto) {
        return existencias.getOrDefault(producto, 0);
    }

    public int getVentasConfirmadas() {
        return ventasConfirmadas;
    }

    public int getVentasRechazadas() {
        return ventasRechazadas;
    }

    public void cargarDesde(String archivo) throws IOException {

        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivo))) {

            String linea;

            while ((linea = lector.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(";");

                if (datos.length != 4) continue;

                String producto = datos[1].trim();
                int cantidad = Integer.parseInt(datos[3].trim());

                agregar(producto, cantidad);
            }
        }
    }

    public void mostrarInventario() {
        for (Map.Entry<String, Integer> producto :
                existencias.entrySet()) {

            System.out.println(producto.getKey()
                    + ": " + producto.getValue());
        }
    }
}
