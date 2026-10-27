import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "inventario")
public class Inventario {

    @JacksonXmlProperty(localName = "material")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Material> materiales = new ArrayList<>();

    // Constructor vacío
    public Inventario() {
    }

    public List<Material> getMateriales() {
        return materiales;
    }

    public void setMateriales(List<Material> materiales) {
        this.materiales = materiales;
    }
}
