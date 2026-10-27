import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Scanner;

public class CatalogoPeliculas {

    public static void main(String[] args) {
        try {
            // 1. Leer y mostrar el catálogo original
            System.out.println("=== CATÁLOGO ORIGINAL ===");
            Document doc = cargarDocumento("datos/peliculas.xml");
            mostrarCatalogo(doc);

            // 2. Localizar una película por su identificador y cambiar copias
            Scanner sc = new Scanner(System.in);
            System.out.print("\nIntroduce el identificador de la película a modificar (ej. P01): ");
            String idBuscado = sc.nextLine().trim();

            Element pelicula = buscarPeliculaPorId(doc, idBuscado);
            if (pelicula == null) {
                System.out.println("No se encontró ninguna película con id = " + idBuscado);
                System.out.println("No se ha modificado ni sobrescrito el fichero.");
                sc.close();
                return;   // No se guarda nada
            }

            // Mostrar datos actuales
            String titulo = obtenerTextoElemento(pelicula, "titulo");
            String genero = obtenerTextoElemento(pelicula, "genero");
            int copiasActuales = Integer.parseInt(obtenerTextoElemento(pelicula, "copias"));
            System.out.println("Película encontrada: " + idBuscado + " | " + titulo + " | " + genero + " | " + copiasActuales + " copias");

            // Pedir y validar el nuevo número de copias (entero >= 0)
            int nuevasCopias = -1;
            boolean cantidadValida = false;
            while (!cantidadValida) {
                System.out.print("Introduce el nuevo número de copias: ");
                String entrada = sc.nextLine().trim();
                try {
                    nuevasCopias = Integer.parseInt(entrada);
                    if (nuevasCopias < 0) {
                        System.out.println("Error: el número de copias no puede ser negativo. Inténtalo de nuevo.");
                    } else {
                        cantidadValida = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: debes introducir un número entero válido. Inténtalo de nuevo.");
                }
            }

            // Actualizar el valor de copias
            NodeList nodosCopias = pelicula.getElementsByTagName("copias");
            if (nodosCopias.getLength() > 0) {
                nodosCopias.item(0).setTextContent(String.valueOf(nuevasCopias));
            }

            System.out.println("Copias actualizadas correctamente.");

            // 3. Guardar el catálogo completo en el mismo XML
            String fichero = "datos/peliculas.xml";
            guardarDocumento(doc, fichero);
            System.out.println("\nCatálogo guardado en: " + fichero);

            // 4. Volver a leerlo para comprobar el cambio
            System.out.println("\n=== CATÁLOGO DESPUÉS DE LA MODIFICACIÓN ===");
            Document docModificado = cargarDocumento(fichero);
            mostrarCatalogo(docModificado);

            sc.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Carga un fichero XML en un Document usando DOM */
    private static Document cargarDocumento(String ruta) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(ruta));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /** Muestra todas las películas del catálogo y los totales */
    private static void mostrarCatalogo(Document doc) {
        NodeList listaPeliculas = doc.getElementsByTagName("pelicula");
        int totalPeliculas = 0;
        int totalCopias = 0;

        for (int i = 0; i < listaPeliculas.getLength(); i++) {
            Element pelicula = (Element) listaPeliculas.item(i);

            // El id es un atributo
            String id = pelicula.getAttribute("id");

            // titulo, genero y copias son elementos hijos
            String titulo = obtenerTextoElemento(pelicula, "titulo");
            String genero = obtenerTextoElemento(pelicula, "genero");
            String copiasStr = obtenerTextoElemento(pelicula, "copias");
            int copias = Integer.parseInt(copiasStr);

            String textoCopias = (copias == 1) ? "1 copia" : copias + " copias";
            System.out.println(id + " | " + titulo + " | " + genero + " | " + textoCopias);

            totalPeliculas++;
            totalCopias += copias;
        }

        System.out.println("Películas registradas: " + totalPeliculas);
        System.out.println("Copias totales: " + totalCopias);
    }

    /** Busca una película por su atributo id */
    private static Element buscarPeliculaPorId(Document doc, String idBuscado) {
        NodeList listaPeliculas = doc.getElementsByTagName("pelicula");
        for (int i = 0; i < listaPeliculas.getLength(); i++) {
            Element pelicula = (Element) listaPeliculas.item(i);
            if (idBuscado.equals(pelicula.getAttribute("id"))) {
                return pelicula;
            }
        }
        return null;
    }

    /** Obtiene el texto de un elemento hijo por nombre de etiqueta */
    private static String obtenerTextoElemento(Element padre, String nombreEtiqueta) {
        NodeList nodos = padre.getElementsByTagName(nombreEtiqueta);
        if (nodos.getLength() > 0) {
            return nodos.item(0).getTextContent().trim();
        }
        return "";
    }

    /** Guarda un Document en un fichero XML */
    private static void guardarDocumento(Document doc, String ruta) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(ruta));
        transformer.transform(source, result);
    }
}