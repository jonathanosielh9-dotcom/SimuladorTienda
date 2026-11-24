package tienda.modelo;

import java.math.BigDecimal;

public class ArticuloModa extends Articulo {

    private String talla;

    public ArticuloModa(String id, String nombre, BigDecimal precio, int stock, String talla) {
        super(id, nombre, Categoria.ROPA, precio, stock);
        setTalla(talla);
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        if (talla == null || talla.isBlank()) {
            throw new IllegalArgumentException("La talla no puede estar vacía");
        }
        this.talla = talla.trim().toUpperCase();
    }

    @Override
    public BigDecimal getPrecioFinal() {
        return getPrecio();
    }
}
