package tienda.persistencia;

import tienda.modelo.RegistroUsuario;
import tienda.modelo.Rol;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RepositorioUsuarios {

    private final Path archivo;
    private final ObjectMapper mapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioUsuarios(Path archivo) {
        this.archivo = archivo;
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        inicializarArchivo();
    }

    private void inicializarArchivo() {
        lock.writeLock().lock();
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            if (Files.notExists(archivo)) {
                mapper.writeValue(archivo.toFile(), new ArrayList<RegistroUsuario>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar " + archivo, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<RegistroUsuario> listarTodos() {
        lock.readLock().lock();
        try {
            if (Files.notExists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(archivo.toFile(), new TypeReference<List<RegistroUsuario>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer usuarios desde " + archivo, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<RegistroUsuario> buscarPorCorreo(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        String objetivo = correo.trim().toLowerCase();
        return listarTodos().stream()
                .filter(u -> u.user() != null && u.user().trim().toLowerCase().equals(objetivo))
                .findFirst();
    }

    public void guardar(RegistroUsuario nuevoUsuario) {
        lock.writeLock().lock();
        try {
            List<RegistroUsuario> usuarios = listarTodos();
            String correoNormalizado = nuevoUsuario.user().trim().toLowerCase();
            usuarios.removeIf(u -> u.user() != null && u.user().trim().toLowerCase().equals(correoNormalizado));
            usuarios.add(nuevoUsuario);
            escribirAtomico(usuarios);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean eliminarPorCorreo(String correo) {
        lock.writeLock().lock();
        try {
            if (correo == null) {
                return false;
            }
            List<RegistroUsuario> usuarios = listarTodos();
            String correoNormalizado = correo.trim().toLowerCase();
            boolean eliminado = usuarios.removeIf(u -> u.user() != null && u.user().trim().toLowerCase().equals(correoNormalizado));
            if (eliminado) {
                escribirAtomico(usuarios);
            }
            return eliminado;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<RegistroUsuario> listarPorRol(Rol rol) {
        return listarTodos().stream()
                .filter(u -> u.rol() == rol)
                .toList();
    }

    private void escribirAtomico(List<RegistroUsuario> usuarios) {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            Path temporal = Files.createTempFile(archivo.getParent(), "usuarios-", ".tmp");
            mapper.writeValue(temporal.toFile(), usuarios);
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar usuarios en " + archivo, e);
        }
    }
}
