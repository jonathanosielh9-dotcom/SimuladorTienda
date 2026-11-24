package tienda.servicio;

import io.github.cdimascio.dotenv.Dotenv;
import tienda.modelo.Autenticable;
import tienda.modelo.RegistroUsuario;
import tienda.modelo.Rol;
import tienda.persistencia.RepositorioUsuarios;
import tienda.seguridad.HashContrasena;

import java.util.Objects;
import java.util.Optional;

public class ServicioAutenticacion implements Autenticable {

    private static final String PIMIENTA_POR_DEFECTO = "pimienta-predeterminada-simulador-tienda-2026";

    private final RepositorioUsuarios repositorioUsuarios;
    private final HashContrasena hashContrasena;

    public ServicioAutenticacion(RepositorioUsuarios repositorioUsuarios) {
        this(repositorioUsuarios, new HashContrasena(resolverPimienta()));
    }

    public ServicioAutenticacion(RepositorioUsuarios repositorioUsuarios, HashContrasena hashContrasena) {
        this.repositorioUsuarios = Objects.requireNonNull(repositorioUsuarios, "El repositorio de usuarios es obligatorio");
        this.hashContrasena = Objects.requireNonNull(hashContrasena, "La utilidad de hash de contraseña es obligatoria");
    }

    private static String resolverPimienta() {
        String env = System.getenv("TIENDA_PIMIENTA");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String dotVal = dotenv.get("TIENDA_PIMIENTA");
            if (dotVal != null && !dotVal.isBlank()) {
                return dotVal.trim();
            }
        } catch (RuntimeException ignored) {
        }
        return PIMIENTA_POR_DEFECTO;
    }

    public RegistroUsuario registrar(String correo, String contrasena, Rol rol) {
        String correoNormalizado = validarCorreo(correo);
        validarContrasena(contrasena);
        Rol rolFinal = Objects.requireNonNull(rol, "El rol es obligatorio");

        if (repositorioUsuarios.buscarPorCorreo(correoNormalizado).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con el correo: " + correoNormalizado);
        }

        String hash = hashContrasena.cifrar(contrasena);
        RegistroUsuario nuevo = new RegistroUsuario(correoNormalizado, hash, rolFinal);
        repositorioUsuarios.guardar(nuevo);
        return nuevo;
    }

    @Override
    public Optional<RegistroUsuario> autenticar(String correo, String contrasena) {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isEmpty()) {
            return Optional.empty();
        }
        String correoNormalizado = correo.trim().toLowerCase();
        Optional<RegistroUsuario> encontrado = repositorioUsuarios.buscarPorCorreo(correoNormalizado);
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        RegistroUsuario usuario = encontrado.get();
        if (usuario.password() == null || usuario.password().isBlank()) {
            return Optional.empty();
        }
        if (hashContrasena.verificar(contrasena, usuario.password())) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    public boolean verificarCredencialesAdmin(String correoAdmin, String contrasenaAdmin) {
        return autenticar(correoAdmin, contrasenaAdmin)
                .map(u -> u.rol() == Rol.ADMINISTRADOR)
                .orElse(false);
    }

    public boolean eliminarUsuarioComoAdmin(String correoAdmin, String contrasenaAdmin, String correoObjetivo) {
        if (!verificarCredencialesAdmin(correoAdmin, contrasenaAdmin)) {
            throw new SecurityException("Verificación de administrador fallida: contraseña incorrecta.");
        }
        if (correoObjetivo == null || correoObjetivo.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario a eliminar no puede estar vacío");
        }
        String objetivoNormalizado = correoObjetivo.trim().toLowerCase();
        if (objetivoNormalizado.equals(correoAdmin.trim().toLowerCase())) {
            throw new IllegalArgumentException("El administrador no puede eliminar su propia cuenta activa");
        }
        return repositorioUsuarios.eliminarPorCorreo(objetivoNormalizado);
    }

    private String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        String normalizado = correo.trim().toLowerCase();
        if (!normalizado.contains("@") || !normalizado.contains(".")) {
            throw new IllegalArgumentException("El formato del correo no es válido");
        }
        return normalizado;
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
    }
}
