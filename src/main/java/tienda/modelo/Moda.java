package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

public class Moda extends Articulo {

    private static final BigDecimal FACTOR_TEMPORADA = new BigDecimal("0.85");

    private String talla;
    private String color;

    public Moda(String id, String nombre, BigDecimal precio, int stock, String talla) {
        this(id, nombre, precio, stock, talla, "Sin especificar");
    }

    @JsonCreator
    public Moda(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock,
            @JsonProperty("talla") String talla,
            @JsonProperty("color") String color) {
        super(id, nombre, Categoria.ROPA, precio, stock);
        setTalla(talla);
        setColor(color != null && !color.isBlank() ? color : "Sin especificar");
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

    public static boolean esTemporadaDescuento(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        Month mes = fecha.getMonth();
        return mes == Month.NOVEMBER || mes == Month.DECEMBER;
    }

    public boolean esTemporadaDescuento() {
        return esTemporadaDescuento(LocalDate.now());
    }

    @Override
    public BigDecimal getPrecioFinal() {
        BigDecimal base = Objects.requireNonNull(getPrecio());
        if (esTemporadaDescuento()) {
            return base.multiply(FACTOR_TEMPORADA).setScale(2, RoundingMode.HALF_UP);
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }
}
