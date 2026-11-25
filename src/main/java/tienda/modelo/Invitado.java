package tienda.modelo;

import tienda.excepciones.StockInsuficienteException;

import java.util.Objects;

public class Invitado extends Usuario {

    private final Carrito carritoEnMemoria;

    public Invitado() {
        super("invitado@local", "invitado-sesion");
        this.carritoEnMemoria = new Carrito();
    }

    public Invitado(String correo, String contrasena) {
        super(correo, contrasena);
        this.carritoEnMemoria = new Carrito();
    }

    public Carrito getCarritoEnMemoria() {
        return carritoEnMemoria;
    }

    public void agregarAlCarrito(Articulo articulo, int cantidad) throws StockInsuficienteException {
        Objects.requireNonNull(articulo, "El artículo no puede ser nulo");
        carritoEnMemoria.agregarArticulo(articulo, cantidad);
    }

    public void limpiarSesion() {
        carritoEnMemoria.vaciar();
    }
}
