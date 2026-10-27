import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class InventarioApp {

    private static final String FICHERO_ORIGINAL = "datos/inventario.xml";
    private static final String FICHERO_ACTUALIZADO = "datos/inventario_actualizado.xml";
    private static final String FICHERO_CSV = "datos/inventario.csv";

    public static void main(String[] args) {
        try {
            XmlMapper xmlMapper = crearXmlMapper();

            // ========== 2. Leer y mostrar el inventario original ==========
            System.out.println("=== INVENTARIO ORIGINAL ===");
            Inventario inventario = xmlMapper.readValue(Paths.get(FICHERO_ORIGINAL).toFile(), Inventario.class);
            mostrarInventario(inventario);

            // ========== 3. Buscar un material y modificar sus unidades ==========
            Scanner sc = new Scanner(System.in);
            System.out.print("\nIntroduce el código del material a modificar (ej. M01): ");
            String codigoBuscado = sc.nextLine().trim();

            Material materialEncontrado = buscarMaterial(inventario, codigoBuscado);
            if (materialEncontrado == null) {
                System.out.println("Material no encontrado");
                System.out.println("No se ha creado ni sobrescrito ningún fichero de salida.");
                sc.close();
                return;
            }

            System.out.println("Material encontrado: " + materialEncontrado);
            int unidadesAnteriores = materialEncontrado.getUnidades();

            // Validar cantidad (>= 0)
            int nuevasUnidades = pedirUnidadesValidas(sc);
            materialEncontrado.setUnidades(nuevasUnidades);

            System.out.println("Material actualizado: " + materialEncontrado.getNombre()
                    + " | unidades anteriores: " + unidadesAnteriores
                    + " | unidades nuevas: " + nuevasUnidades);

            // ========== 4. Guardar y recuperar el inventario actualizado ==========
            // Guardar con formato legible y UTF-8
            xmlMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(Paths.get(FICHERO_ACTUALIZADO).toFile(), inventario);
            System.out.println("\nInventario guardado en: " + FICHERO_ACTUALIZADO);

            // Volver a leer el XML actualizado
            Inventario inventarioActualizado = xmlMapper.readValue(
                    Paths.get(FICHERO_ACTUALIZADO).toFile(), Inventario.class);

            System.out.println("\n=== INVENTARIO ACTUALIZADO (segunda lectura) ===");
            // Mostrar unidades del material modificado
            Material matMod = buscarMaterial(inventarioActualizado, codigoBuscado);
            if (matMod != null) {
                System.out.println("Unidades del material " + codigoBuscado + ": " + matMod.getUnidades());
            }
            int suma = sumarUnidades(inventarioActualizado);
            System.out.println("Suma total de unidades: " + suma);

            // ========== 5. Convertir a CSV ==========
            int filas = escribirCsv(inventarioActualizado, FICHERO_CSV);
            System.out.println("\nCSV generado en: " + FICHERO_CSV);
            System.out.println("Filas de datos exportadas: " + filas);

            sc.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Configura el XmlMapper con UTF-8 y formato legible */
    private static XmlMapper crearXmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        return mapper;
    }

    /** Muestra todos los materiales y los totales */
    private static void mostrarInventario(Inventario inventario) {
        int totalMateriales = 0;
        int totalUnidades = 0;

        for (Material m : inventario.getMateriales()) {
            System.out.println(m.getCodigo() + " | " + m.getNombre() + " | " + m.getUnidades() + " unidades");
            totalMateriales++;
            totalUnidades += m.getUnidades();
        }

        System.out.println("Materiales registrados: " + totalMateriales);
        System.out.println("Unidades totales: " + totalUnidades);
    }

    /** Busca un material por código exacto */
    private static Material buscarMaterial(Inventario inventario, String codigo) {
        for (Material m : inventario.getMateriales()) {
            if (codigo.equals(m.getCodigo())) {
                return m;
            }
        }
        return null;
    }

    /** Pide un número entero >= 0, rechazando valores incorrectos */
    private static int pedirUnidadesValidas(Scanner sc) {
        while (true) {
            System.out.print("Introduce la nueva cantidad de unidades: ");
            String entrada = sc.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor < 0) {
                    System.out.println("Error: las unidades no pueden ser negativas. Inténtalo de nuevo.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: debes introducir un número entero válido. Inténtalo de nuevo.");
            }
        }
    }

    /** Suma las unidades de todos los materiales */
    private static int sumarUnidades(Inventario inventario) {
        int suma = 0;
        for (Material m : inventario.getMateriales()) {
            suma += m.getUnidades();
        }
        return suma;
    }

    /**
     * Escribe el inventario en CSV (UTF-8) con separador punto y coma.
     * Devuelve el número de filas de datos (sin contar la cabecera).
     */
    private static int escribirCsv(Inventario inventario, String ruta) throws Exception {
        Path path = Paths.get(ruta);
        int filas = 0;

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Cabecera
            writer.write("codigo;nombre;unidades");
            writer.newLine();

            // Datos
            for (Material m : inventario.getMateriales()) {
                writer.write(m.getCodigo() + ";" + m.getNombre() + ";" + m.getUnidades());
                writer.newLine();
                filas++;
            }
        }
        return filas;
    }
}
