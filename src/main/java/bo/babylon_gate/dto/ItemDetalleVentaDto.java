package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDetalleVentaDto {
    private Long idDetalle;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String marcaProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
    private Boolean esMayorista;
    private String observacion;
    private String lotesAsignados;
}