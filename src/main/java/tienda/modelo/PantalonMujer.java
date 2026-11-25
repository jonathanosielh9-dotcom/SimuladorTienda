package tienda.modelo;

import java.math.BigDecimal;

public class PantalonMujer extends Moda {

    private final String genero = "MUJER";

    public PantalonMujer(String id, String nombre, BigDecimal precio, int stock, String talla) {
        super(id, nombre, precio, stock, talla);
    }

    public String getGenero() {
        return genero;
    }
}
