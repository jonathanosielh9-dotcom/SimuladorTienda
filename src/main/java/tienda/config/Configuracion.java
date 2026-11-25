package tienda.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Path;

public final class Configuracion {

    public static final Path ARCHIVO_CATALOGO = Path.of("catalogo.json");
    public static final Path ARCHIVO_USUARIOS = Path.of("usuarios.json");
    public static final Path ARCHIVO_PEDIDOS = Path.of("pedidos.json");
    public static final Path CARPETA_IMAGENES = Path.of("imagenes");
    public static final Path IMAGEN_GENERICA = CARPETA_IMAGENES.resolve("generica.png");

    private static final Dotenv ENV = Dotenv.configure().ignoreIfMissing().load();

    private Configuracion() {}

    public static String obtener(String clave, String porDefecto) {
        String valor = ENV.get(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    public static String obtenerObligatoria(String clave) {
        String valor = ENV.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable " + clave + " en .env");
        }
        return valor;
    }
}
