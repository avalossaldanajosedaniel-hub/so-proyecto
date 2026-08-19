import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LeerProcesos {
    public static void main(String[] args) {
        Path rutaArchivo = Path.of("evidencias/procesos.txt");

        try {
            // Leer todas las líneas del archivo
            List<String> lineas = Files.readAllLines(rutaArchivo);

            // Imprimir cada línea en la consola
            System.out.println("--- Contenido del archivo ---");
            for (String linea : lineas) {
                System.out.println(linea);
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
