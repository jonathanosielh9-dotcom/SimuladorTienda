package tienda.modelo;

import java.math.BigDecimal;

public class Computadora extends Tecnologia {

    private String procesador;

    public Computadora(String id, String nombre, BigDecimal precio, int stock, int garantiaMeses, String procesador) {
        super(id, nombre, precio, stock, garantiaMeses);
        setProcesador(procesador);
    }

    public String getProcesador() {
        return procesador;
    }

    public void setProcesador(String procesador) {
        if (procesador == null || procesador.isBlank()) {
            throw new IllegalArgumentException("El procesador no puede estar vacío");
        }
        this.procesador = procesador.trim();
    }
}
