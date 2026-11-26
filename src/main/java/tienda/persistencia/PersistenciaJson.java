package tienda.persistencia;

import tienda.modelo.Articulo;
import tienda.modelo.Pedido;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PersistenciaJson {

    private static final int MAX_REINTENTOS = 5;
    private static final long ESPERA_REINTENTO_MS = 120L;

    private final JsonMapper mapper;

    public PersistenciaJson() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("tienda.modelo.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.time.")
                .build();

        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .activateDefaultTypingAsProperty(ptv, DefaultTyping.NON_FINAL, "tipo")
                .build();
    }

    public JsonMapper getMapper() {
        return mapper;
    }

    public void guardarArticulos(Path archivo, List<Articulo> articulos) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(articulos, "La lista de artículos no puede ser nula");
        escribirAtomicoConReintentos(archivo, articulos);
    }

    public List<Articulo> cargarArticulos(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        return leerConReintentos(archivo, new TypeReference<List<Articulo>>() {});
    }

    public void guardarPedidos(Path archivo, List<Pedido> pedidos) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(pedidos, "La lista de pedidos no puede ser nula");
        escribirAtomicoConReintentos(archivo, pedidos);
    }

    public List<Pedido> cargarPedidos(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        return leerConReintentos(archivo, new TypeReference<List<Pedido>>() {});
    }

    private <T> List<T> leerConReintentos(Path archivo, TypeReference<List<T>> tipo) throws IOException {
        if (Files.notExists(archivo)) {
            return Collections.emptyList();
        }
        IOException ultimoError = null;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                if (Files.notExists(archivo) || Files.size(archivo) == 0) {
                    return Collections.emptyList();
                }
                List<T> cargados = mapper.readValue(archivo.toFile(), tipo);
                return cargados != null ? cargados : Collections.emptyList();
            } catch (IOException e) {
                ultimoError = e;
                pausarReintento(intento);
            }
        }
        throw ultimoError;
    }

    private void escribirAtomicoConReintentos(Path archivo, Object contenido) throws IOException {
        Path destino = archivo.toAbsolutePath().normalize();
        Path directorio = destino.getParent();
        if (directorio != null) {
            Files.createDirectories(directorio);
        }
        IOException ultimoError = null;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            Path temporal = null;
            try {
                temporal = Files.createTempFile(directorio, destino.getFileName().toString() + "_", ".tmp");
                mapper.writeValue(temporal.toFile(), contenido);
                moverAtomico(temporal, destino);
                return;
            } catch (IOException e) {
                ultimoError = e;
                if (temporal != null) {
                    try {
                        Files.deleteIfExists(temporal);
                    } catch (IOException ignored) {
                    }
                }
                pausarReintento(intento);
            }
        }
        throw ultimoError;
    }

    private void moverAtomico(Path origen, Path destino) throws IOException {
        try {
            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void pausarReintento(int intento) {
        if (intento >= MAX_REINTENTOS) {
            return;
        }
        try {
            Thread.sleep(ESPERA_REINTENTO_MS * intento);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
