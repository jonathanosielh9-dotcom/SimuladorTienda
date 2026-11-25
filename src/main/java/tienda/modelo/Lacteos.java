package tienda.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Lacteos extends Alimento {

    private boolean deslactosado;

    public Lacteos(String id, String nombre, BigDecimal precio, int stock, LocalDate fechaCaducidad, boolean deslactosado) {
        super(id, nombre, precio, stock, fechaCaducidad);
        this.deslactosado = deslactosado;
    }

    public boolean isDeslactosado() {
        return deslactosado;
    }

    public void setDeslactosado(boolean deslactosado) {
        this.deslactosado = deslactosado;
    }
}
