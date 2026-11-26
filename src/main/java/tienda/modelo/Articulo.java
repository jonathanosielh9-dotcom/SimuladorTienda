package tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tienda.excepciones.StockInsuficienteException;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo", defaultImpl = Articulo.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Tecnologia.class, name = "TECNOLOGIA"),
    @JsonSubTypes.Type(value = Celular.class, name = "CELULAR"),
    @JsonSubTypes.Type(value = Computadora.class, name = "COMPUTADORA"),
    @JsonSubTypes.Type(value = Laptop.class, name = "LAPTOP"),
    @JsonSubTypes.Type(value = Television.class, name = "TELEVISION"),
    @JsonSubTypes.Type(value = Moda.class, name = "MODA"),
    @JsonSubTypes.Type(value = Hombre.class, name = "HOMBRE"),
    @JsonSubTypes.Type(value = Mujer.class, name = "MUJER"),
    @JsonSubTypes.Type(value = Alimento.class, name = "ALIMENTO"),
    @JsonSubTypes.Type(value = Abarrotes.class, name = "ABARROTES"),
    @JsonSubTypes.Type(value = Frutas.class, name = "FRUTAS"),
    @JsonSubTypes.Type(value = Lacteos.class, name = "LACTEOS"),
    @JsonSubTypes.Type(value = Salchichoneria.class, name = "SALCHICHONERIA"),
    @JsonSubTypes.Type(value = Verduras.class, name = "VERDURAS")
})
public class Articulo {

    private final String id;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int stock;
    private String subcategoria;

    @JsonCreator
    public Articulo(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("categoria") Categoria categoria,
            @JsonProperty("precio") BigDecimal precio,
            @JsonProperty("stock") int stock) {
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

    @JsonIgnore
    public BigDecimal getPrecioFinal() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni nulo");
        }
        this.precio = precio;
    }

    public String getSubcategoria() {
        if (subcategoria != null && !subcategoria.isBlank()) {
            return subcategoria;
        }
        return getClass().getSimpleName();
    }

    public void setSubcategoria(String subcategoria) {
        if (subcategoria != null && !subcategoria.isBlank()) {
            this.subcategoria = subcategoria.trim();
        }
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
