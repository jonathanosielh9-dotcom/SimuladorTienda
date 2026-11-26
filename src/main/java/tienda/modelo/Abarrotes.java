package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Abarrotes extends Alimento {

    @JsonCreator
    public Abarrotes(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("fechaCaducidad") LocalDate fechaCaducidad) {
        super(id, nombre, precio, stock, fechaCaducidad);
    }
}
