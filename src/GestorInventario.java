// GestorInventario.java - ahora con exclusion mutua
import java.util.HashMap;
import java.util.Map;

public class GestorInventario {

    private Map<String, Integer> existencias = new HashMap<>();
    private int ventasConfirmadas = 0;
    private int ventasRechazadas = 0;

    public synchronized void agregar(String producto, int cantidad) {
        existencias.put(producto, cantidad);
    }

    
    public synchronized boolean vender(String producto, int cantidad) {

        Integer disponible = existencias.get(producto);
        if (disponible == null) return false;

        if (disponible >= cantidad) {

  
            try { Thread.sleep(1); } catch (InterruptedException e) { }

            existencias.put(producto, disponible - cantidad);
            ventasConfirmadas++;
            return true;
        }

        ventasRechazadas++;
        return false;
    }

    public synchronized int existencia(String producto) {
        return existencias.getOrDefault(producto, 0);
    }

    public synchronized int getVentasConfirmadas() { return ventasConfirmadas; }
    public synchronized int getVentasRechazadas() { return ventasRechazadas; }

    public void cargarDesde(String archivo) {
        // ... lectura del archivo ...
    }
}
