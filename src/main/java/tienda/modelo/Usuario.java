package tienda.modelo;

import java.util.Objects;

public class Usuario {

    private String correo;
    private String contrasena;

    public Usuario(String correo, String contrasena) {
        setCorreo(correo);
        setContrasena(contrasena);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        this.correo = correo.trim();
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        this.contrasena = contrasena;
    }

    public boolean autenticar(String contrasenaCandidata) {
        if (contrasenaCandidata == null) {
            return false;
        }
        return Objects.equals(this.contrasena, contrasenaCandidata);
    }
}
