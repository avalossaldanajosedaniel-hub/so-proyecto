import java.util.Random;

// GeneradorPedidos.java - simula a un cajero que manda pedidos
public class GeneradorPedidos {

    public static void main(String[] args) {

        long pid = ProcessHandle.current().pid();
        System.out.println("Generador arrancado. PID " + pid);

        int cuantos = Integer.parseInt(args[0]);

        String[] productos = {
            "Router Gigabit",
            "Switch 24 Puertos",
            "Cable UTP Cat6",
            "Access Point WiFi 6",
            "Patch Cord Fibra"
        };
        Random random = new Random();

        for (int i = 1; i <= cuantos; i++) {
            String producto = productos[random.nextInt(productos.length)];
            int cantidad = random.nextInt(5) + 1;
            System.out.println(i + ";" + producto + ";" + cantidad);
        }

        System.out.println("Generador termina. PID " + pid);
    }
}
