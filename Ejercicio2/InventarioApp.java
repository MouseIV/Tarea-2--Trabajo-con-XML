import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.BufferedWriter;
import java.io.File;
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

            // 1. Leer y mostrar el inventario original
            System.out.println("=== INVENTARIO ORIGINAL ===");
            Inventario inventario = xmlMapper.readValue(new File(FICHERO_ORIGINAL), Inventario.class);
            mostrarInventario(inventario);

            // 2. Buscar material
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

            // 3. Modificar unidades con validación
            System.out.println("Material encontrado: " + materialEncontrado);
            int unidadesAnteriores = materialEncontrado.getUnidades();
            
            int nuevasUnidades = pedirUnidadesValidas(sc);
            materialEncontrado.setUnidades(nuevasUnidades);

            System.out.println("Material actualizado: " + materialEncontrado.getNombre()
                    + " | unidades anteriores: " + unidadesAnteriores
                    + " | unidades nuevas: " + nuevasUnidades);

            // 4. Guardar y recuperar el inventario actualizado
            xmlMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FICHERO_ACTUALIZADO), inventario);
            System.out.println("\nInventario guardado en: " + FICHERO_ACTUALIZADO);

            Inventario inventarioActualizado = xmlMapper.readValue(new File(FICHERO_ACTUALIZADO), Inventario.class);

            System.out.println("\n=== INVENTARIO ACTUALIZADO (segunda lectura) ===");
            Material matMod = buscarMaterial(inventarioActualizado, codigoBuscado);
            if (matMod != null) {
                System.out.println("Unidades del material " + codigoBuscado + ": " + matMod.getUnidades());
            }
            int suma = sumarUnidades(inventarioActualizado);
            System.out.println("Suma total de unidades: " + suma);

            // 5. Convertir a CSV
            int filas = escribirCsv(inventarioActualizado, FICHERO_CSV);
            System.out.println("\nCSV generado en: " + FICHERO_CSV);
            System.out.println("Filas de datos exportadas: " + filas);

            sc.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static XmlMapper crearXmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        return mapper;
    }

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

    private static Material buscarMaterial(Inventario inventario, String codigo) {
        for (Material m : inventario.getMateriales()) {
            if (codigo.equals(m.getCodigo())) {
                return m;
            }
        }
        return null;
    }

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

    private static int sumarUnidades(Inventario inventario) {
        int suma = 0;
        for (Material m : inventario.getMateriales()) {
            suma += m.getUnidades();
        }
        return suma;
    }

    private static int escribirCsv(Inventario inventario, String ruta) throws Exception {
        Path path = Paths.get(ruta);
        int filas = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("codigo;nombre;unidades");
            writer.newLine();
            for (Material m : inventario.getMateriales()) {
                writer.write(m.getCodigo() + ";" + m.getNombre() + ";" + m.getUnidades());
                writer.newLine();
                filas++;
            }
        }
        return filas;
    }
}