package tienda.consola;

import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.Categoria;
import tienda.modelo.ItemCarrito;
import tienda.modelo.Pago;
import tienda.modelo.Pedido;
import tienda.persistencia.PersistenciaJson;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioPedidos;
import tienda.servicio.ServicioTienda;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsolaTienda {

    private static final Path ARCHIVO_ARTICULOS = Path.of("datos", "articulos.json");
    private static final Path ARCHIVO_PEDIDOS = Path.of("datos", "pedidos.json");

    private final RepositorioArticulos repoArticulos;
    private final RepositorioPedidos repoPedidos;
    private final ServicioTienda servicioTienda;
    private final PersistenciaJson persistenciaJson;
    private final Carrito carrito = new Carrito();
    private volatile boolean guardadoEnSalida = false;

    public ConsolaTienda(
            RepositorioArticulos repoArticulos,
            RepositorioPedidos repoPedidos,
            ServicioTienda servicioTienda,
            PersistenciaJson persistenciaJson) {
        this.repoArticulos = repoArticulos;
        this.repoPedidos = repoPedidos;
        this.servicioTienda = servicioTienda;
        this.persistenciaJson = persistenciaJson;
    }

    public static void main(String[] args) {
        RepositorioArticulos repoArticulos = new RepositorioArticulos();
        RepositorioPedidos repoPedidos = new RepositorioPedidos();
        ServicioTienda servicioTienda = new ServicioTienda(repoArticulos, repoPedidos);
        PersistenciaJson persistenciaJson = new PersistenciaJson();

        ConsolaTienda consola = new ConsolaTienda(repoArticulos, repoPedidos, servicioTienda, persistenciaJson);
        consola.registrarShutdownHook();
        consola.cargarDatosIniciales();
        consola.iniciar();
    }

    private void registrarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!guardadoEnSalida) {
                guardarDatosSilencioso("\nCierre detectado. Guardando datos automáticamente...");
            }
        }, "autoguardado-shutdown-hook"));
    }

    private void cargarDatosIniciales() {
        try {
            repoArticulos.cargarDesdeJson(ARCHIVO_ARTICULOS, persistenciaJson);
            repoPedidos.cargarDesdeJson(ARCHIVO_PEDIDOS, persistenciaJson);
        } catch (IOException e) {
            System.out.println("Aviso: no se pudieron cargar los archivos iniciales (" + e.getMessage() + ")");
        }
    }

    public void iniciar() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean ejecutando = true;
            while (ejecutando) {
                mostrarMenu();
                int opcion = leerEntero(scanner, "Elige una opción: ");
                ejecutando = manejarOpcion(scanner, opcion);
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n=== SIMULADOR TIENDA ===");
        System.out.println("1. Registrar artículo");
        System.out.println("2. Listar artículos");
        System.out.println("3. Agregar artículo al carrito");
        System.out.println("4. Ver carrito");
        System.out.println("5. Crear pedido");
        System.out.println("6. Listar pedidos y pagar");
        System.out.println("7. Guardar datos en JSON");
        System.out.println("8. Cargar datos desde JSON");
        System.out.println("9. Actualizar precio y stock");
        System.out.println("10. Quitar artículo del carrito");
        System.out.println("11. Vaciar carrito");
        System.out.println("12. Buscar artículos por categoría");
        System.out.println("13. Consultar pedidos por correo");
        System.out.println("14. Cancelar pedido pendiente");
        System.out.println("0. Salir (autoguarda datos)");
    }

    private boolean manejarOpcion(Scanner scanner, int opcion) {
        try {
            return switch (opcion) {
                case 1 -> {
                    registrarArticulo(scanner);
                    yield true;
                }
                case 2 -> {
                    listarArticulos();
                    yield true;
                }
                case 3 -> {
                    agregarAlCarrito(scanner);
                    yield true;
                }
                case 4 -> {
                    verCarrito();
                    yield true;
                }
                case 5 -> {
                    crearPedido(scanner);
                    yield true;
                }
                case 6 -> {
                    listarYPagarPedido(scanner);
                    yield true;
                }
                case 7 -> {
                    guardarEnJson();
                    yield true;
                }
                case 8 -> {
                    cargarDesdeJson();
                    yield true;
                }
                case 9 -> {
                    actualizarPrecioYStock(scanner);
                    yield true;
                }
                case 10 -> {
                    quitarArticuloDelCarrito(scanner);
                    yield true;
                }
                case 11 -> {
                    vaciarCarrito();
                    yield true;
                }
                case 12 -> {
                    buscarPorCategoria(scanner);
                    yield true;
                }
                case 13 -> {
                    consultarPedidosPorCorreo(scanner);
                    yield true;
                }
                case 14 -> {
                    cancelarPedido(scanner);
                    yield true;
                }
                case 0 -> {
                    guardarDatosSilencioso("Guardando datos antes de salir...");
                    guardadoEnSalida = true;
                    System.out.println("¡Hasta luego!");
                    yield false;
                }
                default -> {
                    System.out.println("Opción no válida.");
                    yield true;
                }
            };
        } catch (IllegalArgumentException | IllegalStateException | StockInsuficienteException | TarjetaInvalidaException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            return true;
        }
    }

    private void guardarDatosSilencioso(String mensajePrevio) {
        System.out.println(mensajePrevio);
        try {
            repoArticulos.guardarEnJson(ARCHIVO_ARTICULOS, persistenciaJson);
            repoPedidos.guardarEnJson(ARCHIVO_PEDIDOS, persistenciaJson);
            System.out.println("Autoguardado completado en " + ARCHIVO_ARTICULOS + " y " + ARCHIVO_PEDIDOS);
        } catch (IOException e) {
            System.out.println("Aviso: no se pudieron autoguardar los datos (" + e.getMessage() + ")");
        }
    }

    private void registrarArticulo(Scanner scanner) {
        String id = leerTexto(scanner, "ID del artículo: ");
        String nombre = leerTexto(scanner, "Nombre: ");
        Categoria categoria = leerCategoria(scanner);
        BigDecimal precio = leerDecimal(scanner, "Precio: ");
        int stock = leerEntero(scanner, "Stock inicial: ");

        Articulo articulo = new Articulo(id, nombre, categoria, precio, stock);
        repoArticulos.guardar(articulo);
        System.out.println("Artículo guardado: " + articulo.getId() + " - " + articulo.getNombre());
    }

    private void listarArticulos() {
        List<Articulo> lista = repoArticulos.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay artículos registrados.");
            return;
        }
        System.out.println("\n--- Catálogo ---");
        for (Articulo a : lista) {
            System.out.printf("[%s] %s | %s | $%s | Stock: %d%n",
                    a.getId(), a.getNombre(), a.getCategoria(), a.getPrecio(), a.getStock());
        }
    }

    private void buscarPorCategoria(Scanner scanner) {
        Categoria categoria = leerCategoria(scanner);
        List<Articulo> filtrados = repoArticulos.listarTodos().stream()
                .filter(a -> a.getCategoria() == categoria)
                .toList();

        if (filtrados.isEmpty()) {
            System.out.println("No hay artículos en la categoría " + categoria.name() + ".");
            return;
        }

        System.out.println("\n--- Artículos en " + categoria.name() + " ---");
        for (Articulo a : filtrados) {
            System.out.printf("[%s] %s | $%s | Stock: %d%n",
                    a.getId(), a.getNombre(), a.getPrecio(), a.getStock());
        }
    }

    private void agregarAlCarrito(Scanner scanner) throws StockInsuficienteException {
        listarArticulos();
        String id = leerTexto(scanner, "ID del artículo a agregar: ");
        int cantidad = leerEntero(scanner, "Cantidad: ");
        servicioTienda.agregarAlCarrito(carrito, id, cantidad);
        System.out.println("Agregado al carrito.");
    }

    private void verCarrito() {
        if (carrito.estaVacio()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        System.out.println("\n--- Carrito ---");
        for (ItemCarrito item : carrito.getItems().values()) {
            System.out.printf("- [%s] %s x%d -> Subtotal: $%s%n",
                    item.getArticulo().getId(), item.getArticulo().getNombre(), item.getCantidad(), item.getSubtotal());
        }
        System.out.println("Total: $" + carrito.calcularTotal());
    }

    private void quitarArticuloDelCarrito(Scanner scanner) {
        if (carrito.estaVacio()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        verCarrito();
        String id = leerTexto(scanner, "ID del artículo a quitar: ");
        if (!carrito.getItems().containsKey(id)) {
            System.out.println("El artículo con ID '" + id + "' no está en el carrito.");
            return;
        }
        carrito.quitarArticulo(id);
        System.out.println("Artículo '" + id + "' eliminado del carrito.");
    }

    private void vaciarCarrito() {
        if (carrito.estaVacio()) {
            System.out.println("El carrito ya está vacío.");
            return;
        }
        carrito.vaciar();
        System.out.println("Carrito vaciado.");
    }

    private void crearPedido(Scanner scanner) throws StockInsuficienteException {
        if (carrito.estaVacio()) {
            System.out.println("No puedes crear un pedido con el carrito vacío.");
            return;
        }
        String correo = leerTexto(scanner, "Correo del usuario: ");
        Pedido pedido = servicioTienda.crearPedido(correo, carrito);
        System.out.println("Pedido creado con ID: " + pedido.getId() + " | Total: $" + pedido.getTotal());
    }

    private void listarYPagarPedido(Scanner scanner) throws TarjetaInvalidaException {
        List<Pedido> pedidos = repoPedidos.listarTodos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("\n--- Pedidos ---");
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Cliente: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getCorreoUsuario(), p.getTotal(), p.getEstado());
        }

        String responder = leerTexto(scanner, "¿Deseas pagar algún pedido? (s/n): ");
        if (!responder.equalsIgnoreCase("s")) {
            return;
        }
        String pedidoId = leerTexto(scanner, "ID del pedido: ");
        String tarjeta = leerTexto(scanner, "Número de tarjeta: ");
        Pago pago = servicioTienda.procesarPago(pedidoId, tarjeta);
        System.out.println("Pago procesado correctamente. ID de pago: " + pago.getId());
    }

    private void consultarPedidosPorCorreo(Scanner scanner) {
        String correo = leerTexto(scanner, "Correo del usuario: ");
        List<Pedido> encontrados = repoPedidos.buscarPorUsuario(correo);
        if (encontrados.isEmpty()) {
            System.out.println("No se encontraron pedidos para el correo: " + correo);
            return;
        }
        System.out.println("\n--- Pedidos de " + correo + " ---");
        for (Pedido p : encontrados) {
            System.out.printf("ID: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getTotal(), p.getEstado());
        }
    }

    private void cancelarPedido(Scanner scanner) {
        List<Pedido> pedidos = repoPedidos.listarTodos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("\n--- Pedidos ---");
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Cliente: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getCorreoUsuario(), p.getTotal(), p.getEstado());
        }
        String pedidoId = leerTexto(scanner, "ID del pedido a cancelar: ");
        servicioTienda.cancelarPedido(pedidoId);
        System.out.println("Pedido cancelado: " + pedidoId);
    }

    private void guardarEnJson() throws IOException {
        repoArticulos.guardarEnJson(ARCHIVO_ARTICULOS, persistenciaJson);
        repoPedidos.guardarEnJson(ARCHIVO_PEDIDOS, persistenciaJson);
        System.out.println("Datos guardados en " + ARCHIVO_ARTICULOS + " y " + ARCHIVO_PEDIDOS);
    }

    private void cargarDesdeJson() throws IOException {
        repoArticulos.cargarDesdeJson(ARCHIVO_ARTICULOS, persistenciaJson);
        repoPedidos.cargarDesdeJson(ARCHIVO_PEDIDOS, persistenciaJson);
        System.out.println("Datos cargados desde JSON.");
    }

    private void actualizarPrecioYStock(Scanner scanner) {
        listarArticulos();
        String id = leerTexto(scanner, "ID del artículo a actualizar: ");
        Optional<Articulo> opt = repoArticulos.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No existe un artículo con ID: " + id);
        }

        BigDecimal nuevoPrecio = leerDecimal(scanner, "Nuevo precio: ");
        int nuevoStock = leerEntero(scanner, "Nuevo stock: ");

        if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        Articulo actual = opt.get();
        Articulo actualizado = new Articulo(
                actual.getId(),
                actual.getNombre(),
                actual.getCategoria(),
                nuevoPrecio,
                nuevoStock
        );
        repoArticulos.guardar(actualizado);
        System.out.printf("Artículo actualizado: [%s] %s -> $%s | Stock: %d%n",
                actualizado.getId(), actualizado.getNombre(), actualizado.getPrecio(), actualizado.getStock());
    }

    private Categoria leerCategoria(Scanner scanner) {
        Categoria[] categorias = Categoria.values();
        System.out.println("Categorías disponibles:");
        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i + 1) + ". " + categorias[i].name());
        }
        int seleccion = leerEntero(scanner, "Selecciona categoría (número): ");
        if (seleccion < 1 || seleccion > categorias.length) {
            throw new IllegalArgumentException("Categoría inválida.");
        }
        return categorias[seleccion - 1];
    }

    private String leerTexto(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número entero válido.");
            }
        }
    }

    private BigDecimal leerDecimal(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return new BigDecimal(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un monto numérico válido (ej. 499.00).");
            }
        }
    }
}
