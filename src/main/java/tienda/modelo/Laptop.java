package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Laptop extends Tecnologia {

    private int ramGb;

    @JsonCreator
    public Laptop(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("garantiaMeses") int garantiaMeses,
            @JsonProperty("ramGb") int ramGb) {
        super(id, nombre, precio, stock, garantiaMeses);
        setRamGb(ramGb);
    }

    public int getRamGb() {
        return ramGb;
    }

    public void setRamGb(int ramGb) {
        if (ramGb <= 0) {
            throw new IllegalArgumentException("La memoria RAM en GB debe ser mayor a cero");
        }
        this.ramGb = ramGb;
    }
}
