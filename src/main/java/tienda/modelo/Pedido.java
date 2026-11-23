package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Pedido {

    private final String id;
    private final String correoUsuario;
    private final List<ItemCarrito> items;
    private final BigDecimal total;
    private final Instant fechaCreacion;
    private EstadoPedido estado;

    public Pedido(String correoUsuario, Carrito carrito) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario no puede estar vacío");
        }
        Objects.requireNonNull(carrito, "El carrito no puede ser nulo");
        if (carrito.estaVacio()) {
            throw new IllegalArgumentException("No se puede crear un pedido con el carrito vacío");
        }
        this.id = UUID.randomUUID().toString();
        this.correoUsuario = correoUsuario.trim().toLowerCase();
        this.items = new ArrayList<>(carrito.getItems().values());
        this.total = carrito.calcularTotal();
        this.fechaCreacion = Instant.now();
        this.estado = EstadoPedido.PENDIENTE;
    }

    @JsonCreator
    public Pedido(
            @JsonProperty("id") String id,
            @JsonProperty("correoUsuario") String correoUsuario,
            @JsonProperty("items") List<ItemCarrito> items,
            @JsonProperty("total") BigDecimal total,
            @JsonProperty("fechaCreacion") Instant fechaCreacion,
            @JsonProperty("estado") EstadoPedido estado) {
        this.id = Objects.requireNonNull(id, "El id del pedido no puede ser nulo");
        this.correoUsuario = Objects.requireNonNull(correoUsuario, "El correo del usuario no puede ser nulo");
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.total = Objects.requireNonNull(total, "El total no puede ser nulo");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
    }

    public String getId() {
        return id;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void marcarPagado() {
        this.estado = EstadoPedido.PAGADO;
    }

    public void cancelar() {
        if (this.estado == EstadoPedido.PAGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido ya pagado");
        }
        this.estado = EstadoPedido.CANCELADO;
    }
}
