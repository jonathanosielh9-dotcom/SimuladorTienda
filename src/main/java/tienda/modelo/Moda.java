package tienda.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Moda extends Articulo {

    private static final BigDecimal FACTOR_TEMPORADA = new BigDecimal("0.85");

    private String talla;
    private String color;

    public Moda(String id, String nombre, BigDecimal precio, int stock, String talla) {
        this(id, nombre, precio, stock, talla, "Sin especificar");
    }

    public Moda(String id, String nombre, BigDecimal precio, int stock, String talla, String color) {
        super(id, nombre, Categoria.ROPA, precio, stock);
        setTalla(talla);
        setColor(color);
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        if (talla == null || talla.isBlank()) {
            throw new IllegalArgumentException("La talla no puede estar vacía");
        }
        this.talla = talla.trim().toUpperCase();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("El color no puede estar vacío");
        }
        this.color = color.trim();
    }

    @Override
    public BigDecimal getPrecioFinal() {
        BigDecimal base = Objects.requireNonNull(getPrecio());
        return base.multiply(FACTOR_TEMPORADA).setScale(2, RoundingMode.HALF_UP);
    }
}
