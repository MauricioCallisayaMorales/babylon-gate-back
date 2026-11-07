package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDetalleCompraDto {
    private Long idDetalle;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String marcaProducto;
    private Integer idProveedor;
    private String nombreProveedor;
    private Integer cantidad;
    private BigDecimal costoUnidad;
    private BigDecimal costoTotal;
    private String fechaVencimiento;
}