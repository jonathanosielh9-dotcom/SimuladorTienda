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
import java.util.Scanner;

public class ConsolaTienda {

    private static final Path ARCHIVO_ARTICULOS = Path.of("datos", "articulos.json");
    private static final Path ARCHIVO_PEDIDOS = Path.of("datos", "pedidos.json");

    private final RepositorioArticulos repoArticulos;
    private final RepositorioPedidos repoPedidos;
    private final ServicioTienda servicioTienda;
    private final PersistenciaJson persistenciaJson;
    private final Carrito carrito = new Carrito();

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
        consola.cargarDatosIniciales();
        consola.iniciar();
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
        System.out.println("0. Salir");
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
                case 0 -> {
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
            System.out.printf("- %s x%d -> Subtotal: $%s%n",
                    item.getArticulo().getNombre(), item.getCantidad(), item.getSubtotal());
        }
        System.out.println("Total: $" + carrito.calcularTotal());
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
