package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Celular extends Tecnologia {

    private int almacenamientoGb;

    @JsonCreator
    public Celular(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("garantiaMeses") int garantiaMeses,
            @JsonProperty("almacenamientoGb") int almacenamientoGb) {
        super(id, nombre, precio, stock, garantiaMeses);
        setAlmacenamientoGb(almacenamientoGb);
    }

    public int getAlmacenamientoGb() {
        return almacenamientoGb;
    }

    public void setAlmacenamientoGb(int almacenamientoGb) {
        if (almacenamientoGb <= 0) {
            throw new IllegalArgumentException("El almacenamiento en GB debe ser mayor a cero");
        }
        this.almacenamientoGb = almacenamientoGb;
    }
}
