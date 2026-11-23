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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioTiendaTest {

    private RepositorioArticulos repositorioArticulos;
    private RepositorioPedidos repositorioPedidos;
    private ServicioTienda servicioTienda;
    private Articulo audifonos;

    @BeforeEach
    void setUp() {
        repositorioArticulos = new RepositorioArticulos();
        repositorioPedidos = new RepositorioPedidos();
        servicioTienda = new ServicioTienda(repositorioArticulos, repositorioPedidos);

        audifonos = new Articulo("ART-1", "Audífonos", Categoria.ELECTRONICA, new BigDecimal("500.00"), 10);
        repositorioArticulos.guardar(audifonos);
    }

    @Test
    void agregarAlCarrito_lanzaExcepcionCuandoExcedeStock() {
        Carrito carrito = new Carrito();

        assertThrows(StockInsuficienteException.class,
                () -> servicioTienda.agregarAlCarrito(carrito, "ART-1", 11));
        assertTrue(carrito.estaVacio());
    }

    @Test
    void crearPedido_descuentaStockYGuardaPedido() throws StockInsuficienteException {
        Carrito carrito = new Carrito();
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 3);

        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);

        assertNotNull(pedido.getId());
        assertEquals("cliente@correo.com", pedido.getCorreoUsuario());
        assertEquals(new BigDecimal("1500.00"), pedido.getTotal());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(7, audifonos.getStock());
        assertTrue(carrito.estaVacio());
        assertFalse(repositorioPedidos.buscarPorId(pedido.getId()).isEmpty());
    }

    @Test
    void procesarPago_validaTarjetaYMarcaPedidoPagado() throws Exception {
        Carrito carrito = new Carrito();
        servicioTienda.agregarAlCarrito(carrito, "ART-1", 2);
        Pedido pedido = servicioTienda.crearPedido("cliente@correo.com", carrito);

        assertThrows(TarjetaInvalidaException.class,
                () -> servicioTienda.procesarPago(pedido.getId(), "1234"));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());

        Pago pago = servicioTienda.procesarPago(pedido.getId(), "4111 2222 3333 4444");

        assertNotNull(pago.getId());
        assertEquals(pedido.getId(), pago.getPedidoId());
        assertEquals(new BigDecimal("1000.00"), pago.getMonto());
        assertEquals("4444", pago.getTerminacionTarjeta());
        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
    }
}
