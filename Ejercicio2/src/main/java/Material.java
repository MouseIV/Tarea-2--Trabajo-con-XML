import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "material")
public class Material {

    @JacksonXmlProperty(localName = "codigo")
    private String codigo;

    @JacksonXmlProperty(localName = "nombre")
    private String nombre;

    @JacksonXmlProperty(localName = "unidades")
    private int unidades;

    // Constructor vacío (obligatorio para Jackson)
    public Material() {
    }

    public Material(String codigo, String nombre, int unidades) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidades = unidades;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    @Override
    public String toString() {
        return codigo + " | " + nombre + " | " + unidades + " unidades";
    }
}
