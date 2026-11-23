package tienda.modelo;

import tienda.excepciones.TarjetaInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Pago {

    private final String id;
    private final String pedidoId;
    private final BigDecimal monto;
    private final String terminacionTarjeta;
    private final Instant fechaPago;

    public Pago(String pedidoId, BigDecimal monto, String numeroTarjeta) throws TarjetaInvalidaException {
        if (pedidoId == null || pedidoId.isBlank()) {
            throw new IllegalArgumentException("El id del pedido no puede estar vacío");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        String soloDigitos = validarYNormalizarTarjeta(numeroTarjeta);
        this.id = UUID.randomUUID().toString();
        this.pedidoId = pedidoId;
        this.monto = monto;
        this.terminacionTarjeta = soloDigitos.substring(soloDigitos.length() - 4);
        this.fechaPago = Instant.now();
    }

    private String validarYNormalizarTarjeta(String numeroTarjeta) throws TarjetaInvalidaException {
        if (numeroTarjeta == null) {
            throw new TarjetaInvalidaException("El número de tarjeta no puede ser nulo");
        }
        String limpio = numeroTarjeta.replaceAll("[\\s-]", "");
        if (!limpio.matches("\\d{13,19}")) {
            throw new TarjetaInvalidaException("El número de tarjeta debe tener entre 13 y 19 dígitos");
        }
        return limpio;
    }

    public String getId() {
        return id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getTerminacionTarjeta() {
        return terminacionTarjeta;
    }

    public Instant getFechaPago() {
        return fechaPago;
    }
}
