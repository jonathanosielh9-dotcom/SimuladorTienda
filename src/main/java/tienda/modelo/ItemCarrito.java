package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tienda.excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemCarrito {

    private final Articulo articulo;
    private int cantidad;

    public ItemCarrito(Articulo articulo, int cantidad) throws StockInsuficienteException {
        this.articulo = Objects.requireNonNull(articulo, "El artículo no puede ser nulo");
        actualizarCantidad(cantidad);
    }

    @JsonCreator
    public ItemCarrito(
            @JsonProperty("articulo") Articulo articulo,
            @JsonProperty("cantidad") int cantidad,
            @JsonProperty("subtotal") BigDecimal subtotalIgnorado) {
        this.articulo = Objects.requireNonNull(articulo, "El artículo no puede ser nulo");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.cantidad = cantidad;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void actualizarCantidad(int nuevaCantidad) throws StockInsuficienteException {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (nuevaCantidad > articulo.getStock()) {
            throw new StockInsuficienteException(
                    "No hay suficiente stock de '" + articulo.getNombre() + "' (" + nuevaCantidad + " solicitados)"
            );
        }
        this.cantidad = nuevaCantidad;
    }

    public BigDecimal getSubtotal() {
        return articulo.getPrecioFinal().multiply(BigDecimal.valueOf(cantidad));
    }
}
