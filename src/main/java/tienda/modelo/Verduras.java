package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Verduras extends Alimento {

    private boolean organica;

    @JsonCreator
    public Verduras(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("fechaCaducidad") LocalDate fechaCaducidad,
            @JsonProperty("organica") boolean organica) {
        super(id, nombre, precio, stock, fechaCaducidad);
        this.organica = organica;
    }

    public boolean isOrganica() {
        return organica;
    }

    public void setOrganica(boolean organica) {
        this.organica = organica;
    }
}
