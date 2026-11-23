package tienda.persistencia;

import tienda.modelo.Pedido;

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
}
