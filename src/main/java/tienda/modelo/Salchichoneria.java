package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Salchichoneria extends Alimento {

    @JsonCreator
    public Salchichoneria(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("fechaCaducidad") LocalDate fechaCaducidad) {
        super(id, nombre, precio, stock, fechaCaducidad);
    }
}
