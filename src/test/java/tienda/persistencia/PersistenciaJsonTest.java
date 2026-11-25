package tienda.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.Categoria;
import tienda.modelo.Pedido;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaJsonTest {

    @TempDir
    Path tempDir;

    @Test
    void guardarYCargarArticulosYPedidos() throws Exception {
        PersistenciaJson persistencia = new PersistenciaJson();
        Path archivoArticulos = tempDir.resolve("articulos.json");
        Path archivoPedidos = tempDir.resolve("pedidos.json");

        Articulo teclado = new Articulo("ART-10", "Teclado Mecánico", Categoria.ELECTRONICA, new BigDecimal("1250.50"), 8);
        persistencia.guardarArticulos(archivoArticulos, List.of(teclado));

        Carrito carrito = new Carrito();
        carrito.agregarArticulo(teclado, 2);
        Pedido pedido = new Pedido("comprador@correo.com", carrito);
        persistencia.guardarPedidos(archivoPedidos, List.of(pedido));

        List<Articulo> articulosLeidos = persistencia.cargarArticulos(archivoArticulos);
        List<Pedido> pedidosLeidos = persistencia.cargarPedidos(archivoPedidos);

        assertEquals(1, articulosLeidos.size());
        assertEquals("ART-10", articulosLeidos.get(0).getId());
        assertEquals(new BigDecimal("1250.50"), articulosLeidos.get(0).getPrecio());
        assertEquals(8, articulosLeidos.get(0).getStock());

        assertEquals(1, pedidosLeidos.size());
        assertEquals(pedido.getId(), pedidosLeidos.get(0).getId());
        assertEquals(new BigDecimal("2501.00"), pedidosLeidos.get(0).getTotal());
        assertEquals(pedido.getFechaCreacion(), pedidosLeidos.get(0).getFechaCreacion());
    }

    @Test
    void cargarArchivoInexistente_retornaListaVacia() throws Exception {
        PersistenciaJson persistencia = new PersistenciaJson();
        Path inexistente = tempDir.resolve("no-existe.json");

        assertTrue(persistencia.cargarArticulos(inexistente).isEmpty());
        assertTrue(persistencia.cargarPedidos(inexistente).isEmpty());
    }

    @Test
    void repositoriosGuardanYCarganDesdeJsonReemplazandoEstadoPrevio() throws Exception {
        PersistenciaJson persistencia = new PersistenciaJson();
        Path archivoArticulos = tempDir.resolve("repo-articulos.json");
        Path archivoPedidos = tempDir.resolve("repo-pedidos.json");

        RepositorioArticulos repoArticulosOrigen = new RepositorioArticulos();
        Articulo mouse = new Articulo("ART-20", "Mouse Inalámbrico", Categoria.ELECTRONICA, new BigDecimal("499.00"), 15);
        repoArticulosOrigen.guardar(mouse);
        repoArticulosOrigen.guardarEnJson(archivoArticulos, persistencia);

        Carrito carrito = new Carrito();
        carrito.agregarArticulo(mouse, 3);
        Pedido pedido = new Pedido("cliente@ejemplo.com", carrito);

        RepositorioPedidos repoPedidosOrigen = new RepositorioPedidos();
        repoPedidosOrigen.guardar(pedido);
        repoPedidosOrigen.guardarEnJson(archivoPedidos, persistencia);

        RepositorioArticulos repoArticulosDestino = new RepositorioArticulos();
        repoArticulosDestino.guardar(new Articulo("ART-VIEJO", "Artículo Temporal", Categoria.ALIMENTOS, new BigDecimal("10.00"), 1));
        repoArticulosDestino.cargarDesdeJson(archivoArticulos, persistencia);

        RepositorioPedidos repoPedidosDestino = new RepositorioPedidos();
        repoPedidosDestino.cargarDesdeJson(archivoPedidos, persistencia);

        assertEquals(1, repoArticulosDestino.listarTodos().size());
        assertTrue(repoArticulosDestino.buscarPorId("ART-VIEJO").isEmpty());
        assertEquals("Mouse Inalámbrico", repoArticulosDestino.buscarPorId("ART-20").orElseThrow().getNombre());

        assertEquals(1, repoPedidosDestino.listarTodos().size());
        assertEquals(pedido.getId(), repoPedidosDestino.buscarPorId(pedido.getId()).orElseThrow().getId());
        assertEquals(new BigDecimal("1497.00"), repoPedidosDestino.buscarPorId(pedido.getId()).orElseThrow().getTotal());
    }
}
