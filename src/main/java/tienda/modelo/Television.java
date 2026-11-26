package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Television extends Tecnologia {

    private int pulgadas;
    private String resolucion;

    @JsonCreator
    public Television(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("garantiaMeses") int garantiaMeses,
            @JsonProperty("pulgadas") int pulgadas,
            @JsonProperty("resolucion") String resolucion) {
        super(id, nombre, precio, stock, garantiaMeses);
        setPulgadas(pulgadas);
        setResolucion(resolucion);
    }

    public int getPulgadas() {
        return pulgadas;
    }

    public void setPulgadas(int pulgadas) {
        if (pulgadas <= 0) {
            throw new IllegalArgumentException("Las pulgadas deben ser mayores a cero");
        }
        this.pulgadas = pulgadas;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        if (resolucion == null || resolucion.isBlank()) {
            throw new IllegalArgumentException("La resolución no puede estar vacía");
        }
        this.resolucion = resolucion.trim();
    }
}
