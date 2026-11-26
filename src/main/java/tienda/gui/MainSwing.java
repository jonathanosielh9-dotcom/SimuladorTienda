package tienda.gui;

import tienda.config.Configuracion;
import tienda.persistencia.PersistenciaJson;
import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioCarritos;
import tienda.persistencia.RepositorioPedidos;
import tienda.persistencia.RepositorioUsuarios;
import tienda.servicio.GeneradorComprobantes;
import tienda.servicio.GestorImagenes;
import tienda.servicio.ServicioAutenticacion;
import tienda.servicio.ServicioTienda;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MainSwing {

    private MainSwing() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            String dirProp = System.getProperty("tienda.dir", "").trim();
            Path baseDir = !dirProp.isEmpty() ? Path.of(dirProp)
                    : (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()
                            ? Path.of(args[0].trim()) : null);
            if (baseDir != null) {
                try {
                    Files.createDirectories(baseDir);
                } catch (IOException e) {
                    throw new IllegalStateException("No se pudo crear el directorio compartido: " + baseDir, e);
                }
            }
            Path archivoUsuarios = baseDir != null ? baseDir.resolve("usuarios.json") : Configuracion.ARCHIVO_USUARIOS;
            Path archivoCatalogo = baseDir != null ? baseDir.resolve("catalogo.json") : Configuracion.ARCHIVO_CATALOGO;
            Path archivoPedidos = baseDir != null ? baseDir.resolve("pedidos.json") : Configuracion.ARCHIVO_PEDIDOS;
            Path rutaCarritos = baseDir != null ? baseDir.resolve("carritos.json")
                    : (archivoUsuarios.toAbsolutePath().getParent() != null
                            ? archivoUsuarios.toAbsolutePath().getParent().resolve("carritos.json")
                            : Path.of("carritos.json").toAbsolutePath().getParent().resolve("carritos.json"));

            PersistenciaJson persistencia = new PersistenciaJson();
            RepositorioUsuarios repoUsuarios = new RepositorioUsuarios(archivoUsuarios);
            RepositorioArticulos repoArticulos = new RepositorioArticulos();
            RepositorioPedidos repoPedidos = new RepositorioPedidos();
            RepositorioCarritos repoCarritos = new RepositorioCarritos(rutaCarritos);

            try {
                if (Files.exists(archivoCatalogo)) {
                    repoArticulos.cargarDesdeJson(archivoCatalogo, persistencia);
                }
                if (Files.exists(archivoPedidos)) {
                    repoPedidos.cargarDesdeJson(archivoPedidos, persistencia);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error al cargar datos iniciales de la tienda", e);
            }

            ServicioAutenticacion servicioAuth = new ServicioAutenticacion(repoUsuarios);
            servicioAuth.asegurarAdministradorInicial();

            ServicioTienda servicioTienda = new ServicioTienda(repoArticulos, repoPedidos);
            GestorImagenes gestorImagenes = new GestorImagenes();
            GeneradorComprobantes generadorComprobantes = new GeneradorComprobantes();

            VentanaPrincipalSwing ventana = new VentanaPrincipalSwing(
                    servicioAuth,
                    servicioTienda,
                    repoUsuarios,
                    repoArticulos,
                    repoPedidos,
                    repoCarritos,
                    persistencia,
                    gestorImagenes,
                    generadorComprobantes
            );
            ventana.setVisible(true);
        });
    }
}
