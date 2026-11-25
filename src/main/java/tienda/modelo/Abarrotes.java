package tienda.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Abarrotes extends Alimento {

    public Abarrotes(String id, String nombre, BigDecimal precio, int stock, LocalDate fechaCaducidad) {
        super(id, nombre, precio, stock, fechaCaducidad);
    }
}
