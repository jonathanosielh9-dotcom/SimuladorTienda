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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PersistenciaJson {

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
        prepararDirectorio(archivo);
        mapper.writeValue(archivo.toFile(), articulos);
    }

    public List<Articulo> cargarArticulos(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        if (Files.notExists(archivo)) {
            return Collections.emptyList();
        }
        List<Articulo> cargados = mapper.readValue(archivo.toFile(), new TypeReference<List<Articulo>>() {});
        return cargados != null ? cargados : Collections.emptyList();
    }

    public void guardarPedidos(Path archivo, List<Pedido> pedidos) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(pedidos, "La lista de pedidos no puede ser nula");
        prepararDirectorio(archivo);
        mapper.writeValue(archivo.toFile(), pedidos);
    }

    public List<Pedido> cargarPedidos(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        if (Files.notExists(archivo)) {
            return Collections.emptyList();
        }
        List<Pedido> cargados = mapper.readValue(archivo.toFile(), new TypeReference<List<Pedido>>() {});
        return cargados != null ? cargados : Collections.emptyList();
    }

    private void prepararDirectorio(Path archivo) throws IOException {
        Path padre = archivo.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
    }
}
