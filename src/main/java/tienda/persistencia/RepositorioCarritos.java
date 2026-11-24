package tienda.persistencia;

import tienda.excepciones.StockInsuficienteException;
import tienda.modelo.Articulo;
import tienda.modelo.Carrito;
import tienda.modelo.ItemCarrito;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RepositorioCarritos {

    private final Path archivo;
    private final JsonMapper mapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, List<ItemCarrito>> carritosPorUsuario = new LinkedHashMap<>();

    public RepositorioCarritos(Path archivo) {
        this.archivo = Objects.requireNonNull(archivo, "La ruta del archivo de carritos no puede ser nula");
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        cargarInicial();
    }

    private JavaType tipoMapaCarritos() {
        TypeFactory tf = mapper.getTypeFactory();
        JavaType tipoListaItems = tf.constructCollectionType(ArrayList.class, ItemCarrito.class);
        return tf.constructMapType(LinkedHashMap.class, tf.constructType(String.class), tipoListaItems);
    }

    private void cargarInicial() {
        lock.writeLock().lock();
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            if (Files.exists(archivo) && Files.size(archivo) > 0) {
                Map<String, List<ItemCarrito>> leidos = mapper.readValue(archivo.toFile(), tipoMapaCarritos());
                if (leidos != null) {
                    carritosPorUsuario.clear();
                    for (Map.Entry<String, List<ItemCarrito>> entry : leidos.entrySet()) {
                        String clave = entry.getKey();
                        List<ItemCarrito> items = entry.getValue();
                        if (clave != null) {
                            carritosPorUsuario.put(
                                    clave.trim().toLowerCase(),
                                    items != null ? new ArrayList<>(items) : new ArrayList<>()
                            );
                        }
                    }
                }
            } else {
                escribirAtomico();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el repositorio de carritos en " + archivo, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void guardarCarritoUsuario(String correoUsuario, Carrito carrito) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return;
        }
        Objects.requireNonNull(carrito, "El carrito no puede ser nulo");
        String clave = correoUsuario.trim().toLowerCase();
        lock.writeLock().lock();
        try {
            if (carrito.estaVacio()) {
                carritosPorUsuario.remove(clave);
            } else {
                carritosPorUsuario.put(clave, new ArrayList<>(carrito.getItems().values()));
            }
            escribirAtomico();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Carrito cargarCarritoUsuario(String correoUsuario, RepositorioArticulos repoArticulos) {
        Carrito carrito = new Carrito();
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return carrito;
        }
        String clave = correoUsuario.trim().toLowerCase();
        List<ItemCarrito> guardados;
        lock.readLock().lock();
        try {
            List<ItemCarrito> lista = carritosPorUsuario.get(clave);
            guardados = lista != null ? new ArrayList<>(lista) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }

        for (ItemCarrito itemGuardado : guardados) {
            Optional<Articulo> actualOpt = repoArticulos.buscarPorId(itemGuardado.getArticulo().getId());
            if (actualOpt.isPresent()) {
                Articulo actual = actualOpt.get();
                int cantidadAjustada = Math.min(itemGuardado.getCantidad(), actual.getStock());
                if (cantidadAjustada > 0) {
                    try {
                        carrito.agregarArticulo(actual, cantidadAjustada);
                    } catch (StockInsuficienteException ignored) {
                    }
                }
            }
        }
        return carrito;
    }

    public List<ItemCarrito> obtenerItemsGuardados(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return Collections.emptyList();
        }
        String clave = correoUsuario.trim().toLowerCase();
        lock.readLock().lock();
        try {
            List<ItemCarrito> lista = carritosPorUsuario.get(clave);
            return lista != null ? Collections.unmodifiableList(lista) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void eliminarCarritoUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return;
        }
        String clave = correoUsuario.trim().toLowerCase();
        lock.writeLock().lock();
        try {
            if (carritosPorUsuario.remove(clave) != null) {
                escribirAtomico();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirAtomico() {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            Path temporal = Files.createTempFile(archivo.getParent(), "carritos-", ".tmp");
            mapper.writeValue(temporal.toFile(), carritosPorUsuario);
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar carritos en " + archivo, e);
        }
    }
}
