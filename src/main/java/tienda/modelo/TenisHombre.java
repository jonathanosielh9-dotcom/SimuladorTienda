package tienda.modelo;

import java.math.BigDecimal;

public class TenisHombre extends Moda {

    private final String genero = "HOMBRE";

    public TenisHombre(String id, String nombre, BigDecimal precio, int stock, String talla) {
        super(id, nombre, precio, stock, talla);
    }

    public String getGenero() {
        return genero;
    }
}
