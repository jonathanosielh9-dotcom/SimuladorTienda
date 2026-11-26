package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Hombre extends Moda {

    public Hombre(String id, String nombre, BigDecimal precio, int stock, String talla) {
        super(id, nombre, precio, stock, talla);
    }

    @JsonCreator
    public Hombre(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("talla") String talla,
            @JsonProperty("color") String color) {
        super(id, nombre, precio, stock, talla, color);
    }
}
