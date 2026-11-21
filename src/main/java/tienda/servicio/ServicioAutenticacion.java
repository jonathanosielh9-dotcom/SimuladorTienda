package tienda.servicio;

import tienda.excepciones.ContrasenaIncorrectaException;
import tienda.excepciones.CorreoDuplicadoException;
import tienda.modelo.Autenticable;
import tienda.modelo.RegistroUsuario;
import tienda.modelo.Rol;
import tienda.persistencia.RepositorioUsuarios;
import tienda.seguridad.HashContrasena;

import java.util.Optional;

public class ServicioAutenticacion implements Autenticable {

    private final RepositorioUsuarios repositorio;
    private final HashContrasena hashContrasena;

    public ServicioAutenticacion(RepositorioUsuarios repositorio, HashContrasena hashContrasena) {
        this.repositorio = repositorio;
        this.hashContrasena = hashContrasena;
    }

    public RegistroUsuario registrar(String correo, String contrasenaPlano, Rol rol)
            throws CorreoDuplicadoException {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        if (contrasenaPlano == null || contrasenaPlano.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        String correoNormalizado = correo.trim().toLowerCase();
        if (repositorio.buscarPorCorreo(correoNormalizado).isPresent()) {
            throw new CorreoDuplicadoException(correoNormalizado);
        }
        String hash = hashContrasena.cifrar(contrasenaPlano);
        RegistroUsuario nuevo = new RegistroUsuario(
                correoNormalizado,
                hash,
                rol != null ? rol : Rol.USUARIO_REGISTRADO
        );
        repositorio.guardar(nuevo);
        return nuevo;
    }

    @Override
    public Optional<RegistroUsuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }
        return repositorio.buscarPorCorreo(correo)
                .filter(u -> hashContrasena.verificar(contrasena, u.password()));
    }

    public RegistroUsuario iniciarSesion(String correo, String contrasena)
            throws ContrasenaIncorrectaException {
        Optional<RegistroUsuario> encontrado = repositorio.buscarPorCorreo(correo);
        if (encontrado.isEmpty() || !hashContrasena.verificar(contrasena, encontrado.get().password())) {
            throw new ContrasenaIncorrectaException("Correo o contraseña incorrectos");
        }
        return encontrado.get();
    }
}
