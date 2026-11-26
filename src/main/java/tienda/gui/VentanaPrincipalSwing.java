package tienda.gui;

import tienda.config.Configuracion;
import tienda.excepciones.ContrasenaIncorrectaException;
import tienda.excepciones.CorreoDuplicadoException;
import tienda.excepciones.StockInsuficienteException;
import tienda.excepciones.TarjetaInvalidaException;
import tienda.modelo.Alimento;
import tienda.modelo.Abarrotes;
import tienda.modelo.Alimento;
import tienda.modelo.Articulo;
import tienda.modelo.Celular;
import tienda.modelo.Computadora;
import tienda.modelo.Frutas;
import tienda.modelo.Lacteos;
import tienda.modelo.Laptop;
import tienda.modelo.Hombre;
import tienda.modelo.Moda;
import tienda.modelo.Mujer;
import tienda.modelo.Salchichoneria;
import tienda.modelo.Tecnologia;
import tienda.modelo.Television;
import tienda.modelo.Verduras;
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
import javax.swing.JCheckBox;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VentanaPrincipalSwing extends JFrame {

    private static final Color COLOR_FONDO = new Color(240, 249, 255);
    private static final Color COLOR_PASTEL_DENSO = new Color(125, 211, 252);
    private static final Color COLOR_PASTEL_CLARO = new Color(240, 249, 255);
    private static final Color COLOR_PANEL_SUAVE = new Color(248, 250, 252);
    private static final Color COLOR_AZUL_MARINO = new Color(15, 41, 74);
    private static final Color COLOR_AZUL_CIELO = new Color(125, 211, 252);
    private static final Color COLOR_AZUL_CIELO_HOVER = new Color(56, 189, 248);
    private static final Color COLOR_SELECCION = new Color(186, 230, 253);

    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);

    private static final String VISTA_INICIO = "INICIO";
    private static final String VISTA_REGISTRO = "REGISTRO";

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
    private DefaultTableModel modeloAdminUsuarios;
    private JTable tablaAdminUsuarios;
    private JTextField txtCorreoEliminarAdmin;

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

        javax.swing.Timer timerSync = new javax.swing.Timer(2000, ev -> sincronizarCatalogoDesdeDisco());
        timerSync.start();
        this.gestorImagenes = Objects.requireNonNull(gestorImagenes);
        this.generadorComprobantes = Objects.requireNonNull(generadorComprobantes);

        setTitle("Simulador de Tienda");
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rootPanel.add(construirPanelInicio(), VISTA_INICIO);
        rootPanel.add(construirPanelAcceso(), VISTA_ACCESO);
        rootPanel.add(construirPanelRegistro(), VISTA_REGISTRO);
        rootPanel.add(construirPanelTienda(), VISTA_TIENDA);
        setContentPane(rootPanel);

        rootLayout.show(rootPanel, VISTA_INICIO);
        aplicarEstiloBase();
    }
    private JPanel construirPanelInicio() {
        JPanel contenedor = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_PASTEL_DENSO, 0, getHeight(), COLOR_PASTEL_CLARO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);

        JLabel lblMarca = new JLabel("Simulador Tienda", SwingConstants.CENTER);
        lblMarca.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 34));
        lblMarca.setForeground(COLOR_AZUL_MARINO);
        lblMarca.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnIniciar = crearBotonAzul("Iniciar sesión", true);
        btnIniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciar.setMaximumSize(new Dimension(260, 44));
        btnIniciar.setPreferredSize(new Dimension(240, 42));
        btnIniciar.addActionListener(e -> rootLayout.show(rootPanel, VISTA_ACCESO));

        JButton btnInvitado = crearBotonAzul("Continuar como invitado", false);
        btnInvitado.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnInvitado.setMaximumSize(new Dimension(260, 44));
        btnInvitado.setPreferredSize(new Dimension(240, 42));
        btnInvitado.addActionListener(e -> entrarComoInvitado());

        JButton btnCrear = crearBotonAzul("Crear cuenta", true);
        btnCrear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrear.setMaximumSize(new Dimension(260, 44));
        btnCrear.setPreferredSize(new Dimension(240, 42));
        btnCrear.addActionListener(e -> rootLayout.show(rootPanel, VISTA_REGISTRO));

        centro.add(Box.createVerticalGlue());
        centro.add(lblMarca);
        centro.add(Box.createVerticalStrut(28));
        centro.add(btnIniciar);
        centro.add(Box.createVerticalStrut(14));
        centro.add(btnInvitado);
        centro.add(Box.createVerticalStrut(14));
        centro.add(btnCrear);
        centro.add(Box.createVerticalGlue());

        contenedor.add(centro, BorderLayout.CENTER);
        return contenedor;
    }


    private JPanel construirPanelAcceso() {
        JPanel contenedor = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_PASTEL_DENSO, 0, getHeight(), COLOR_PASTEL_CLARO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JPanel panelLogin = crearTarjeta("Iniciar Sesión");
        txtLoginCorreo = crearCampoTexto();
        txtLoginPass = crearCampoPassword();
        JButton btnLogin = crearBotonAzul("Entrar", true);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> intentarLogin());

        JButton btnVolver = crearBotonAzul("Volver", false);
        btnVolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolver.addActionListener(e -> rootLayout.show(rootPanel, VISTA_INICIO));

        panelLogin.add(crearEtiqueta("Correo electrónico:"));
        panelLogin.add(Box.createVerticalStrut(4));
        panelLogin.add(txtLoginCorreo);
        panelLogin.add(Box.createVerticalStrut(12));
        panelLogin.add(crearEtiqueta("Contraseña:"));
        panelLogin.add(Box.createVerticalStrut(4));
        panelLogin.add(txtLoginPass);
        panelLogin.add(Box.createVerticalStrut(18));
        panelLogin.add(btnLogin);
        panelLogin.add(Box.createVerticalStrut(10));
        panelLogin.add(btnVolver);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        wrapper.setOpaque(false);
        panelLogin.setPreferredSize(new Dimension(380, 310));
        wrapper.add(panelLogin);
        contenedor.add(wrapper, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirPanelRegistro() {
        JPanel contenedor = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_PASTEL_DENSO, 0, getHeight(), COLOR_PASTEL_CLARO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JPanel panelRegistro = crearTarjeta("Crear Cuenta Nueva");
        txtRegCorreo = crearCampoTexto();
        txtRegPass = crearCampoPassword();
        JButton btnRegistrar = crearBotonAzul("Registrar y Entrar", true);
        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegistrar.addActionListener(e -> intentarRegistro());

        JButton btnVolver = crearBotonAzul("Volver", false);
        btnVolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolver.addActionListener(e -> rootLayout.show(rootPanel, VISTA_INICIO));

        panelRegistro.add(crearEtiqueta("Correo electrónico:"));
        panelRegistro.add(Box.createVerticalStrut(4));
        panelRegistro.add(txtRegCorreo);
        panelRegistro.add(Box.createVerticalStrut(12));
        panelRegistro.add(crearEtiqueta("Contraseña (mín. 4 caracteres):"));
        panelRegistro.add(Box.createVerticalStrut(4));
        panelRegistro.add(txtRegPass);
        panelRegistro.add(Box.createVerticalStrut(18));
        panelRegistro.add(btnRegistrar);
        panelRegistro.add(Box.createVerticalStrut(10));
        panelRegistro.add(btnVolver);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        wrapper.setOpaque(false);
        panelRegistro.setPreferredSize(new Dimension(380, 310));
        wrapper.add(panelRegistro);
        contenedor.add(wrapper, BorderLayout.CENTER);
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
tabbedPane.addChangeListener(e -> {
    int idxCarrito = tabbedPane.indexOfTab("Carrito y Pago");
    if (idxCarrito != -1 && tabbedPane.getSelectedIndex() == idxCarrito) {
        verificarStockAlAbrirCarrito();
    }
});
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


        JButton btnPagar = new JButton("Crear Pedido y Pagar");
        btnPagar.addActionListener(e -> flujoCrearPedidoYPagar());

        botones.add(btnQuitar);
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
        modeloAdminUsuarios = new DefaultTableModel(
                new Object[]{"Correo", "Rol"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAdminUsuarios = new JTable(modeloAdminUsuarios);
        txtCorreoEliminarAdmin = crearCampoTexto();
        txtCorreoEliminarAdmin.setColumns(22);
        tablaAdminUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaAdminUsuarios.getSelectedRow();
                if (fila >= 0) {
                    txtCorreoEliminarAdmin.setText(String.valueOf(modeloAdminUsuarios.getValueAt(fila, 0)));
                }
            }
        });

        JPanel panelUsuariosCorreo = new JPanel(new BorderLayout(0, 6));
        panelUsuariosCorreo.setBorder(BorderFactory.createTitledBorder("Usuarios por Correo"));
        panelUsuariosCorreo.add(new JScrollPane(tablaAdminUsuarios), BorderLayout.CENTER);

        JPanel barraAdminUsuarios = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barraAdminUsuarios.add(new JLabel("Correo:"));
        barraAdminUsuarios.add(txtCorreoEliminarAdmin);
JButton btnEliminarUsuario = new JButton("Eliminar usuario");
btnEliminarUsuario.addActionListener(e -> eliminarUsuarioComoAdminSwing());
barraAdminUsuarios.add(btnEliminarUsuario);
        panelUsuariosCorreo.add(barraAdminUsuarios, BorderLayout.SOUTH);
        panelUsuariosCorreo.setPreferredSize(new Dimension(0, 170));

        JPanel surAdmin = new JPanel(new BorderLayout());
        surAdmin.add(bottomAdmin, BorderLayout.CENTER);
        surAdmin.add(panelUsuariosCorreo, BorderLayout.SOUTH);

        tab.add(topAdmin, BorderLayout.CENTER);
        tab.add(surAdmin, BorderLayout.SOUTH);
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
        java.util.List<ItemCarrito> previos = new java.util.ArrayList<>(repoCarritos.obtenerItemsGuardados(usuario.user()));
        this.carritoActual = repoCarritos.cargarCarritoUsuario(usuario.user(), repoArticulos);
        java.util.List<ItemCarrito> noDisponiblesAlEntrar = new java.util.ArrayList<>();
        for (ItemCarrito item : previos) {
            if (item == null || item.getArticulo() == null) continue;
            String idArt = item.getArticulo().getId();
            var artOpt = repoArticulos.buscarPorId(idArt);
            if (artOpt.isEmpty() || artOpt.get().getStock() <= 0 || artOpt.get().getStock() < item.getCantidad()) {
                noDisponiblesAlEntrar.add(item);
            }
        }
        actualizarVisibilidadTabs();
        refrescarTodo();
        rootLayout.show(rootPanel, VISTA_TIENDA);
        if (!noDisponiblesAlEntrar.isEmpty()) {
            notificarYQuitarArticulosNoDisponibles(
                    noDisponiblesAlEntrar,
                    "Los siguientes art\u00edculos de tu carrito ya no est\u00e1n disponibles:"
            );
            repoCarritos.guardarCarritoUsuario(usuario.user(), carritoActual);
        }
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
        if (modeloAdminUsuarios != null) {
            modeloAdminUsuarios.setRowCount(0);
            for (RegistroUsuario u : repoUsuarios.listarTodos()) {
                modeloAdminUsuarios.addRow(new Object[]{u.user(), u.rol()});
            }
        }
        modeloAdminPedidos.setRowCount(0);
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
            ItemCarrito itemEnCarrito = carritoActual.getItems().get(id);
            if (itemEnCarrito != null && nuevoStock <= 0) {
                notificarYQuitarArticulosNoDisponibles(
                        List.of(itemEnCarrito),
                        "El administrador marcó como sin stock el siguiente artículo:"
                );
            }
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
        ItemCarrito itemEliminado = carritoActual.getItems().get(id);
        servicioTienda.eliminarArticuloEnCascada(id, carritos);
        if (itemEliminado != null) {
            notificarYQuitarArticulosNoDisponibles(
                    List.of(itemEliminado),
                    "El administrador eliminó del catálogo el siguiente artículo:"
            );
        }
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
        JComboBox<String> cmbSubtipo = new JComboBox<>();
        JTextField txtPrecio = new JTextField(10);
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(10, 0, 100000, 1));
        JTextField txtExtra1 = new JTextField(LocalDate.now().plusDays(15).toString(), 14);
        JTextField txtExtra2 = new JTextField("16", 14);
        JTextField txtExtra3 = new JTextField("4K", 14);
        JCheckBox chkBoolExtra = new JCheckBox("Activo", false);
        JLabel lblSubtipo = new JLabel("Subtipo:");
        JLabel lblExtra1 = new JLabel("Caducidad (YYYY-MM-DD):");
        JLabel lblExtra2 = new JLabel("Dato adicional:");
        JLabel lblExtra3 = new JLabel("Resolución:");
        JLabel lblBoolExtra = new JLabel("Característica:");

        Runnable actualizarCamposDetalle = () -> {
            Categoria cat = (Categoria) cmbCategoria.getSelectedItem();
            String sub = (String) cmbSubtipo.getSelectedItem();
            boolean esRopa = (cat == Categoria.ROPA);

            if (cat == Categoria.ALIMENTOS) {
                lblExtra1.setVisible(true);
                txtExtra1.setVisible(true);
                lblExtra1.setText("Caducidad (YYYY-MM-DD):");
                if (txtExtra1.getText().isBlank() || !txtExtra1.getText().contains("-")) {
                    txtExtra1.setText(LocalDate.now().plusDays(15).toString());
                }
                lblExtra2.setVisible(false);
                txtExtra2.setVisible(false);
                lblExtra3.setVisible(false);
                txtExtra3.setVisible(false);
                boolean usaBool = "Lacteos".equals(sub) || "Frutas".equals(sub) || "Verduras".equals(sub);
                lblBoolExtra.setVisible(usaBool);
                chkBoolExtra.setVisible(usaBool);
                if ("Lacteos".equals(sub)) {
                    lblBoolExtra.setText("Lácteo:");
                    chkBoolExtra.setText("Deslactosado");
                } else if ("Frutas".equals(sub) || "Verduras".equals(sub)) {
                    lblBoolExtra.setText("Cultivo:");
                    chkBoolExtra.setText("Orgánico");
                }
            } else if (cat == Categoria.ELECTRONICA) {
                lblBoolExtra.setVisible(false);
                chkBoolExtra.setVisible(false);
                lblExtra1.setVisible(true);
                txtExtra1.setVisible(true);
                lblExtra1.setText("Garantía (meses):");
                if (!txtExtra1.getText().trim().matches("\\d+")) {
                    txtExtra1.setText("12");
                }
                lblExtra2.setVisible(true);
                txtExtra2.setVisible(true);
                boolean esTv = "Television".equals(sub);
                lblExtra3.setVisible(esTv);
                txtExtra3.setVisible(esTv);
                if ("Computadora".equals(sub)) {
                    lblExtra2.setText("Procesador:");
                    txtExtra2.setText("Intel Core i7");
                } else if ("Laptop".equals(sub)) {
                    lblExtra2.setText("RAM (GB):");
                    txtExtra2.setText("16");
                } else if ("Celular".equals(sub)) {
                    lblExtra2.setText("Almacenamiento (GB):");
                    txtExtra2.setText("256");
                } else if (esTv) {
                    lblExtra2.setText("Pulgadas:");
                    txtExtra2.setText("55");
                    lblExtra3.setText("Resolución:");
                    if (txtExtra3.getText().isBlank()) {
                        txtExtra3.setText("4K");
                    }
                }
            } else if (cat == Categoria.ROPA) {
                lblBoolExtra.setVisible(false);
                chkBoolExtra.setVisible(false);
                lblExtra2.setVisible(false);
                txtExtra2.setVisible(false);
                lblExtra3.setVisible(false);
                txtExtra3.setVisible(false);
                lblExtra1.setVisible(true);
                txtExtra1.setVisible(true);
                lblExtra1.setText("Talla:");
                if (txtExtra1.getText().isBlank() || txtExtra1.getText().contains("-")) {
                    txtExtra1.setText("M");
                }
            }
        };

        Runnable poblarSubtipos = () -> {
            Categoria cat = (Categoria) cmbCategoria.getSelectedItem();
            cmbSubtipo.removeAllItems();
            if (cat == Categoria.ALIMENTOS) {
                cmbSubtipo.addItem("Lacteos");
                cmbSubtipo.addItem("Frutas");
                cmbSubtipo.addItem("Verduras");
                cmbSubtipo.addItem("Abarrotes");
                cmbSubtipo.addItem("Salchichoneria");
            } else if (cat == Categoria.ELECTRONICA) {
                cmbSubtipo.addItem("Computadora");
                cmbSubtipo.addItem("Laptop");
                cmbSubtipo.addItem("Celular");
                cmbSubtipo.addItem("Television");
            } else if (cat == Categoria.ROPA) {
                cmbSubtipo.addItem("Hombre");
                cmbSubtipo.addItem("Mujer");
            }
            actualizarCamposDetalle.run();
        };

        cmbCategoria.addActionListener(e -> poblarSubtipos.run());
        cmbSubtipo.addActionListener(e -> actualizarCamposDetalle.run());
        poblarSubtipos.run();

        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        form.add(new JLabel("ID:"));
        form.add(txtId);
        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        form.add(new JLabel("Categoría:"));
        form.add(cmbCategoria);
        form.add(lblSubtipo);
        form.add(cmbSubtipo);
        form.add(new JLabel("Precio base:"));
        form.add(txtPrecio);
        form.add(new JLabel("Stock inicial:"));
        form.add(spnStock);
        form.add(lblExtra1);
        form.add(txtExtra1);
        form.add(lblExtra2);
        form.add(txtExtra2);
        form.add(lblExtra3);
        form.add(txtExtra3);
        form.add(lblBoolExtra);
        form.add(chkBoolExtra);

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
            String subtipo = (String) cmbSubtipo.getSelectedItem();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int stock = (Integer) spnStock.getValue();

            Articulo nuevo;
            if (categoria == Categoria.ALIMENTOS) {
                LocalDate caducidad = LocalDate.parse(txtExtra1.getText().trim());
                boolean flag = chkBoolExtra.isSelected();
                if ("Lacteos".equals(subtipo)) {
                    nuevo = new Lacteos(id, nombre, precio, stock, caducidad, flag);
                } else if ("Frutas".equals(subtipo)) {
                    nuevo = new Frutas(id, nombre, precio, stock, caducidad, flag);
                } else if ("Verduras".equals(subtipo)) {
                    nuevo = new Verduras(id, nombre, precio, stock, caducidad, flag);
                } else if ("Salchichoneria".equals(subtipo)) {
                    nuevo = new Salchichoneria(id, nombre, precio, stock, caducidad);
                } else {
                    nuevo = new Abarrotes(id, nombre, precio, stock, caducidad);
                }
            } else if (categoria == Categoria.ELECTRONICA) {
                int garantia = Integer.parseInt(txtExtra1.getText().trim());
                String extra2 = txtExtra2.getText().trim();
                String extra3 = txtExtra3.getText().trim();
                if ("Computadora".equals(subtipo)) {
                    nuevo = new Computadora(id, nombre, precio, stock, garantia, extra2);
                } else if ("Laptop".equals(subtipo)) {
                    nuevo = new Laptop(id, nombre, precio, stock, garantia, Integer.parseInt(extra2));
                } else if ("Celular".equals(subtipo)) {
                    nuevo = new Celular(id, nombre, precio, stock, garantia, Integer.parseInt(extra2));
                } else if ("Television".equals(subtipo)) {
                    nuevo = new Television(id, nombre, precio, stock, garantia, Integer.parseInt(extra2), extra3);
                } else {
                    nuevo = new Tecnologia(id, nombre, precio, stock, garantia);
                }
            } else if (categoria == Categoria.ROPA) {
                String talla = txtExtra1.getText().trim();
                boolean esHombre = "Hombre".equals(subtipo);
                nuevo = esHombre
                        ? new Hombre(id, nombre, precio, stock, talla)
                        : new Mujer(id, nombre, precio, stock, talla);
            } else {
                throw new IllegalArgumentException("Categoría no soportada: " + categoria);
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
private void notificarYQuitarArticulosNoDisponibles(List<ItemCarrito> afectados, String encabezado) {
    if (afectados == null || afectados.isEmpty()) {
        return;
    }
    StringBuilder detalle = new StringBuilder();
    for (ItemCarrito item : afectados) {
        detalle.append("- ").append(item.getArticulo().getNombre())
               .append(" (ID: ").append(item.getArticulo().getId())
               .append(", en carrito: ").append(item.getCantidad()).append(")\n");
    }
    JOptionPane.showMessageDialog(
            this,
            encabezado + "\n\n" + detalle + "\nSe quitarán de tu carrito de compras.",
            "Artículo no disponible",
            JOptionPane.WARNING_MESSAGE
    );
    for (ItemCarrito item : afectados) {
        carritoActual.quitarArticulo(item.getArticulo().getId());
    }
    guardarCarritoSiAplica();
    refrescarCarrito();
}

private void verificarStockAlAbrirCarrito() {
    List<ItemCarrito> sinStock = servicioTienda.detectarArticulosSinStock(carritoActual);
    notificarYQuitarArticulosNoDisponibles(
            sinStock,
            "Los siguientes artículos en tu carrito ya no tienen stock disponible:"
    );
}

    private void eliminarUsuarioComoAdminSwing() {
        if (esInvitado || usuarioActual == null || usuarioActual.rol() != Rol.ADMINISTRADOR) {
            JOptionPane.showMessageDialog(this, "Acción exclusiva de administrador.");
            return;
        }
        String correoObjetivo = txtCorreoEliminarAdmin != null ? txtCorreoEliminarAdmin.getText().trim() : "";
        if (correoObjetivo.isBlank()) {
            correoObjetivo = JOptionPane.showInputDialog(
                    this,
                    "Correo del usuario registrado a eliminar:"
            );
        }
        if (correoObjetivo == null || correoObjetivo.isBlank()) {
            return;
        }
        JPasswordField campoPassAdmin = new JPasswordField();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                campoPassAdmin,
                "Confirma tu contraseña de administrador:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            boolean eliminado = servicioAuth.eliminarUsuarioComoAdmin(
                    usuarioActual.user(),
                    new String(campoPassAdmin.getPassword()),
                    correoObjetivo
            );
            if (eliminado) {
                repoCarritos.eliminarCarritoUsuario(correoObjetivo);
                persistirEstadoSilencioso();
                refrescarTodo();
                JOptionPane.showMessageDialog(
                        this,
                        "Usuario eliminado y su carrito borrado. Su historial de pedidos se conserva."
                );
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró un usuario con ese correo.");
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al eliminar usuario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarEstiloBase() {
        rootPanel.setOpaque(true);
        rootPanel.setBackground(COLOR_PASTEL_CLARO);
        if (lblSesionActiva != null) {
            lblSesionActiva.setFont(FUENTE_NEGRITA);
            lblSesionActiva.setForeground(COLOR_AZUL_MARINO);
        }
        if (lblTotalCarrito != null) {
            lblTotalCarrito.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTotalCarrito.setForeground(COLOR_AZUL_MARINO);
        }
        if (lblPreviewImagen != null) {
            lblPreviewImagen.setFont(FUENTE_NORMAL);
            lblPreviewImagen.setForeground(COLOR_AZUL_MARINO);
            lblPreviewImagen.setOpaque(true);
            lblPreviewImagen.setBackground(COLOR_PANEL_SUAVE);
            lblPreviewImagen.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 2, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }
        estilizarArbol(rootPanel);
    }

    private void estilizarArbol(Component comp) {
        if (comp instanceof JPanel panel) {
            if (panel.isOpaque()) {
                panel.setBackground(COLOR_FONDO);
            }
            if (panel.getBorder() instanceof javax.swing.border.TitledBorder tb) {
                tb.setTitleColor(COLOR_AZUL_MARINO);
                tb.setTitleFont(FUENTE_SUBTITULO);
                tb.setBorder(BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 2, true));
            }
            for (Component hijo : panel.getComponents()) {
                estilizarArbol(hijo);
            }
        } else if (comp instanceof JScrollPane sp) {
            sp.getViewport().setBackground(Color.WHITE);
            sp.setBorder(BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 1));
            estilizarArbol(sp.getViewport().getView());
        } else if (comp instanceof JTabbedPane tp) {
            tp.setFont(FUENTE_NEGRITA);
            tp.setForeground(COLOR_AZUL_MARINO);
            tp.setBackground(COLOR_PANEL_SUAVE);
            for (int i = 0; i < tp.getTabCount(); i++) {
                estilizarArbol(tp.getComponentAt(i));
            }
        } else if (comp instanceof JTable tabla) {
            estilizarTabla(tabla);
        } else if (comp instanceof JButton btn) {
            configurarBotonAzul(btn, true);
        } else if (comp instanceof JPasswordField pf) {
            pf.setFont(FUENTE_NORMAL);
            pf.setBackground(Color.WHITE);
            pf.setForeground(COLOR_AZUL_MARINO);
            pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        } else if (comp instanceof JTextField tf) {
            tf.setFont(FUENTE_NORMAL);
            tf.setBackground(Color.WHITE);
            tf.setForeground(COLOR_AZUL_MARINO);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        } else if (comp instanceof JComboBox<?> cb) {
            cb.setFont(FUENTE_NORMAL);
            cb.setBackground(COLOR_PANEL_SUAVE);
            cb.setForeground(COLOR_AZUL_MARINO);
        } else if (comp instanceof JLabel lbl && lbl != lblPreviewImagen) {
            if (lbl.getFont() == null || lbl.getFont().getSize() < 20) {
                lbl.setFont(FUENTE_NEGRITA);
            }
            lbl.setForeground(COLOR_AZUL_MARINO);
        }
    }

    private JButton crearBotonAzul(String texto, boolean primario) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                Color base = primario ? COLOR_AZUL_CIELO : COLOR_SELECCION;
                Color over = primario ? COLOR_AZUL_CIELO_HOVER : COLOR_AZUL_CIELO;
                g.setColor(hover ? over : base);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        configurarBotonAzul(btn, primario);
        return btn;
    }

    private void configurarBotonAzul(JButton btn, boolean primario) {
        btn.setFont(primario ? FUENTE_NEGRITA : FUENTE_NORMAL);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setForeground(COLOR_AZUL_MARINO);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_MARINO, primario ? 2 : 1, true),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        if (Boolean.TRUE.equals(btn.getClientProperty("hoverListener"))) {
            return;
        }
        btn.putClientProperty("hoverListener", Boolean.TRUE);
        btn.putClientProperty("hover", Boolean.FALSE);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.putClientProperty("hover", Boolean.TRUE);
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.putClientProperty("hover", Boolean.FALSE);
                btn.repaint();
            }
        });
    }

    private void estilizarTabla(JTable tabla) {
        if (tabla == null) {
            return;
        }
        tabla.setFont(FUENTE_NORMAL);
        tabla.setForeground(COLOR_AZUL_MARINO);
        tabla.setBackground(Color.WHITE);
        tabla.setRowHeight(26);
        tabla.setShowGrid(true);
        tabla.setGridColor(COLOR_AZUL_CIELO);
        tabla.setIntercellSpacing(new Dimension(8, 4));
        tabla.setSelectionBackground(COLOR_SELECCION);
        tabla.setSelectionForeground(COLOR_AZUL_MARINO);
        if (tabla.getTableHeader() != null) {
            tabla.getTableHeader().setFont(FUENTE_NEGRITA);
            tabla.getTableHeader().setBackground(COLOR_AZUL_CIELO);
            tabla.getTableHeader().setForeground(COLOR_AZUL_MARINO);
        }
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(224, 242, 254), 0, getHeight(), Color.WHITE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 2, true),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_SUBTITULO);
        lblTitulo.setForeground(COLOR_AZUL_MARINO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(14));
        return tarjeta;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_NEGRITA);
        lbl.setForeground(COLOR_AZUL_MARINO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(FUENTE_NORMAL);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COLOR_AZUL_MARINO);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setPreferredSize(new Dimension(260, 34));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return campo;
    }

    private JPasswordField crearCampoPassword() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(FUENTE_NORMAL);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COLOR_AZUL_MARINO);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setPreferredSize(new Dimension(260, 34));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_AZUL_MARINO, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return campo;
    }


    private long ultimaMarcaCatalogo = Long.MIN_VALUE;
    private long ultimoTamCatalogo = -1L;

    private void sincronizarCatalogoDesdeDisco() {
        if (!isDisplayable() || (!esInvitado && usuarioActual == null)) {
            return;
        }
        Path archivoCat = Configuracion.ARCHIVO_CATALOGO;
        if (archivoCat == null || !Files.exists(archivoCat)) {
            return;
        }
        try {
            long mod = Files.getLastModifiedTime(archivoCat).toMillis();
            long tam = Files.size(archivoCat);
            if (mod == ultimaMarcaCatalogo && tam == ultimoTamCatalogo) {
                return;
            }
            ultimaMarcaCatalogo = mod;
            ultimoTamCatalogo = tam;
            repoArticulos.cargarDesdeJson(archivoCat, persistencia);
            List<ItemCarrito> noDisponibles = new ArrayList<>();
            List<ItemCarrito> snapshotItems = new ArrayList<>(carritoActual.getItems().values());
            boolean huboCambioCarrito = false;
            for (ItemCarrito item : snapshotItems) {
                if (item == null || item.getArticulo() == null) continue;
                String idArt = item.getArticulo().getId();
                Optional<Articulo> opt = repoArticulos.buscarPorId(idArt);
                if (opt.isEmpty() || opt.get().getStock() <= 0) {
                    noDisponibles.add(item);
                    huboCambioCarrito = true;
                } else {
                    Articulo vigente = opt.get();
                    if (vigente.getStock() < item.getCantidad()) {
                        carritoActual.quitarArticulo(idArt);
                        carritoActual.agregarArticulo(vigente, vigente.getStock());
                        huboCambioCarrito = true;
                    }
                }
            }
            refrescarTodo();
            if (!noDisponibles.isEmpty()) {
                notificarYQuitarArticulosNoDisponibles(
                        noDisponibles,
                        "Un art\u00edculo de tu carrito fue eliminado o se qued\u00f3 sin stock:"
                );
            } else if (huboCambioCarrito) {
                guardarCarritoSiAplica();
                refrescarCarrito();
            }
        } catch (Exception ignored) {
        }
    }
}
