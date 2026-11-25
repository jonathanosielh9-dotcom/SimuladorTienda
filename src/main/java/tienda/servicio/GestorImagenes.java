package tienda.servicio;

import tienda.config.Configuracion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public class GestorImagenes {

    private final Path carpetaImagenes;
    private final Path imagenGenerica;

    public GestorImagenes() {
        this(Configuracion.CARPETA_IMAGENES, Configuracion.IMAGEN_GENERICA);
    }

    public GestorImagenes(Path carpetaImagenes, Path imagenGenerica) {
        this.carpetaImagenes = Objects.requireNonNull(carpetaImagenes, "La carpeta de imágenes es obligatoria");
        this.imagenGenerica = Objects.requireNonNull(imagenGenerica, "La ruta de imagen genérica es obligatoria");
    }

    public String prepararImagenProducto(Path origenSeleccionado) throws IOException {
        if (origenSeleccionado == null) {
            return normalizarRuta(imagenGenerica);
        }
        if (Files.notExists(origenSeleccionado) || Files.isDirectory(origenSeleccionado)) {
            throw new FileNotFoundException("No se encontró el archivo de imagen: " + origenSeleccionado);
        }
        Files.createDirectories(carpetaImagenes);
        String nombreArchivo = origenSeleccionado.getFileName().toString();
        Path destino = carpetaImagenes.resolve(nombreArchivo);
        Files.copy(origenSeleccionado, destino, StandardCopyOption.REPLACE_EXISTING);
        return normalizarRuta(destino);
    }

    public Optional<Path> resolverImagenParaMostrar(String rutaImagenProducto, String nombreProductoFallback) {
        if (rutaImagenProducto != null && !rutaImagenProducto.isBlank()) {
            Path candidata = Path.of(rutaImagenProducto.trim());
            if (Files.exists(candidata) && Files.isRegularFile(candidata)) {
                return Optional.of(candidata);
            }
        }
        if (Files.exists(imagenGenerica) && Files.isRegularFile(imagenGenerica)) {
            return Optional.of(imagenGenerica);
        }
        return Optional.empty();
    }

    public String textoRespaldoImagen(String nombreProducto) {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            return "Producto sin imagen";
        }
        return nombreProducto.trim();
    }

    private String normalizarRuta(Path ruta) {
        return ruta.toString().replace('\\', '/');
    }
}
