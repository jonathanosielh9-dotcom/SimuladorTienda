package tienda.modelo;

import java.math.BigDecimal;

public interface MetodoPago {

    boolean procesar(BigDecimal monto);
}
