package tienda.modelo;

import java.math.BigDecimal;

public class PagoTransferencia implements MetodoPago {

    private final String referencia;
    private final String clabe;
    private final String banco;

    public PagoTransferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia de transferencia no puede estar vacía");
        }
        this.referencia = referencia.trim();
        this.clabe = null;
        this.banco = null;
    }

    public PagoTransferencia(String clabe, String banco, String referencia) {
        if (clabe == null || !clabe.trim().matches("\\d{18}")) {
            throw new IllegalArgumentException("La CLABE interbancaria debe tener exactamente 18 dígitos");
        }
        if (banco == null || banco.isBlank()) {
            throw new IllegalArgumentException("El banco no puede estar vacío");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia de transferencia no puede estar vacía");
        }
        this.clabe = clabe.trim();
        this.banco = banco.trim();
        this.referencia = referencia.trim();
    }

    public String getReferencia() {
        return referencia;
    }

    public String getClabe() {
        return clabe;
    }

    public String getBanco() {
        return banco;
    }

    public String mostrarDatos() {
        StringBuilder sb = new StringBuilder();
        if (banco != null) {
            sb.append("Banco: ").append(banco);
        }
        if (clabe != null) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("CLABE: ").append(clabe);
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append("Referencia: ").append(referencia);
        return sb.toString();
    }

    @Override
    public boolean procesar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (clabe != null && !clabe.matches("\\d{18}")) {
            return false;
        }
        return true;
    }
}
