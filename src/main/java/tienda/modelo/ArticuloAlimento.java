package tienda.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class ArticuloAlimento extends Articulo {

    private LocalDate fechaCaducidad;

    public ArticuloAlimento(String id, String nombre, BigDecimal precio, int stock, LocalDate fechaCaducidad) {
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
        return getPrecio();
    }
}
