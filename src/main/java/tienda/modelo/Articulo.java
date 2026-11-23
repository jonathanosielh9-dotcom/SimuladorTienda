package tienda.modelo;

import tienda.excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.Objects;

public class Articulo {

    private final String id;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int stock;

    public Articulo(String id, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del artículo no puede estar vacío");
        }
        this.id = id.trim();
        setNombre(nombre);
        setCategoria(categoria);
        setPrecio(precio);
        setStock(stock);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del artículo no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = Objects.requireNonNull(categoria, "La categoría no puede ser nula");
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni nulo");
        }
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }

    public void descontarStock(int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero");
        }
        if (cantidad > this.stock) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para '" + nombre + "': disponible " + stock + ", solicitado " + cantidad
            );
        }
        this.stock -= cantidad;
    }

    public void reponerStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reponer debe ser mayor a cero");
        }
        this.stock += cantidad;
    }
}
