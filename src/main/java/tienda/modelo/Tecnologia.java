package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Tecnologia extends Articulo {

    private static final BigDecimal FACTOR_IVA = new BigDecimal("1.16");

    private int garantiaMeses;

    @JsonCreator
    public Tecnologia(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("garantiaMeses") int garantiaMeses) {
        super(id, nombre, Categoria.ELECTRONICA, precio, stock);
        setGarantiaMeses(garantiaMeses);
    }

    public int getGarantiaMeses() {
        return garantiaMeses;
    }

    public void setGarantiaMeses(int garantiaMeses) {
        if (garantiaMeses < 0) {
            throw new IllegalArgumentException("La garantía en meses no puede ser negativa");
        }
        this.garantiaMeses = garantiaMeses;
    }

    @Override
    public BigDecimal getPrecioFinal() {
        BigDecimal base = Objects.requireNonNull(getPrecio());
        return base.multiply(FACTOR_IVA).setScale(2, RoundingMode.HALF_UP);
    }
}
