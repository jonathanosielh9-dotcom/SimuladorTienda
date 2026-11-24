package tienda.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ArticuloTecnologia extends Articulo {

    private int garantiaMeses;

    public ArticuloTecnologia(String id, String nombre, BigDecimal precio, int stock, int garantiaMeses) {
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
        // Pequeño recargo por cobertura extendida si la garantía supera 12 meses (5%)
        if (garantiaMeses > 12) {
            return getPrecio().multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
        }
        return Objects.requireNonNull(getPrecio());
    }
}
