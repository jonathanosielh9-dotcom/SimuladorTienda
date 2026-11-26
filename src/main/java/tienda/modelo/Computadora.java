package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Computadora extends Tecnologia {

    private String procesador;

    @JsonCreator
    public Computadora(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("garantiaMeses") int garantiaMeses,
            @JsonProperty("procesador") String procesador) {
        super(id, nombre, precio, stock, garantiaMeses);
        setProcesador(procesador);
    }

    public String getProcesador() {
        return procesador;
    }

    public void setProcesador(String procesador) {
        if (procesador == null || procesador.isBlank()) {
            throw new IllegalArgumentException("El procesador no puede estar vacío");
        }
        this.procesador = procesador.trim();
    }
}
