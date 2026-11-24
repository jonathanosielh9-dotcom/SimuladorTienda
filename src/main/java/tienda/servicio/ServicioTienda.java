package tienda.servicio;

import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.EstadoPedido;
import tienda.modelo.ItemCarrito;
import tienda.modelo.Pago;
import tienda.modelo.Pedido;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioPedidos;

import java.util.Objects;

public class ServicioTienda {

    private final RepositorioArticulos repositorioArticulos;
    private final RepositorioPedidos repositorioPedidos;

    public ServicioTienda(RepositorioArticulos repositorioArticulos, RepositorioPedidos repositorioPedidos) {
        this.repositorioArticulos = Objects.requireNonNull(repositorioArticulos, "El repositorio de artículos es obligatorio");
        this.repositorioPedidos = Objects.requireNonNull(repositorioPedidos, "El repositorio de pedidos es obligatorio");
    }

    public void agregarAlCarrito(Carrito carrito, String articuloId, int cantidad) throws StockInsuficienteException {
        Objects.requireNonNull(carrito, "El carrito no puede ser nulo");
        Articulo articulo = repositorioArticulos.buscarPorId(articuloId)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + articuloId));
        carrito.agregarArticulo(articulo, cantidad);
    }

    public Pedido crearPedido(String correoUsuario, Carrito carrito) throws StockInsuficienteException {
        Pedido pedido = new Pedido(correoUsuario, carrito);
        for (ItemCarrito item : pedido.getItems()) {
            Articulo articuloActual = repositorioArticulos.buscarPorId(item.getArticulo().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + item.getArticulo().getId()));
            articuloActual.descontarStock(item.getCantidad());
        }
        repositorioPedidos.guardar(pedido);
        carrito.vaciar();
        return pedido;
    }

    public Pago procesarPago(String pedidoId, String numeroTarjeta) throws TarjetaInvalidaException {
        Pedido pedido = repositorioPedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden pagar pedidos en estado PENDIENTE");
        }
        Pago pago = new Pago(pedido.getId(), pedido.getTotal(), numeroTarjeta);
        pedido.marcarPagado();
        return pago;
    }

    public void cancelarPedido(String pedidoId) {
        Pedido pedido = repositorioPedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido ya está cancelado");
        }
        boolean estabaPendiente = pedido.getEstado() == EstadoPedido.PENDIENTE;
        pedido.cancelar();
        if (estabaPendiente) {
            for (ItemCarrito item : pedido.getItems()) {
                repositorioArticulos.buscarPorId(item.getArticulo().getId())
                        .ifPresent(articulo -> articulo.reponerStock(item.getCantidad()));
            }
        }
    }
}
