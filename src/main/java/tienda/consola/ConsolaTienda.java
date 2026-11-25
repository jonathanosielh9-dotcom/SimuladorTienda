package tienda.consola;

import tienda.excepciones.CorreoDuplicadoException;

import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.Categoria;
import tienda.modelo.EstadoPedido;
import tienda.modelo.ItemCarrito;
import tienda.modelo.Pago;
import tienda.modelo.Pedido;
import tienda.modelo.RegistroUsuario;
import tienda.modelo.Rol;
import tienda.persistencia.PersistenciaJson;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioCarritos;
import tienda.persistencia.RepositorioPedidos;
import tienda.persistencia.RepositorioUsuarios;
import tienda.servicio.ServicioAutenticacion;
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
    private static final Path ARCHIVO_USUARIOS = Path.of("datos", "usuarios.json");
    private static final Path ARCHIVO_CARRITOS = Path.of("datos", "carritos.json");

    private final RepositorioArticulos repoArticulos;
    private final RepositorioPedidos repoPedidos;
    private final RepositorioUsuarios repoUsuarios;
    private final RepositorioCarritos repoCarritos;
    private final ServicioTienda servicioTienda;
    private final ServicioAutenticacion servicioAutenticacion;
    private final PersistenciaJson persistenciaJson;

    private RegistroUsuario usuarioActual;
    private boolean esInvitado;
    private Carrito carritoActual;
    private volatile boolean guardadoEnSalida = false;

    public ConsolaTienda(
            RepositorioArticulos repoArticulos,
            RepositorioPedidos repoPedidos,
            RepositorioUsuarios repoUsuarios,
            RepositorioCarritos repoCarritos,
            ServicioTienda servicioTienda,
            ServicioAutenticacion servicioAutenticacion,
            PersistenciaJson persistenciaJson) {
        this.repoArticulos = repoArticulos;
        this.repoPedidos = repoPedidos;
        this.repoUsuarios = repoUsuarios;
        this.repoCarritos = repoCarritos;
        this.servicioTienda = servicioTienda;
        this.servicioAutenticacion = servicioAutenticacion;
        this.persistenciaJson = persistenciaJson;
    }

    public static void main(String[] args) {
        RepositorioArticulos repoArticulos = new RepositorioArticulos();
        RepositorioPedidos repoPedidos = new RepositorioPedidos();
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios(ARCHIVO_USUARIOS);
        RepositorioCarritos repoCarritos = new RepositorioCarritos(ARCHIVO_CARRITOS);
        ServicioTienda servicioTienda = new ServicioTienda(repoArticulos, repoPedidos);
        ServicioAutenticacion servicioAutenticacion = new ServicioAutenticacion(repoUsuarios);
        PersistenciaJson persistenciaJson = new PersistenciaJson();

        ConsolaTienda consola = new ConsolaTienda(
                repoArticulos,
                repoPedidos,
                repoUsuarios,
                repoCarritos,
                servicioTienda,
                servicioAutenticacion,
                persistenciaJson
        );
        consola.registrarShutdownHook();
        consola.cargarDatosIniciales();
        consola.iniciar();
    }

    private void registrarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!guardadoEnSalida) {
                autoguardarTodo("\nCierre detectado. Guardando datos automáticamente...");
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

    private void autoguardarTodo(String mensajePrevio) {
        if (mensajePrevio != null && !mensajePrevio.isBlank()) {
            System.out.println(mensajePrevio);
        }
        try {
            repoArticulos.guardarEnJson(ARCHIVO_ARTICULOS, persistenciaJson);
            repoPedidos.guardarEnJson(ARCHIVO_PEDIDOS, persistenciaJson);
            if (usuarioActual != null && usuarioActual.rol() == Rol.USUARIO_REGISTRADO && carritoActual != null) {
                repoCarritos.guardarCarritoUsuario(usuarioActual.user(), carritoActual);
            }
        } catch (IOException e) {
            System.out.println("Aviso: no se pudieron autoguardar los datos (" + e.getMessage() + ")");
        }
    }

    public void iniciar() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean salirPrograma = false;
            while (!salirPrograma) {
                boolean sesionIniciada = pantallaAcceso(scanner);
                if (!sesionIniciada) {
                    autoguardarTodo("Guardando datos antes de salir...");
                    guardadoEnSalida = true;
                    System.out.println("¡Hasta luego!");
                    salirPrograma = true;
                    break;
                }

                if (usuarioActual != null && usuarioActual.rol() == Rol.ADMINISTRADOR) {
                    salirPrograma = bucleMenuAdmin(scanner);
                } else {
                    salirPrograma = bucleMenuCliente(scanner);
                }

                finalizarSesionActual();
                if (!salirPrograma) {
                    autoguardarTodo("Sesión cerrada. Datos guardados automáticamente.");
                }
            }
            if (!guardadoEnSalida) {
                autoguardarTodo("Guardando datos antes de salir...");
                guardadoEnSalida = true;
                System.out.println("¡Hasta luego!");
            }
        }
    }

    private boolean pantallaAcceso(Scanner scanner) {
        while (true) {
            System.out.println("\n=== TIENDA EN LÍNEA - ACCESO ===");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrarse (nueva cuenta de usuario)");
            System.out.println("3. Continuar como invitado (sin cuenta)");
            if (repoUsuarios.listarPorRol(Rol.ADMINISTRADOR).isEmpty()) {
                System.out.println("4. Configurar primera cuenta de Administrador");
            }
            System.out.println("0. Salir");

            int opcion = leerEntero(scanner, "Elige una opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        String correo = leerTexto(scanner, "Correo: ");
                        String pass = leerTexto(scanner, "Contraseña: ");
                        Optional<RegistroUsuario> opt = servicioAutenticacion.autenticar(correo, pass);
                        if (opt.isEmpty()) {
                            System.out.println("Credenciales inválidas.");
                            break;
                        }
                        usuarioActual = opt.get();
                        esInvitado = false;
                        if (usuarioActual.rol() == Rol.USUARIO_REGISTRADO) {
                            carritoActual = repoCarritos.cargarCarritoUsuario(usuarioActual.user(), repoArticulos);
                            System.out.println("Bienvenido, " + usuarioActual.user()
                                    + " (artículos en carrito guardado: " + carritoActual.getItems().size() + ")");
                        } else {
                            carritoActual = new Carrito();
                            System.out.println("Sesión de ADMINISTRADOR iniciada: " + usuarioActual.user());
                        }
                        return true;
                    }
                    case 2 -> {
                        String correo = leerTexto(scanner, "Correo para registrar: ");
                        String pass = leerTexto(scanner, "Crea una contraseña: ");
                        RegistroUsuario nuevo = servicioAutenticacion.registrar(correo, pass, Rol.USUARIO_REGISTRADO);
                        usuarioActual = nuevo;
                        esInvitado = false;
                        carritoActual = new Carrito();
                        autoguardarTodo(null);
                        System.out.println("Cuenta registrada correctamente. Bienvenido, " + nuevo.user());
                        return true;
                    }
                    case 3 -> {
                        usuarioActual = null;
                        esInvitado = true;
                        carritoActual = new Carrito();
                        System.out.println("Modo INVITADO activo (tu carrito no se guardará al cerrar la aplicación).");
                        return true;
                    }
                    case 4 -> {
                        if (!repoUsuarios.listarPorRol(Rol.ADMINISTRADOR).isEmpty()) {
                            System.out.println("Ya existe al menos un administrador registrado.");
                            break;
                        }
                        String correo = leerTexto(scanner, "Correo del administrador: ");
                        String pass = leerTexto(scanner, "Contraseña del administrador: ");
                        RegistroUsuario admin = servicioAutenticacion.registrar(correo, pass, Rol.ADMINISTRADOR);
                        usuarioActual = admin;
                        esInvitado = false;
                        carritoActual = new Carrito();
                        autoguardarTodo(null);
                        System.out.println("Administrador creado e iniciado: " + admin.user());
                        return true;
                    }
                    case 0 -> {
                        return false;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (CorreoDuplicadoException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void finalizarSesionActual() {
        if (usuarioActual != null && usuarioActual.rol() == Rol.USUARIO_REGISTRADO && carritoActual != null) {
            repoCarritos.guardarCarritoUsuario(usuarioActual.user(), carritoActual);
        }
        usuarioActual = null;
        esInvitado = false;
        carritoActual = null;
    }

    private boolean bucleMenuCliente(Scanner scanner) {
        while (true) {
            String tipoCuenta = esInvitado ? "INVITADO" : "USUARIO REGISTRADO (" + usuarioActual.user() + ")";
            System.out.println("\n=== TIENDA EN LÍNEA [" + tipoCuenta + "] ===");
            System.out.println("1. Listar artículos");
            System.out.println("2. Buscar artículos por categoría");
            System.out.println("3. Agregar artículo al carrito");
            System.out.println("4. Ver carrito");
            System.out.println("5. Quitar artículo del carrito");
            System.out.println("6. Vaciar carrito");
            System.out.println("7. Crear pedido");
            System.out.println("8. Ver mis pedidos y pagar");
            System.out.println("9. Cancelar pedido pendiente");
            System.out.println("10. Cerrar sesión");
            System.out.println("0. Salir de la aplicación");

            int opcion = leerEntero(scanner, "Elige una opción: ");
            try {
                switch (opcion) {
                    case 1 -> listarArticulos();
                    case 2 -> buscarPorCategoria(scanner);
                    case 3 -> {
                        agregarAlCarrito(scanner);
                        persistirCarritoSiRegistrado();
                    }
                    case 4 -> verCarrito();
                    case 5 -> {
                        quitarArticuloDelCarrito(scanner);
                        persistirCarritoSiRegistrado();
                    }
                    case 6 -> {
                        vaciarCarrito();
                        persistirCarritoSiRegistrado();
                    }
                    case 7 -> {
                        crearPedidoCliente(scanner);
                        persistirCarritoSiRegistrado();
                        autoguardarTodo(null);
                    }
                    case 8 -> {
                        verYPagarMisPedidos(scanner);
                        autoguardarTodo(null);
                    }
                    case 9 -> {
                        cancelarPedidoCliente(scanner);
                        autoguardarTodo(null);
                    }
                    case 10 -> {
                        return false;
                    }
                    case 0 -> {
                        return true;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException | StockInsuficienteException | TarjetaInvalidaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private boolean bucleMenuAdmin(Scanner scanner) {
        while (true) {
            System.out.println("\n=== PANEL DE ADMINISTRADOR [" + usuarioActual.user() + "] ===");
            System.out.println("1. Registrar artículo");
            System.out.println("2. Quitar artículo de la tienda");
            System.out.println("3. Actualizar precio y stock");
            System.out.println("4. Listar artículos");
            System.out.println("5. Buscar artículos por categoría");
            System.out.println("6. Buscar / listar pedidos y pagar");
            System.out.println("7. Cancelar pedido");
            System.out.println("8. Consultar artículos en el carrito de un usuario");
            System.out.println("9. Eliminar usuario (requiere contraseña de admin)");
            System.out.println("10. Cerrar sesión");
            System.out.println("0. Salir de la aplicación");

            int opcion = leerEntero(scanner, "Elige una opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        registrarArticulo(scanner);
                        autoguardarTodo(null);
                    }
                    case 2 -> {
                        quitarArticuloCatalogo(scanner);
                        autoguardarTodo(null);
                    }
                    case 3 -> {
                        actualizarPrecioYStock(scanner);
                        autoguardarTodo(null);
                    }
                    case 4 -> listarArticulos();
                    case 5 -> buscarPorCategoria(scanner);
                    case 6 -> buscarYListarPedidosAdmin(scanner);
                    case 7 -> {
                        cancelarPedidoAdmin(scanner);
                        autoguardarTodo(null);
                    }
                    case 8 -> consultarCarritoDeUsuario(scanner);
                    case 9 -> eliminarUsuarioConPasswordAdmin(scanner);
                    case 10 -> {
                        return false;
                    }
                    case 0 -> {
                        return true;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException | TarjetaInvalidaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void persistirCarritoSiRegistrado() {
        if (usuarioActual != null && usuarioActual.rol() == Rol.USUARIO_REGISTRADO && carritoActual != null) {
            repoCarritos.guardarCarritoUsuario(usuarioActual.user(), carritoActual);
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

    private void quitarArticuloCatalogo(Scanner scanner) {
        listarArticulos();
        String id = leerTexto(scanner, "ID del artículo a quitar de la tienda: ");
        if (repoArticulos.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("No existe un artículo con ID: " + id);
        }
        repoArticulos.eliminarPorId(id);
        System.out.println("Artículo eliminado del catálogo: " + id);
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
        servicioTienda.agregarAlCarrito(carritoActual, id, cantidad);
        System.out.println("Agregado al carrito.");
    }

    private void verCarrito() {
        if (carritoActual.estaVacio()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        System.out.println("\n--- Carrito ---");
        for (ItemCarrito item : carritoActual.getItems().values()) {
            System.out.printf("- [%s] %s x%d -> Subtotal: $%s%n",
                    item.getArticulo().getId(), item.getArticulo().getNombre(), item.getCantidad(), item.getSubtotal());
        }
        System.out.println("Total: $" + carritoActual.calcularTotal());
    }

    private void quitarArticuloDelCarrito(Scanner scanner) {
        if (carritoActual.estaVacio()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        verCarrito();
        String id = leerTexto(scanner, "ID del artículo a quitar: ");
        if (!carritoActual.getItems().containsKey(id)) {
            System.out.println("El artículo con ID '" + id + "' no está en el carrito.");
            return;
        }
        carritoActual.quitarArticulo(id);
        System.out.println("Artículo '" + id + "' eliminado del carrito.");
    }

    private void vaciarCarrito() {
        if (carritoActual.estaVacio()) {
            System.out.println("El carrito ya está vacío.");
            return;
        }
        carritoActual.vaciar();
        System.out.println("Carrito vaciado.");
    }

    private void crearPedidoCliente(Scanner scanner) throws StockInsuficienteException {
        if (carritoActual.estaVacio()) {
            System.out.println("No puedes crear un pedido con el carrito vacío.");
            return;
        }
        String correo = esInvitado
                ? leerTexto(scanner, "Correo para registrar el pedido (invitado): ")
                : usuarioActual.user();
        Pedido pedido = servicioTienda.crearPedido(correo, carritoActual);
        System.out.println("Pedido creado con ID: " + pedido.getId()
                + " | Total: $" + pedido.getTotal()
                + " | Comprobante generado en datos/pedidos/pedido-" + pedido.getId() + ".txt");
    }

    private void verYPagarMisPedidos(Scanner scanner) throws TarjetaInvalidaException {
        List<Pedido> pedidos = esInvitado
                ? repoPedidos.buscarPorUsuario(leerTexto(scanner, "Ingresa tu correo para consultar tus pedidos: "))
                : repoPedidos.buscarPorUsuario(usuarioActual.user());

        if (pedidos.isEmpty()) {
            System.out.println("No tienes pedidos registrados.");
            return;
        }
        System.out.println("\n--- Mis pedidos ---");
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getTotal(), p.getEstado());
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

    private void cancelarPedidoCliente(Scanner scanner) {
        List<Pedido> pedidos = esInvitado
                ? repoPedidos.buscarPorUsuario(leerTexto(scanner, "Ingresa tu correo: "))
                : repoPedidos.buscarPorUsuario(usuarioActual.user());

        List<Pedido> pendientes = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE)
                .toList();

        if (pendientes.isEmpty()) {
            System.out.println("No tienes pedidos pendientes por cancelar.");
            return;
        }
        System.out.println("\n--- Pedidos pendientes ---");
        for (Pedido p : pendientes) {
            System.out.printf("ID: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getTotal(), p.getEstado());
        }
        String pedidoId = leerTexto(scanner, "ID del pedido a cancelar: ");
        servicioTienda.cancelarPedido(pedidoId);
        System.out.println("Pedido cancelado y stock repuesto: " + pedidoId);
    }

    private void buscarYListarPedidosAdmin(Scanner scanner) throws TarjetaInvalidaException {
        System.out.println("1. Listar todos los pedidos");
        System.out.println("2. Buscar pedidos por correo de usuario");
        System.out.println("3. Buscar pedido por ID");
        int filtro = leerEntero(scanner, "Elige una opción: ");

        List<Pedido> pedidos = switch (filtro) {
            case 2 -> repoPedidos.buscarPorUsuario(leerTexto(scanner, "Correo del usuario: "));
            case 3 -> {
                String idBuscado = leerTexto(scanner, "ID del pedido: ");
                yield repoPedidos.buscarPorId(idBuscado).map(List::of).orElseGet(List::of);
            }
            default -> repoPedidos.listarTodos();
        };

        if (pedidos.isEmpty()) {
            System.out.println("No se encontraron pedidos con ese criterio.");
            return;
        }
        System.out.println("\n--- Pedidos ---");
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Cliente: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getCorreoUsuario(), p.getTotal(), p.getEstado());
        }

        String responder = leerTexto(scanner, "¿Deseas procesar el pago de algún pedido pendiente? (s/n): ");
        if (responder.equalsIgnoreCase("s")) {
            String pedidoId = leerTexto(scanner, "ID del pedido: ");
            String tarjeta = leerTexto(scanner, "Número de tarjeta: ");
            Pago pago = servicioTienda.procesarPago(pedidoId, tarjeta);
            autoguardarTodo(null);
            System.out.println("Pago procesado correctamente. ID de pago: " + pago.getId());
        }
    }

    private void cancelarPedidoAdmin(Scanner scanner) {
        List<Pedido> pedidos = repoPedidos.listarTodos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        for (Pedido p : pedidos) {
            System.out.printf("ID: %s | Cliente: %s | Total: $%s | Estado: %s%n",
                    p.getId(), p.getCorreoUsuario(), p.getTotal(), p.getEstado());
        }
        String pedidoId = leerTexto(scanner, "ID del pedido a cancelar: ");
        servicioTienda.cancelarPedido(pedidoId);
        System.out.println("Pedido cancelado: " + pedidoId);
    }

    private void consultarCarritoDeUsuario(Scanner scanner) {
        String correo = leerTexto(scanner, "Correo del usuario registrado: ");
        List<ItemCarrito> items = repoCarritos.obtenerItemsGuardados(correo);
        if (items.isEmpty()) {
            System.out.println("El usuario '" + correo + "' no tiene artículos guardados en su carrito.");
            return;
        }
        System.out.println("\n--- Carrito guardado de " + correo + " ---");
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            System.out.printf("- [%s] %s x%d -> Subtotal: $%s%n",
                    item.getArticulo().getId(), item.getArticulo().getNombre(), item.getCantidad(), item.getSubtotal());
            total = total.add(item.getSubtotal());
        }
        System.out.println("Total estimado: $" + total);
    }

    private void eliminarUsuarioConPasswordAdmin(Scanner scanner) {
        String correoObjetivo = leerTexto(scanner, "Correo del usuario a eliminar: ");
        String passAdmin = leerTexto(scanner, "Confirma tu contraseña de administrador: ");
        boolean eliminado = servicioAutenticacion.eliminarUsuarioComoAdmin(
                usuarioActual.user(), passAdmin, correoObjetivo);
        if (eliminado) {
            repoCarritos.eliminarCarritoUsuario(correoObjetivo);
            System.out.println("Usuario eliminado correctamente: " + correoObjetivo);
        } else {
            System.out.println("No se encontró ningún usuario con el correo: " + correoObjetivo);
        }
    }

    private void actualizarPrecioYStock(Scanner scanner) {
        listarArticulos();
        String id = leerTexto(scanner, "ID del artículo a actualizar: ");
        Optional<Articulo> opt = repoArticulos.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No existe un artículo con ID: " + id);
        }

        BigDecimal nuevoPrecio = leerDecimal(scanner, "Nuevo precio: ");
        int nuevoStock = leerEntero(scanner, "Nuevo stock (0 o mayor): ");

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
