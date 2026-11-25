package tienda.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UsuarioRegistrado extends Usuario {

    private final Carrito carrito;
    private final List<Pedido> historial;

    public UsuarioRegistrado(String correo, String contrasena) {
        this(correo, contrasena, new Carrito(), new ArrayList<>());
    }

    public UsuarioRegistrado(String correo, String contrasena, Carrito carrito, List<Pedido> historial) {
        super(correo, contrasena);
        this.carrito = Objects.requireNonNull(carrito, "El carrito no puede ser nulo");
        this.historial = new ArrayList<>(Objects.requireNonNull(historial, "El historial no puede ser nulo"));
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public List<Pedido> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public void agregarPedidoAlHistorial(Pedido pedido) {
        historial.add(Objects.requireNonNull(pedido, "El pedido no puede ser nulo"));
    }

    public boolean eliminarCuenta(String contrasenaConfirmacion) {
        return autenticar(contrasenaConfirmacion);
    }
}
