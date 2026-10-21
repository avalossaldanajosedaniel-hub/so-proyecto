
import java.util.Random;

public class GeneradorPedidos {

    public static void main(String[] args) {

        long pid = ProcessHandle.current().pid();
        System.err.println("Generador arrancado. PID " + pid);

        int cuantos = Integer.parseInt(args[0]);

        String[] productos = {
  	  "eliminadores",
  	  "conectores rj45",
  	  "poe",
  	  "switches",
   	 "Routers",
   	 "camaras",
   	 "ponchadoras",
   	 "Cable de red"
	};

        Random random = new Random();

        for (int i = 1; i <= cuantos; i++) {
            String producto = productos[random.nextInt(productos.length)];
            int cantidad = random.nextInt(5) + 1;

            System.out.println(i + ";" + producto + ";" + cantidad);
        }

        System.err.println("Generador termina. PID " + pid);
    }
}
