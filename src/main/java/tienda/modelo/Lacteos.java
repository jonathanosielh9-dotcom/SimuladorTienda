package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Lacteos extends Alimento {

    private boolean deslactosado;

    @JsonCreator
    public Lacteos(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("fechaCaducidad") LocalDate fechaCaducidad,
            @JsonProperty("deslactosado") boolean deslactosado) {
        super(id, nombre, precio, stock, fechaCaducidad);
        this.deslactosado = deslactosado;
    }

    public boolean isDeslactosado() {
        return deslactosado;
    }

    public void setDeslactosado(boolean deslactosado) {
        this.deslactosado = deslactosado;
    }
}
