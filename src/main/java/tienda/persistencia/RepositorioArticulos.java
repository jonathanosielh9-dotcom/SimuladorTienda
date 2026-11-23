package tienda.persistencia;

import tienda.modelo.Articulo;
import tienda.modelo.Categoria;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RepositorioArticulos {

    private final Map<String, Articulo> articulos = new LinkedHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void guardar(Articulo articulo) {
        Objects.requireNonNull(articulo, "El artículo no puede ser nulo");
        lock.writeLock().lock();
        try {
            articulos.put(articulo.getId(), articulo);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Articulo> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        lock.readLock().lock();
        try {
            return Optional.ofNullable(articulos.get(id.trim()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Articulo> listarTodos() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(articulos.values()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Articulo> buscarPorCategoria(Categoria categoria) {
        Objects.requireNonNull(categoria, "La categoría no puede ser nula");
        lock.readLock().lock();
        try {
            return articulos.values().stream()
                    .filter(a -> a.getCategoria() == categoria)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void actualizarPrecioYStock(String id, BigDecimal nuevoPrecio, int nuevoStock) {
        lock.writeLock().lock();
        try {
            Articulo articulo = articulos.get(id);
            if (articulo == null) {
                throw new IllegalArgumentException("No existe un artículo con id: " + id);
            }
            if (nuevoPrecio != null) {
                articulo.setPrecio(nuevoPrecio);
            }
            articulo.setStock(nuevoStock);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void guardarEnJson(Path archivo, PersistenciaJson persistencia) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(persistencia, "La instancia de PersistenciaJson no puede ser nula");
        List<Articulo> copia;
        lock.readLock().lock();
        try {
            copia = new ArrayList<>(articulos.values());
        } finally {
            lock.readLock().unlock();
        }
        persistencia.guardarArticulos(archivo, copia);
    }

    public void cargarDesdeJson(Path archivo, PersistenciaJson persistencia) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(persistencia, "La instancia de PersistenciaJson no puede ser nula");
        List<Articulo> cargados = persistencia.cargarArticulos(archivo);
        lock.writeLock().lock();
        try {
            articulos.clear();
            for (Articulo articulo : cargados) {
                articulos.put(articulo.getId(), articulo);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
