package tienda.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Frutas extends Alimento {

    private boolean organica;

    public Frutas(String id, String nombre, BigDecimal precio, int stock, LocalDate fechaCaducidad, boolean organica) {
        super(id, nombre, precio, stock, fechaCaducidad);
        this.organica = organica;
    }

    public boolean isOrganica() {
        return organica;
    }

    public void setOrganica(boolean organica) {
        this.organica = organica;
    }
}
