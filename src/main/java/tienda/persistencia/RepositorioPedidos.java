package tienda.persistencia;

import tienda.modelo.Pedido;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RepositorioPedidos {

    private final Map<String, Pedido> pedidos = new LinkedHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void guardar(Pedido pedido) {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo");
        lock.writeLock().lock();
        try {
            pedidos.put(pedido.getId(), pedido);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Pedido> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        lock.readLock().lock();
        try {
            return Optional.ofNullable(pedidos.get(id.trim()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Pedido> buscarPorUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return Collections.emptyList();
        }
        String normalizado = correoUsuario.trim().toLowerCase();
        lock.readLock().lock();
        try {
            return pedidos.values().stream()
                    .filter(p -> p.getCorreoUsuario().equalsIgnoreCase(normalizado))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Pedido> listarTodos() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(pedidos.values()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void guardarEnJson(Path archivo, PersistenciaJson persistencia) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(persistencia, "La instancia de PersistenciaJson no puede ser nula");
        List<Pedido> copia;
        lock.readLock().lock();
        try {
            copia = new ArrayList<>(pedidos.values());
        } finally {
            lock.readLock().unlock();
        }
        persistencia.guardarPedidos(archivo, copia);
    }

    public void cargarDesdeJson(Path archivo, PersistenciaJson persistencia) throws IOException {
        Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        Objects.requireNonNull(persistencia, "La instancia de PersistenciaJson no puede ser nula");
        List<Pedido> cargados = persistencia.cargarPedidos(archivo);
        lock.writeLock().lock();
        try {
            pedidos.clear();
            for (Pedido pedido : cargados) {
                pedidos.put(pedido.getId(), pedido);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
