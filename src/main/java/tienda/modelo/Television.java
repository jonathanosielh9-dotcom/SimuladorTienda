package tienda.modelo;

import java.math.BigDecimal;

public class Television extends Tecnologia {

    private int pulgadas;
    private String resolucion;

    public Television(
            String id,
            String nombre,
            BigDecimal precio,
            int stock,
            int garantiaMeses,
            int pulgadas,
            String resolucion) {
        super(id, nombre, precio, stock, garantiaMeses);
        setPulgadas(pulgadas);
        setResolucion(resolucion);
    }

    public int getPulgadas() {
        return pulgadas;
    }

    public void setPulgadas(int pulgadas) {
        if (pulgadas <= 0) {
            throw new IllegalArgumentException("Las pulgadas deben ser mayores a cero");
        }
        this.pulgadas = pulgadas;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        if (resolucion == null || resolucion.isBlank()) {
            throw new IllegalArgumentException("La resolución no puede estar vacía");
        }
        this.resolucion = resolucion.trim();
    }
}
