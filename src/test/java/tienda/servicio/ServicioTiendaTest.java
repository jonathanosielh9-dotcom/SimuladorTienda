package tienda.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.Categoria;
import tienda.modelo.EstadoPedido;
import tienda.modelo.Pago;
import tienda.modelo.Pedido;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioPedidos;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioTiendaTest {

    private RepositorioArticulos repoArticulos;
    private RepositorioPedidos repoPedidos;
    private ServicioTienda servicioTienda;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        repoArticulos = new RepositorioArticulos();
        repoPedidos = new RepositorioPedidos();
        servicioTienda = new ServicioTienda(repoArticulos, repoPedidos);
        carrito = new Carrito();

        repoArticulos.guardar(new Articulo("ART-1", "Teclado Mecánico", Categoria.ELECTRONICA, new BigDecimal("1200.00"), 10));
        repoArticulos.guardar(new Articulo("ART-2", "Mouse Inalámbrico", Categoria.ELECTRONICA, new BigDecimal("450.00"), 5));
    }

    @Test
    void agregarAlCarritoSumaCantidadYRespetaStock() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 2);
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 1);

        assertFalse(carrito.estaVacio());
        assertEquals(3, carrito.getItems().get("ART-1").getCantidad());
        assertEquals(new BigDecimal("3600.00"), carrito.calcularTotal());
    }

    @Test
    void agregarAlCarritoLanzaExcepcionSiExcedeStock() {
        assertThrows(StockInsuficienteException.class,
                () -> servicioTienda.agregarAlCarrito(carrito, "ART-2", 6));
    }

    @Test
    void agregarAlCarritoLanzaExcepcionSiArticuloNoExiste() {
        assertThrows(IllegalArgumentException.class,
                () -> servicioTienda.agregarAlCarrito(carrito, "NO-EXISTE", 1));
    }

    @Test
    void crearPedidoDescuentaStockGuardaPedidoYVaciaCarrito() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 4);
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 2);

        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);

        assertNotNull(pedido.getId());
        assertEquals("cliente@correo.com", pedido.getCorreoUsuario());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(new BigDecimal("5700.00"), pedido.getTotal());

        assertTrue(carrito.estaVacio());
        assertEquals(6, repoArticulos.buscarPorId("ART-1").orElseThrow().getStock());
        assertEquals(3, repoArticulos.buscarPorId("ART-2").orElseThrow().getStock());
        assertTrue(repoPedidos.buscarPorId(pedido.getId()).isPresent());
    }

    @Test
    void procesarPagoMarcaPedidoComoPagado() throws StockInsuficienteException, TarjetaInvalidaException {
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        Pedido pedido = servicioTienda.crearPedido("comprador@correo.com", carrito);

        Pago pago = servicioTienda.procesarPago(pedido.getId(), "4111222233334444");

        assertNotNull(pago);
        assertEquals(EstadoPedido.PAGADO, repoPedidos.buscarPorId(pedido.getId()).orElseThrow().getEstado());
    }

    @Test
    void procesarPagoRechazaPedidoYaPagado() throws StockInsuficienteException, TarjetaInvalidaException {
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        Pedido pedido = servicioTienda.crearPedido("comprador@correo.com", carrito);
        servicioTienda.procesarPago(pedido.getId(), "4111222233334444");

        assertThrows(IllegalStateException.class,
                () -> servicioTienda.procesarPago(pedido.getId(), "4111222233334444"));
    }

    @Test
    void procesarPagoRechazaTarjetaInvalida() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        Pedido pedido = servicioTienda.crearPedido("comprador@correo.com", carrito);

        assertThrows(TarjetaInvalidaException.class,
                () -> servicioTienda.procesarPago(pedido.getId(), "123"));
    }

    @Test
    void buscarPedidosPorUsuarioFiltraPorCorreoSinImportarMayusculas() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 1);
        Pedido p1 = servicioTienda.crearPedido("ana@correo.com", carrito);

        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        servicioTienda.crearPedido("luis@correo.com", carrito);

        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        Pedido p2 = servicioTienda.crearPedido("ana@correo.com", carrito);

        List<Pedido> pedidosAna = repoPedidos.buscarPorUsuario("  ANA@CORREO.COM  ");

        assertEquals(2, pedidosAna.size());
        assertTrue(pedidosAna.stream().anyMatch(p -> p.getId().equals(p1.getId())));
        assertTrue(pedidosAna.stream().anyMatch(p -> p.getId().equals(p2.getId())));
        assertTrue(repoPedidos.buscarPorUsuario("nadie@correo.com").isEmpty());
        assertTrue(repoPedidos.buscarPorUsuario("   ").isEmpty());
    }

    @Test
    void cancelarPedidoPendienteCambiaEstadoYReponeStock() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 3);
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 2);
        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);

        assertEquals(7, repoArticulos.buscarPorId("ART-1").orElseThrow().getStock());
        assertEquals(3, repoArticulos.buscarPorId("ART-2").orElseThrow().getStock());

        servicioTienda.cancelarPedido(pedido.getId());

        assertEquals(EstadoPedido.CANCELADO, repoPedidos.buscarPorId(pedido.getId()).orElseThrow().getEstado());
        assertEquals(10, repoArticulos.buscarPorId("ART-1").orElseThrow().getStock());
        assertEquals(5, repoArticulos.buscarPorId("ART-2").orElseThrow().getStock());
    }

    @Test
    void cancelarPedidoPagadoLanzaExcepcionYNoReponeStock() throws StockInsuficienteException, TarjetaInvalidaException {
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 2);
        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);
        servicioTienda.procesarPago(pedido.getId(), "4111222233334444");

        assertThrows(IllegalStateException.class,
                () -> servicioTienda.cancelarPedido(pedido.getId()));
        assertEquals(EstadoPedido.PAGADO, repoPedidos.buscarPorId(pedido.getId()).orElseThrow().getEstado());
        assertEquals(8, repoArticulos.buscarPorId("ART-1").orElseThrow().getStock());
    }

    @Test
    void cancelarPedidoYaCanceladoLanzaExcepcion() throws StockInsuficienteException {
        servicioTienda.agregarAlCarrito(carrito, "ART-2", 1);
        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);
        servicioTienda.cancelarPedido(pedido.getId());

        assertThrows(IllegalStateException.class,
                () -> servicioTienda.cancelarPedido(pedido.getId()));
    }

    @Test
    void cancelarPedidoInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> servicioTienda.cancelarPedido("ID-INEXISTENTE"));
    }
}
