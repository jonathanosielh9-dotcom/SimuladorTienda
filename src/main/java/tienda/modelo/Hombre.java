package tienda.modelo;

import java.math.BigDecimal;

public class Hombre extends Moda {

    public Hombre(String id, String nombre, BigDecimal precio, int stock, String talla) {
        super(id, nombre, precio, stock, talla);
    }

    public Hombre(String id, String nombre, BigDecimal precio, int stock, String talla, String color) {
        super(id, nombre, precio, stock, talla, color);
    }
}
