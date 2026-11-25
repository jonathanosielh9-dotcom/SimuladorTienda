package tienda.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Alimento extends Articulo {

    private static final BigDecimal FACTOR_DESCUENTO_CADUCIDAD = new BigDecimal("0.90");

    private LocalDate fechaCaducidad;

    public Alimento(String id, String nombre, BigDecimal precio, int stock, LocalDate fechaCaducidad) {
        super(id, nombre, Categoria.ALIMENTOS, precio, stock);
        setFechaCaducidad(fechaCaducidad);
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = Objects.requireNonNull(fechaCaducidad, "La fecha de caducidad no puede ser nula");
    }

    @Override
    public BigDecimal getPrecioFinal() {
        BigDecimal base = Objects.requireNonNull(getPrecio());
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaCaducidad);
        if (diasRestantes <= 3) {
            return base.multiply(FACTOR_DESCUENTO_CADUCIDAD).setScale(2, RoundingMode.HALF_UP);
        }
        return base;
    }
}
