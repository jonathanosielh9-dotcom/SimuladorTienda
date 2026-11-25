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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public List<ItemCarrito> detectarArticulosSinStock(Carrito carrito) {
        Objects.requireNonNull(carrito, "El carrito no puede ser nulo");
        List<ItemCarrito> problemáticos = new ArrayList<>();
        for (ItemCarrito item : carrito.getItems().values()) {
            Optional<Articulo> actual = repositorioArticulos.buscarPorId(item.getArticulo().getId());
            if (actual.isEmpty() || actual.get().getStock() < item.getCantidad() || actual.get().getStock() <= 0) {
                problemáticos.add(item);
            }
        }
        return problemáticos;
    }

    public void validarStockCarrito(Carrito carrito) throws StockInsuficienteException {
        List<ItemCarrito> problemáticos = detectarArticulosSinStock(carrito);
        if (!problemáticos.isEmpty()) {
            ItemCarrito primero = problemáticos.get(0);
            int disponible = repositorioArticulos.buscarPorId(primero.getArticulo().getId())
                    .map(Articulo::getStock)
                    .orElse(0);
            throw new StockInsuficienteException(
                    "Stock insuficiente o agotado para '" + primero.getArticulo().getNombre()
                            + "' (solicitado: " + primero.getCantidad() + ", disponible: " + disponible + ")"
            );
        }
    }

    public boolean eliminarArticuloEnCascada(String articuloId, Iterable<Carrito> carritosActivos) {
        boolean eliminado = repositorioArticulos.eliminarPorId(articuloId);
        if (articuloId != null && carritosActivos != null) {
            for (Carrito carrito : carritosActivos) {
                if (carrito != null) {
                    carrito.quitarArticulo(articuloId);
                }
            }
        }
        return eliminado;
    }

    public Pedido crearPedido(String correoUsuario, Carrito carrito) throws StockInsuficienteException {
        validarStockCarrito(carrito);
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
