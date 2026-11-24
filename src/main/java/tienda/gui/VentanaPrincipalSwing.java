package tienda.gui;

import tienda.config.Configuracion;
import tienda.excepciones.ContrasenaIncorrectaException;
import tienda.excepciones.CorreoDuplicadoException;
import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Alimento;
import tienda.modelo.Articulo;
import tienda.modelo.Moda;
import tienda.modelo.Tecnologia;
import tienda.modelo.Carrito;
import tienda.modelo.Categoria;
import tienda.modelo.EstadoPedido;
import tienda.modelo.ItemCarrito;
import tienda.modelo.PagoTarjeta;
import tienda.modelo.PagoTransferencia;
import tienda.modelo.PagoWallet;
import tienda.modelo.Pedido;
import tienda.modelo.RegistroUsuario;
import tienda.modelo.Rol;
import tienda.persistencia.PersistenciaJson;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioCarritos;
import tienda.persistencia.RepositorioPedidos;
import tienda.persistencia.RepositorioUsuarios;
import tienda.servicio.GeneradorComprobantes;
import tienda.servicio.GestorImagenes;
import tienda.servicio.ServicioAutenticacion;
import tienda.servicio.ServicioTienda;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VentanaPrincipalSwing extends JFrame {

    private static final String VISTA_ACCESO = "ACCESO";
    private static final String VISTA_TIENDA = "TIENDA";

    private final ServicioAutenticacion servicioAuth;
    private final ServicioTienda servicioTienda;
    private final RepositorioUsuarios repoUsuarios;
    private final RepositorioArticulos repoArticulos;
    private final RepositorioPedidos repoPedidos;
    private final RepositorioCarritos repoCarritos;
    private final PersistenciaJson persistencia;
    private final GestorImagenes gestorImagenes;
    private final GeneradorComprobantes generadorComprobantes;

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);

    // Estado de sesión
    private RegistroUsuario usuarioActual;
    private boolean esInvitado;
    private Carrito carritoActual = new Carrito();
    private BigDecimal saldoWallet = new BigDecimal("5000.00");

    // Componentes de acceso
    private JTextField txtLoginCorreo;
    private JPasswordField txtLoginPass;
    private JTextField txtRegCorreo;
    private JPasswordField txtRegPass;

    // Componentes de tienda
    private JLabel lblSesionActiva;
    private JTabbedPane tabbedPane;
    private JComboBox<String> cmbCategoriaFiltro;
    private DefaultTableModel modeloCatalogo;
    private JTable tablaCatalogo;
    private JLabel lblPreviewImagen;
    private JSpinner spnCantidadAgregar;

    private DefaultTableModel modeloCarrito;
    private JTable tablaCarrito;
    private JLabel lblTotalCarrito;

    private DefaultTableModel modeloPedidos;
    private JTable tablaPedidos;

    // Componentes de administración
    private JPanel panelAdminTab;
    private DefaultTableModel modeloAdminArticulos;
    private JTable tablaAdminArticulos;
    private JTextField txtAdminPrecio;
    private JSpinner spnAdminStock;
    private DefaultTableModel modeloAdminPedidos;
    private JTable tablaAdminPedidos;

    public VentanaPrincipalSwing(
            ServicioAutenticacion servicioAuth,
            ServicioTienda servicioTienda,
            RepositorioUsuarios repoUsuarios,
            RepositorioArticulos repoArticulos,
            RepositorioPedidos repoPedidos,
            RepositorioCarritos repoCarritos,
            PersistenciaJson persistencia,
            GestorImagenes gestorImagenes,
            GeneradorComprobantes generadorComprobantes
    ) {
        this.servicioAuth = Objects.requireNonNull(servicioAuth);
        this.servicioTienda = Objects.requireNonNull(servicioTienda);
        this.repoUsuarios = Objects.requireNonNull(repoUsuarios);
        this.repoArticulos = Objects.requireNonNull(repoArticulos);
        this.repoPedidos = Objects.requireNonNull(repoPedidos);
        this.repoCarritos = Objects.requireNonNull(repoCarritos);
        this.persistencia = Objects.requireNonNull(persistencia);
        this.gestorImagenes = Objects.requireNonNull(gestorImagenes);
        this.generadorComprobantes = Objects.requireNonNull(generadorComprobantes);

        setTitle("Simulador de Tienda");
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rootPanel.add(construirPanelAcceso(), VISTA_ACCESO);
        rootPanel.add(construirPanelTienda(), VISTA_TIENDA);
        setContentPane(rootPanel);

        rootLayout.show(rootPanel, VISTA_ACCESO);
    }

    private JPanel construirPanelAcceso() {
        JPanel contenedor = new JPanel(new BorderLayout(16, 16));
        contenedor.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titulo = new JLabel("Simulador de Tienda - Acceso", SwingConstants.CENTER);
        titulo.putClientProperty("JComponent.outline", null);
        contenedor.add(titulo, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));

        // Login + Invitado
        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new BoxLayout(panelLogin, BoxLayout.Y_AXIS));
        panelLogin.setBorder(BorderFactory.createTitledBorder("Iniciar Sesión"));

        txtLoginCorreo = new JTextField();
        txtLoginPass = new JPasswordField();
        JButton btnLogin = new JButton("Entrar");
        btnLogin.addActionListener(e -> intentarLogin());

        JButton btnInvitado = new JButton("Continuar como Invitado");
        btnInvitado.addActionListener(e -> entrarComoInvitado());

        panelLogin.add(new JLabel("Correo:"));
        panelLogin.add(txtLoginCorreo);
        panelLogin.add(Box.createVerticalStrut(10));
        panelLogin.add(new JLabel("Contraseña:"));
        panelLogin.add(txtLoginPass);
        panelLogin.add(Box.createVerticalStrut(14));
        panelLogin.add(btnLogin);
        panelLogin.add(Box.createVerticalStrut(10));
        panelLogin.add(btnInvitado);

        // Registro
        JPanel panelRegistro = new JPanel();
        panelRegistro.setLayout(new BoxLayout(panelRegistro, BoxLayout.Y_AXIS));
        panelRegistro.setBorder(BorderFactory.createTitledBorder("Crear Cuenta Nueva"));

        txtRegCorreo = new JTextField();
        txtRegPass = new JPasswordField();
        JButton btnRegistrar = new JButton("Registrar y Entrar");
        btnRegistrar.addActionListener(e -> intentarRegistro());

        panelRegistro.add(new JLabel("Correo:"));
        panelRegistro.add(txtRegCorreo);
        panelRegistro.add(Box.createVerticalStrut(10));
        panelRegistro.add(new JLabel("Contraseña (mín. 4 caracteres):"));
        panelRegistro.add(txtRegPass);
        panelRegistro.add(Box.createVerticalStrut(14));
        panelRegistro.add(btnRegistrar);

        grid.add(panelLogin);
        grid.add(panelRegistro);
        contenedor.add(grid, BorderLayout.CENTER);

        return contenedor;
    }

    private JPanel construirPanelTienda() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barra superior
        JPanel barraTop = new JPanel(new BorderLayout());
        lblSesionActiva = new JLabel("Sesión activa");

        JPanel accionesTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        JButton btnEliminarCuenta = new JButton("Eliminar cuenta");
        btnEliminarCuenta.addActionListener(e -> eliminarCuentaActual());

        accionesTop.add(btnEliminarCuenta);
        accionesTop.add(btnCerrarSesion);

        barraTop.add(lblSesionActiva, BorderLayout.WEST);
        barraTop.add(accionesTop, BorderLayout.EAST);
        panel.add(barraTop, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Catálogo", construirTabCatalogo());
        tabbedPane.addTab("Carrito y Pago", construirTabCarrito());
        tabbedPane.addTab("Mis Pedidos", construirTabPedidos());

        panelAdminTab = construirTabAdmin();
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel construirTabCatalogo() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filtroBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroBar.add(new JLabel("Categoría:"));
        List<String> opciones = new ArrayList<>();
        opciones.add("TODAS");
        for (Categoria c : Categoria.values()) {
            opciones.add(c.name());
        }
        cmbCategoriaFiltro = new JComboBox<>(opciones.toArray(new String[0]));
        cmbCategoriaFiltro.addActionListener(e -> refrescarCatalogo());
        filtroBar.add(cmbCategoriaFiltro);

        JButton btnActualizar = new JButton("Refrescar");
        btnActualizar.addActionListener(e -> refrescarTodo());
        filtroBar.add(btnActualizar);

        tab.add(filtroBar, BorderLayout.NORTH);

        modeloCatalogo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Categoría", "Precio Final", "Stock"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        tablaCatalogo.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarImagenArticuloSeleccionado();
            }
        });

        tab.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        // Panel lateral derecho: imagen + agregar al carrito
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setPreferredSize(new Dimension(260, 0));
        lateral.setBorder(BorderFactory.createTitledBorder("Detalle del Artículo"));

        lblPreviewImagen = new JLabel("Selecciona un artículo", SwingConstants.CENTER);
        lblPreviewImagen.setPreferredSize(new Dimension(220, 180));
        lblPreviewImagen.setMaximumSize(new Dimension(220, 180));
        lblPreviewImagen.setAlignmentX(CENTER_ALIGNMENT);
        lblPreviewImagen.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));

        spnCantidadAgregar = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        spnCantidadAgregar.setMaximumSize(new Dimension(120, 30));

        JButton btnAgregar = new JButton("Agregar al carrito");
        btnAgregar.setAlignmentX(CENTER_ALIGNMENT);
        btnAgregar.addActionListener(e -> agregarSeleccionadoAlCarrito());

        lateral.add(lblPreviewImagen);
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(new JLabel("Cantidad:"));
        lateral.add(spnCantidadAgregar);
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(btnAgregar);

        tab.add(lateral, BorderLayout.EAST);
        return tab;
    }

    private JPanel construirTabCarrito() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        modeloCarrito = new DefaultTableModel(
                new Object[]{"ID", "Artículo", "Cantidad", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tab.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout());
        lblTotalCarrito = new JLabel("Total: $0.00");
        inferior.add(lblTotalCarrito, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> quitarItemCarrito());

        JButton btnLimpiarNoStock = new JButton("Quitar sin stock");
        btnLimpiarNoStock.addActionListener(e -> limpiarArticulosSinStock());

        JButton btnPagar = new JButton("Crear Pedido y Pagar");
        btnPagar.addActionListener(e -> flujoCrearPedidoYPagar());

        botones.add(btnQuitar);
        botones.add(btnLimpiarNoStock);
        botones.add(btnPagar);
        inferior.add(botones, BorderLayout.EAST);

        tab.add(inferior, BorderLayout.SOUTH);
        return tab;
    }

    private JPanel construirTabPedidos() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        modeloPedidos = new DefaultTableModel(
                new Object[]{"ID Pedido", "Usuario", "Estado", "Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos = new JTable(modeloPedidos);
        tab.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar pedido");
        btnCancelar.addActionListener(e -> cancelarPedidoSeleccionado());

        JButton btnComprobante = new JButton("Ver / Guardar comprobante .txt");
        btnComprobante.addActionListener(e -> verYGuardarComprobanteSeleccionado());

        botones.add(btnComprobante);
        botones.add(btnCancelar);
        tab.add(botones, BorderLayout.SOUTH);

        return tab;
    }

    private JPanel construirTabAdmin() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        modeloAdminArticulos = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Categoría", "Precio Base", "Stock"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAdminArticulos = new JTable(modeloAdminArticulos);

        JPanel panelEdicion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtAdminPrecio = new JTextField(10);
        spnAdminStock = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        JButton btnGuardarCambios = new JButton("Guardar precio/stock");
        btnGuardarCambios.addActionListener(e -> guardarPrecioYStockAdmin());

        JButton btnEliminarCascada = new JButton("Eliminar artículo (cascada)");
        btnEliminarCascada.addActionListener(e -> eliminarArticuloCascadaAdmin());

        panelEdicion.add(new JLabel("Nuevo precio:"));
        panelEdicion.add(txtAdminPrecio);
        panelEdicion.add(new JLabel("Nuevo stock:"));
        panelEdicion.add(spnAdminStock);
        panelEdicion.add(btnGuardarCambios);
        panelEdicion.add(btnEliminarCascada);
        JButton btnAgregarArticulo = new JButton("Agregar artículo");
        btnAgregarArticulo.addActionListener(e -> agregarArticuloAdmin());
        panelEdicion.add(btnAgregarArticulo);

        JPanel topAdmin = new JPanel(new BorderLayout());
        topAdmin.setBorder(BorderFactory.createTitledBorder("Gestión de Catálogo"));
        topAdmin.add(new JScrollPane(tablaAdminArticulos), BorderLayout.CENTER);
        topAdmin.add(panelEdicion, BorderLayout.SOUTH);

        modeloAdminPedidos = new DefaultTableModel(
                new Object[]{"ID Pedido", "Usuario", "Estado", "Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAdminPedidos = new JTable(modeloAdminPedidos);
        JPanel bottomAdmin = new JPanel(new BorderLayout());
        bottomAdmin.setPreferredSize(new Dimension(0, 180));
        bottomAdmin.setBorder(BorderFactory.createTitledBorder("Todos los Pedidos del Sistema"));
        bottomAdmin.add(new JScrollPane(tablaAdminPedidos), BorderLayout.CENTER);

        tab.add(topAdmin, BorderLayout.CENTER);
        tab.add(bottomAdmin, BorderLayout.SOUTH);
        return tab;
    }

    private void intentarLogin() {
        String correo = txtLoginCorreo.getText();
        String pass = new String(txtLoginPass.getPassword());
        Optional<RegistroUsuario> opt = servicioAuth.autenticar(correo, pass);
        if (opt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas.", "Error de acceso", JOptionPane.ERROR_MESSAGE);
            return;
        }
        activarSesionUsuario(opt.get());
    }

    private void intentarRegistro() {
        String correo = txtRegCorreo.getText();
        String pass = new String(txtRegPass.getPassword());
        try {
            RegistroUsuario nuevo = servicioAuth.registrar(correo, pass, Rol.USUARIO_REGISTRADO);
            activarSesionUsuario(nuevo);
        } catch (CorreoDuplicadoException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void entrarComoInvitado() {
        this.usuarioActual = null;
        this.esInvitado = true;
        this.carritoActual = new Carrito();
        actualizarVisibilidadTabs();
        refrescarTodo();
        rootLayout.show(rootPanel, VISTA_TIENDA);
    }

    private void activarSesionUsuario(RegistroUsuario usuario) {
        this.usuarioActual = usuario;
        this.esInvitado = false;
        this.carritoActual = repoCarritos.cargarCarritoUsuario(usuario.user(), repoArticulos);
        actualizarVisibilidadTabs();
        refrescarTodo();
        rootLayout.show(rootPanel, VISTA_TIENDA);
    }

    private void cerrarSesion() {
        persistirEstadoSilencioso();
        this.usuarioActual = null;
        this.esInvitado = false;
        this.carritoActual = new Carrito();
        txtLoginPass.setText("");
        rootLayout.show(rootPanel, VISTA_ACCESO);
    }

    private void eliminarCuentaActual() {
        if (esInvitado || usuarioActual == null) {
            JOptionPane.showMessageDialog(this, "Estás en modo invitado.");
            return;
        }
        if (usuarioActual.rol() == Rol.ADMINISTRADOR) {
            JOptionPane.showMessageDialog(this, "La cuenta administradora no se elimina desde aquí.");
            return;
        }
        JPasswordField campoPass = new JPasswordField();
        int resp = JOptionPane.showConfirmDialog(
                this, campoPass, "Confirma tu contraseña para dar de baja la cuenta:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (resp != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            boolean eliminada = servicioAuth.eliminarCuenta(usuarioActual.user(), new String(campoPass.getPassword()));
            if (eliminada) {
                repoCarritos.eliminarCarritoUsuario(usuarioActual.user());
                JOptionPane.showMessageDialog(this, "Cuenta eliminada. Tu historial de pedidos se conserva.");
                cerrarSesion();
            }
        } catch (ContrasenaIncorrectaException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al eliminar cuenta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarVisibilidadTabs() {
        int idxAdmin = tabbedPane.indexOfComponent(panelAdminTab);
        boolean esAdmin = !esInvitado && usuarioActual != null && usuarioActual.rol() == Rol.ADMINISTRADOR;
        if (esAdmin && idxAdmin == -1) {
            tabbedPane.addTab("Administración", panelAdminTab);
        } else if (!esAdmin && idxAdmin != -1) {
            tabbedPane.removeTabAt(idxAdmin);
        }

        if (esInvitado) {
            lblSesionActiva.setText("Modo: INVITADO (carrito no persistente)");
        } else {
            lblSesionActiva.setText("Usuario: " + usuarioActual.user() + " | Rol: " + usuarioActual.rol());
        }
    }

    private void refrescarTodo() {
        refrescarCatalogo();
        refrescarCarrito();
        refrescarPedidos();
        refrescarAdmin();
    }

    private void refrescarCatalogo() {
        modeloCatalogo.setRowCount(0);
        String seleccion = (String) cmbCategoriaFiltro.getSelectedItem();
        List<Articulo> lista;
        if (seleccion == null || "TODAS".equals(seleccion)) {
            lista = repoArticulos.listarTodos();
        } else {
            lista = repoArticulos.buscarPorCategoria(Categoria.valueOf(seleccion));
        }
        for (Articulo a : lista) {
            modeloCatalogo.addRow(new Object[]{
                    a.getId(),
                    a.getNombre(),
                    a.getCategoria(),
                    a.getPrecioFinal(),
                    a.getStock()
            });
        }
    }

    private void mostrarImagenArticuloSeleccionado() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) {
            lblPreviewImagen.setIcon(null);
            lblPreviewImagen.setText("Selecciona un artículo");
            return;
        }
        String id = modeloCatalogo.getValueAt(fila, 0).toString();
        Optional<Articulo> opt = repoArticulos.buscarPorId(id);
        if (opt.isEmpty()) {
            return;
        }
        Articulo art = opt.get();
        Optional<Path> rutaImg = gestorImagenes.resolverImagenParaMostrar(null, art.getNombre());
        if (rutaImg.isPresent()) {
            ImageIcon raw = new ImageIcon(rutaImg.get().toString());
            if (raw.getIconWidth() > 0) {
                Image escalada = raw.getImage().getScaledInstance(210, 170, Image.SCALE_SMOOTH);
                lblPreviewImagen.setText("");
                lblPreviewImagen.setIcon(new ImageIcon(escalada));
                return;
            }
        }
        lblPreviewImagen.setIcon(null);
        lblPreviewImagen.setText(gestorImagenes.textoRespaldoImagen(art.getNombre()));
    }

    private void agregarSeleccionadoAlCarrito() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un artículo del catálogo.");
            return;
        }
        String id = modeloCatalogo.getValueAt(fila, 0).toString();
        int cantidad = (Integer) spnCantidadAgregar.getValue();
        try {
            servicioTienda.agregarAlCarrito(carritoActual, id, cantidad);
            guardarCarritoSiAplica();
            refrescarCarrito();
        } catch (StockInsuficienteException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo agregar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refrescarCarrito() {
        modeloCarrito.setRowCount(0);
        for (ItemCarrito item : carritoActual.getItems().values()) {
            modeloCarrito.addRow(new Object[]{
                    item.getArticulo().getId(),
                    item.getArticulo().getNombre(),
                    item.getCantidad(),
                    item.getSubtotal()
            });
        }
        lblTotalCarrito.setText("Total: $" + carritoActual.calcularTotal());
    }

    private void quitarItemCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String id = modeloCarrito.getValueAt(fila, 0).toString();
        carritoActual.quitarArticulo(id);
        guardarCarritoSiAplica();
        refrescarCarrito();
    }

    private void limpiarArticulosSinStock() {
        List<ItemCarrito> sinStock = servicioTienda.detectarArticulosSinStock(carritoActual);
        for (ItemCarrito item : sinStock) {
            carritoActual.quitarArticulo(item.getArticulo().getId());
        }
        guardarCarritoSiAplica();
        refrescarCarrito();
        JOptionPane.showMessageDialog(this, "Artículos agotados retirados: " + sinStock.size());
    }

    private void flujoCrearPedidoYPagar() {
        if (carritoActual.estaVacio()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }
        String correoPedido = (!esInvitado && usuarioActual != null) ? usuarioActual.user() : "invitado@local";
        try {
            servicioTienda.validarStockCarrito(carritoActual);
            Pedido pedido = servicioTienda.crearPedido(correoPedido, carritoActual);
            guardarCarritoSiAplica();

            String[] metodos = {"Tarjeta", "Transferencia", "Wallet"};
            String metodo = (String) JOptionPane.showInputDialog(
                    this,
                    "Pedido #" + pedido.getId() + " creado por $" + pedido.getTotal() + ".\nElige método de pago:",
                    "Procesar Pago",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    metodos,
                    metodos[0]
            );
            if (metodo == null) {
                refrescarTodo();
                persistirEstadoSilencioso();
                return;
            }

            boolean pagado = procesarMetodoPagoSwing(pedido, metodo);
            if (pagado) {
                pedido.marcarPagado();
                Path archivoComprobante = generadorComprobantes.guardarComprobanteTxt(pedido);
                JOptionPane.showMessageDialog(
                        this,
                        "Pago aprobado. Comprobante guardado en:\n" + archivoComprobante
                );
            }
            refrescarTodo();
            persistirEstadoSilencioso();
        } catch (StockInsuficienteException | TarjetaInvalidaException | IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error en pedido o pago", JOptionPane.ERROR_MESSAGE);
            refrescarTodo();
        }
    }

    private boolean procesarMetodoPagoSwing(Pedido pedido, String metodo) throws TarjetaInvalidaException {
        return switch (metodo) {
            case "Tarjeta" -> {
                String numero = JOptionPane.showInputDialog(this, "Número de tarjeta (15 o 16 dígitos):");
                if (numero == null) {
                    yield false;
                }
                String mesStr = JOptionPane.showInputDialog(this, "Mes de vencimiento (1-12):");
                String anioStr = JOptionPane.showInputDialog(this, "Año de vencimiento (ej. 2028):");
                if (mesStr == null || anioStr == null) {
                    yield false;
                }
                YearMonth ym = YearMonth.of(Integer.parseInt(anioStr.trim()), Integer.parseInt(mesStr.trim()));
                PagoTarjeta tarjeta = new PagoTarjeta(numero, ym);
                if (!tarjeta.procesar(pedido.getTotal())) {
                    throw new TarjetaInvalidaException("No se pudo procesar el pago con tarjeta.");
                }
                JOptionPane.showMessageDialog(this, "Marca detectada: " + tarjeta.detectarMarca());
                yield true;
            }
            case "Transferencia" -> {
                String clabe = JOptionPane.showInputDialog(this, "CLABE interbancaria (18 dígitos):");
                String banco = JOptionPane.showInputDialog(this, "Banco:");
                String ref = JOptionPane.showInputDialog(this, "Referencia:");
                if (clabe == null || banco == null || ref == null) {
                    yield false;
                }
                PagoTransferencia transf = new PagoTransferencia(clabe, banco, ref);
                JOptionPane.showMessageDialog(this, transf.mostrarDatos());
                yield transf.procesar(pedido.getTotal());
            }
            case "Wallet" -> {
                PagoWallet wallet = new PagoWallet(correoActualSafe(), saldoWallet);
                boolean ok = wallet.cobrar(pedido.getTotal());
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Saldo insuficiente en Wallet ($" + saldoWallet + ").");
                    yield false;
                }
                saldoWallet = wallet.getSaldo();
                JOptionPane.showMessageDialog(this, "Cobro realizado. Saldo restante: $" + saldoWallet);
                yield true;
            }
            default -> false;
        };
    }

    private String correoActualSafe() {
        return (!esInvitado && usuarioActual != null) ? usuarioActual.user() : "invitado@local";
    }

    private void refrescarPedidos() {
        modeloPedidos.setRowCount(0);
        if (esInvitado || usuarioActual == null) {
            return;
        }
        for (Pedido p : repoPedidos.buscarPorUsuario(usuarioActual.user())) {
            modeloPedidos.addRow(new Object[]{p.getId(), p.getCorreoUsuario(), p.getEstado(), p.getTotal()});
        }
    }

    private void cancelarPedidoSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String pedidoId = modeloPedidos.getValueAt(fila, 0).toString();
        try {
            servicioTienda.cancelarPedido(pedidoId);
            refrescarTodo();
            persistirEstadoSilencioso();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo cancelar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void verYGuardarComprobanteSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String pedidoId = modeloPedidos.getValueAt(fila, 0).toString();
        repoPedidos.buscarPorId(pedidoId).ifPresent(pedido -> {
            try {
                Path ruta = generadorComprobantes.guardarComprobanteTxt(pedido);
                JTextArea area = new JTextArea(generadorComprobantes.formatearComprobante(pedido));
                area.setEditable(false);
                JOptionPane.showMessageDialog(
                        this,
                        new JScrollPane(area),
                        "Comprobante guardado en " + ruta,
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar comprobante", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void refrescarAdmin() {
        modeloAdminArticulos.setRowCount(0);
        for (Articulo a : repoArticulos.listarTodos()) {
            modeloAdminArticulos.addRow(new Object[]{
                    a.getId(), a.getNombre(), a.getCategoria(), a.getPrecio(), a.getStock()
            });
        }
        modeloAdminPedidos.setRowCount(0);
        for (Pedido p : repoPedidos.listarTodos()) {
            modeloAdminPedidos.addRow(new Object[]{
                    p.getId(), p.getCorreoUsuario(), p.getEstado(), p.getTotal()
            });
        }
    }

    private void guardarPrecioYStockAdmin() {
        int fila = tablaAdminArticulos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String id = modeloAdminArticulos.getValueAt(fila, 0).toString();
        try {
            BigDecimal nuevoPrecio = new BigDecimal(txtAdminPrecio.getText().trim());
            int nuevoStock = (Integer) spnAdminStock.getValue();
            repoArticulos.actualizarPrecioYStock(id, nuevoPrecio, nuevoStock);
            refrescarTodo();
            persistirEstadoSilencioso();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválido: " + ex.getMessage());
        }
    }

    private void eliminarArticuloCascadaAdmin() {
        int fila = tablaAdminArticulos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String id = modeloAdminArticulos.getValueAt(fila, 0).toString();
        List<Carrito> carritos = new ArrayList<>();
        carritos.add(carritoActual);
        for (RegistroUsuario u : repoUsuarios.listarTodos()) {
            carritos.add(repoCarritos.cargarCarritoUsuario(u.user(), repoArticulos));
        }
        servicioTienda.eliminarArticuloEnCascada(id, carritos);
        for (RegistroUsuario u : repoUsuarios.listarTodos()) {
            repoCarritos.guardarCarritoUsuario(u.user(), repoCarritos.cargarCarritoUsuario(u.user(), repoArticulos));
        }
        refrescarTodo();
        persistirEstadoSilencioso();
    }

    private void agregarArticuloAdmin() {
        JTextField txtId = new JTextField(12);
        JTextField txtNombre = new JTextField(18);
        JComboBox<Categoria> cmbCategoria = new JComboBox<>(Categoria.values());
        JTextField txtPrecio = new JTextField(10);
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(10, 0, 100000, 1));
        JTextField txtExtra1 = new JTextField(LocalDate.now().plusDays(15).toString(), 14);
        JTextField txtExtra2 = new JTextField("Negro", 14);
        JLabel lblExtra1 = new JLabel("Caducidad (YYYY-MM-DD):");
        JLabel lblExtra2 = new JLabel("Color:");

        Runnable actualizarCamposExtra = () -> {
            Categoria cat = (Categoria) cmbCategoria.getSelectedItem();
            if (cat == Categoria.ALIMENTOS) {
                lblExtra1.setText("Caducidad (YYYY-MM-DD):");
                txtExtra1.setText(LocalDate.now().plusDays(15).toString());
                lblExtra2.setVisible(false);
                txtExtra2.setVisible(false);
            } else if (cat == Categoria.ELECTRONICA) {
                lblExtra1.setText("Garantía (meses):");
                txtExtra1.setText("12");
                lblExtra2.setVisible(false);
                txtExtra2.setVisible(false);
            } else if (cat == Categoria.ROPA) {
                lblExtra1.setText("Talla:");
                txtExtra1.setText("M");
                lblExtra2.setText("Color:");
                txtExtra2.setText("Negro");
                lblExtra2.setVisible(true);
                txtExtra2.setVisible(true);
            } else {
                lblExtra1.setText("Dato extra (opcional):");
                txtExtra1.setText("");
                lblExtra2.setVisible(false);
                txtExtra2.setVisible(false);
            }
        };

        cmbCategoria.addActionListener(e -> actualizarCamposExtra.run());
        actualizarCamposExtra.run();

        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        form.add(new JLabel("ID:"));
        form.add(txtId);
        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        form.add(new JLabel("Categoría:"));
        form.add(cmbCategoria);
        form.add(new JLabel("Precio base:"));
        form.add(txtPrecio);
        form.add(new JLabel("Stock inicial:"));
        form.add(spnStock);
        form.add(lblExtra1);
        form.add(txtExtra1);
        form.add(lblExtra2);
        form.add(txtExtra2);

        int res = JOptionPane.showConfirmDialog(
                this,
                form,
                "Nuevo Artículo del Catálogo",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String id = txtId.getText().trim();
            if (id.isEmpty()) {
                throw new IllegalArgumentException("El ID no puede estar vacío");
            }
            if (repoArticulos.buscarPorId(id).isPresent()) {
                throw new IllegalArgumentException("Ya existe un artículo con el ID: " + id);
            }
            String nombre = txtNombre.getText().trim();
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int stock = (Integer) spnStock.getValue();

            Articulo nuevo;
            if (categoria == Categoria.ALIMENTOS) {
                LocalDate caducidad = LocalDate.parse(txtExtra1.getText().trim());
                nuevo = new Alimento(id, nombre, precio, stock, caducidad);
            } else if (categoria == Categoria.ELECTRONICA) {
                int garantia = Integer.parseInt(txtExtra1.getText().trim());
                nuevo = new Tecnologia(id, nombre, precio, stock, garantia);
            } else if (categoria == Categoria.ROPA) {
                String talla = txtExtra1.getText().trim();
                String color = txtExtra2.getText().trim();
                nuevo = new Moda(id, nombre, precio, stock, talla, color);
            } else {
                nuevo = new Articulo(id, nombre, categoria, precio, stock);
            }

            repoArticulos.guardar(nuevo);
            refrescarTodo();
            persistirEstadoSilencioso();
            JOptionPane.showMessageDialog(this, "Artículo agregado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo agregar el artículo: " + ex.getMessage(),
                    "Error al crear artículo",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void guardarCarritoSiAplica() {
        if (!esInvitado && usuarioActual != null) {
            repoCarritos.guardarCarritoUsuario(usuarioActual.user(), carritoActual);
        }
    }

    private void persistirEstadoSilencioso() {
        try {
            repoArticulos.guardarEnJson(Configuracion.ARCHIVO_CATALOGO, persistencia);
            repoPedidos.guardarEnJson(Configuracion.ARCHIVO_PEDIDOS, persistencia);
            guardarCarritoSiAplica();
        } catch (IOException ignored) {
        }
    }
}
