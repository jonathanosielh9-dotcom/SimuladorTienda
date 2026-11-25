package tienda.modelo;

import tienda.excepciones.TarjetaInvalidaException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public class PagoTarjeta implements MetodoPago {

    public enum Marca {
        VISA,
        MASTERCARD,
        AMEX,
        DESCONOCIDA
    }

    private final String numeroTarjeta;
    private final YearMonth vencimiento;
    private final Marca marca;

    public PagoTarjeta(String numeroTarjeta) {
        String limpio = normalizarNumero(numeroTarjeta);
        if (!limpio.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("El número de tarjeta debe tener entre 13 y 19 dígitos");
        }
        this.numeroTarjeta = limpio;
        this.vencimiento = null;
        this.marca = detectarMarca(limpio);
    }

    public PagoTarjeta(String numeroTarjeta, YearMonth vencimiento) throws TarjetaInvalidaException {
        String limpio = normalizarNumero(numeroTarjeta);
        if (!limpio.matches("\\d{15,16}")) {
            throw new TarjetaInvalidaException("El número de tarjeta debe tener 15 o 16 dígitos");
        }
        YearMonth fechaValida = Objects.requireNonNull(vencimiento, "La fecha de vencimiento es obligatoria");
        if (fechaValida.isBefore(YearMonth.now())) {
            throw new TarjetaInvalidaException("La tarjeta está vencida");
        }
        this.numeroTarjeta = limpio;
        this.vencimiento = fechaValida;
        this.marca = detectarMarca(limpio);
    }

    private static String normalizarNumero(String numero) {
        if (numero == null) {
            throw new IllegalArgumentException("El número de tarjeta no puede ser nulo");
        }
        return numero.replaceAll("[\\s-]", "");
    }

    public static Marca detectarMarca(String numeroNormalizado) {
        if (numeroNormalizado == null || numeroNormalizado.isBlank()) {
            return Marca.DESCONOCIDA;
        }
        if (numeroNormalizado.startsWith("34") || numeroNormalizado.startsWith("37")) {
            return Marca.AMEX;
        }
        if (numeroNormalizado.startsWith("4")) {
            return Marca.VISA;
        }
        if (numeroNormalizado.length() >= 2) {
            try {
                int dosDigitos = Integer.parseInt(numeroNormalizado.substring(0, 2));
                if (dosDigitos >= 51 && dosDigitos <= 55) {
                    return Marca.MASTERCARD;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (numeroNormalizado.length() >= 4) {
            try {
                int cuatroDigitos = Integer.parseInt(numeroNormalizado.substring(0, 4));
                if (cuatroDigitos >= 2221 && cuatroDigitos <= 2720) {
                    return Marca.MASTERCARD;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return Marca.DESCONOCIDA;
    }

    public Marca detectarMarca() {
        return marca;
    }

    public YearMonth getVencimiento() {
        return vencimiento;
    }

    public String getTerminacion() {
        return numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }

    @Override
    public boolean procesar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (!numeroTarjeta.matches("\\d{15,16}")) {
            return false;
        }
        if (vencimiento != null && vencimiento.isBefore(YearMonth.now())) {
            return false;
        }
        return true;
    }
}
