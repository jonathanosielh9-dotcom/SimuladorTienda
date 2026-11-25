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
import java.nio.file.Path;

public final class MainSwing {

    private MainSwing() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            PersistenciaJson persistencia = new PersistenciaJson();
            RepositorioUsuarios repoUsuarios = new RepositorioUsuarios(Configuracion.ARCHIVO_USUARIOS);
            RepositorioArticulos repoArticulos = new RepositorioArticulos();
            RepositorioPedidos repoPedidos = new RepositorioPedidos();
            Path rutaCarritos = Configuracion.ARCHIVO_USUARIOS.toAbsolutePath().getParent() != null
                    ? Configuracion.ARCHIVO_USUARIOS.toAbsolutePath().getParent().resolve("carritos.json")
                    : Path.of("carritos.json").toAbsolutePath().getParent().resolve("carritos.json");
            RepositorioCarritos repoCarritos = new RepositorioCarritos(rutaCarritos);

            try {
                repoArticulos.cargarDesdeJson(Configuracion.ARCHIVO_CATALOGO, persistencia);
                repoPedidos.cargarDesdeJson(Configuracion.ARCHIVO_PEDIDOS, persistencia);
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
