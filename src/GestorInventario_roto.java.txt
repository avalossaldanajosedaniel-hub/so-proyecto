import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;

public class GestorInventario {

    // Este mapa lo van a tocar todos los hilos a la vez.
    private Map<String, Integer> existencias = new HashMap<>();

    // Contadores para poder auditar lo que paso.
    private int ventasConfirmadas = 0;
    private int ventasRechazadas = 0;

    public void agregar(String producto, int cantidad) {
        existencias.put(producto, cantidad);
    }

    // ESTA es la seccion critica del curso completo.
    public boolean vender(String producto, int cantidad) {

        Integer disponible = existencias.get(producto);
        if (disponible == null) return false;

        if (disponible >= cantidad) {

            // Esta pausa no cambia la logica: solo ensancha la ventana
            // para que el problema se vea sin necesidad de 10000 hilos.
            try { Thread.sleep(1); } catch (InterruptedException e) { }

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

    public int getVentasConfirmadas() { return ventasConfirmadas; }
    public int getVentasRechazadas() { return ventasRechazadas; }

    // Resuelto el TODO: metodo cargarDesde
    public void cargarDesde(String archivo) {
        try {
            Path path = Paths.get(archivo);
            List<String> lineas = Files.readAllLines(path);
            
            for (String linea : lineas) {
                if (linea == null || linea.trim().isEmpty() || linea.startsWith("ID")) {
                    continue;
                }
                
                String[] partes = linea.split(";");
                if (partes.length >= 4) {
                    String nombreProducto = partes[1].trim();
                    int existenciaInicial = Integer.parseInt(partes[3].trim());
                    agregar(nombreProducto, existenciaInicial);
                }
            }
            System.out.println("Catalogo cargado exitosamente desde: " + archivo);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error de formato numérico: " + e.getMessage());
        }
    }
}
