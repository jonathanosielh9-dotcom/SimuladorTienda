package tienda.servicio;

import tienda.modelo.ItemCarrito;
import tienda.modelo.Pedido;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class GeneradorComprobantes {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final Path carpetaComprobantes;

    public GeneradorComprobantes() {
        this(Path.of("comprobantes"));
    }

    public GeneradorComprobantes(Path carpetaComprobantes) {
        this.carpetaComprobantes = Objects.requireNonNull(carpetaComprobantes, "La carpeta de comprobantes es obligatoria");
    }

    public String formatearComprobante(Pedido pedido) {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        COMPROBANTE DE PEDIDO\n");
        sb.append("========================================\n");
        sb.append("ID Pedido : ").append(pedido.getId()).append("\n");
        sb.append("Usuario   : ").append(pedido.getCorreoUsuario()).append("\n");
        sb.append("Estado    : ").append(pedido.getEstado()).append("\n");
        if (pedido.getFechaCreacion() != null) {
            sb.append("Fecha     : ").append(FORMATO_FECHA.format(pedido.getFechaCreacion())).append("\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("ARTICULOS:\n");
        for (ItemCarrito item : pedido.getItems()) {
            sb.append("- ").append(item.getArticulo().getNombre())
              .append(" x").append(item.getCantidad())
              .append(" ($").append(item.getSubtotal()).append(")\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("TOTAL     : $").append(pedido.getTotal()).append("\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    public Path guardarComprobanteTxt(Pedido pedido) throws IOException {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo");
        Files.createDirectories(carpetaComprobantes);
        String nombreSeguro = pedido.getId().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destino = carpetaComprobantes.resolve("pedido_" + nombreSeguro + ".txt");
        Files.writeString(destino, formatearComprobante(pedido), StandardCharsets.UTF_8);
        return destino;
    }
}
