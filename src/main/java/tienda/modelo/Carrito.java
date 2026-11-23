package tienda.modelo;

import tienda.excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Carrito {

    private final Map<String, ItemCarrito> items = new LinkedHashMap<>();

    public void agregarArticulo(Articulo articulo, int cantidad) throws StockInsuficienteException {
        Objects.requireNonNull(articulo, "El artículo no puede ser nulo");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        ItemCarrito existente = items.get(articulo.getId());
        int nuevaCantidadTotal = (existente != null ? existente.getCantidad() : 0) + cantidad;
        if (nuevaCantidadTotal > articulo.getStock()) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para agregar " + cantidad + " de '" + articulo.getNombre() + "'"
            );
        }
        if (existente != null) {
            existente.actualizarCantidad(nuevaCantidadTotal);
        } else {
            items.put(articulo.getId(), new ItemCarrito(articulo, cantidad));
        }
    }

    public void quitarArticulo(String articuloId) {
        if (articuloId != null) {
            items.remove(articuloId);
        }
    }

    public void vaciar() {
        items.clear();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public Map<String, ItemCarrito> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public BigDecimal calcularTotal() {
        return items.values().stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
