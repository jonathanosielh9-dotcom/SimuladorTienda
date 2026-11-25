package tienda.modelo;

import java.math.BigDecimal;

public class PagoWallet implements MetodoPago {

    private final String cuentaWallet;
    private BigDecimal saldo;

    public PagoWallet(String cuentaWallet) {
        this(cuentaWallet, BigDecimal.ZERO);
    }

    public PagoWallet(String cuentaWallet, BigDecimal saldoInicial) {
        if (cuentaWallet == null || cuentaWallet.isBlank()) {
            throw new IllegalArgumentException("La cuenta de wallet no puede estar vacía");
        }
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
        this.cuentaWallet = cuentaWallet.trim();
        this.saldo = saldoInicial;
    }

    public String getCuentaWallet() {
        return cuentaWallet;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public boolean cobrar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (saldo.compareTo(monto) < 0) {
            return false;
        }
        saldo = saldo.subtract(monto);
        return true;
    }

    @Override
    public boolean procesar(BigDecimal monto) {
        return cobrar(monto);
    }
}
