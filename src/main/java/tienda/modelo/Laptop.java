package tienda.modelo;

import java.math.BigDecimal;

public class Laptop extends Tecnologia {

    private int ramGb;

    public Laptop(String id, String nombre, BigDecimal precio, int stock, int garantiaMeses, int ramGb) {
        super(id, nombre, precio, stock, garantiaMeses);
        setRamGb(ramGb);
    }

    public int getRamGb() {
        return ramGb;
    }

    public void setRamGb(int ramGb) {
        if (ramGb <= 0) {
            throw new IllegalArgumentException("La memoria RAM en GB debe ser mayor a cero");
        }
        this.ramGb = ramGb;
    }
}
