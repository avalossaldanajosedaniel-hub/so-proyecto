import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LeerCatalogo {
    public static void main(String[] args) {
        Path rutaArchivo = Path.of("datos/catalogo.txt");

        try {
            // Leer todas las líneas del archivo
            List<String> lineas = Files.readAllLines(rutaArchivo);

            if (lineas.isEmpty()) {
                System.out.println("El archivo catalogo.txt está vacío.");
                return;
            }

            System.out.println("=== Catálogo de Productos ===");
            System.out.println("Total de registros cargados: " + (lineas.size() - 1));
            System.out.println("----------------------------------------");

            // Recorrer las líneas (omite la primera si es encabezado)
            for (int i = 1; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                
                if (!linea.isEmpty()) {
                    // Separar por comas o tabuladores según el formato de tu texto
                    String[] datos = linea.split("[,\\t]+");
                    
                    System.out.println("Registro " + i + ": " + String.join(" | ", datos));
                }
            }

        } catch (IOException e) {
            System.err.println("Error: No se pudo leer el archivo en " + rutaArchivo.toAbsolutePath());
            System.err.println("Asegúrate de que el archivo existe dentro de la carpeta 'datos/'.");
        }
    }
}
