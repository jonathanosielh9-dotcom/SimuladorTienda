package tienda.modelo;

import java.math.BigDecimal;

public class Celular extends Tecnologia {

    private int almacenamientoGb;

    public Celular(String id, String nombre, BigDecimal precio, int stock, int garantiaMeses, int almacenamientoGb) {
        super(id, nombre, precio, stock, garantiaMeses);
        setAlmacenamientoGb(almacenamientoGb);
    }

    public int getAlmacenamientoGb() {
        return almacenamientoGb;
    }

    public void setAlmacenamientoGb(int almacenamientoGb) {
        if (almacenamientoGb <= 0) {
            throw new IllegalArgumentException("El almacenamiento en GB debe ser mayor a cero");
        }
        this.almacenamientoGb = almacenamientoGb;
    }
}
