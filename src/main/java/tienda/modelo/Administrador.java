package tienda.modelo;

import tienda.persistencia.RepositorioArticulos;
import tienda.persistencia.RepositorioUsuarios;

import java.util.Objects;

public class Administrador extends Usuario {

    private final String id;

    public Administrador(String id, String correo, String contrasena) {
        super(correo, contrasena);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del administrador no puede estar vacío");
        }
        this.id = id.trim();
    }

    public String getId() {
        return id;
    }

    public void agregarProducto(RepositorioArticulos repoArticulos, Articulo articulo) {
        Objects.requireNonNull(repoArticulos, "El repositorio de artículos es obligatorio");
        Objects.requireNonNull(articulo, "El artículo es obligatorio");
        repoArticulos.guardar(articulo);
    }

    public boolean eliminarUsuario(RepositorioUsuarios repoUsuarios, String correoUsuario) {
        Objects.requireNonNull(repoUsuarios, "El repositorio de usuarios es obligatorio");
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return false;
        }
        return repoUsuarios.eliminarPorCorreo(correoUsuario.trim());
    }
}
