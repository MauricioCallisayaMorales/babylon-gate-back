package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TempVentaDetalleDto {
    private Long id;
    private Long idTempVenta;
    private Long idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private Boolean esMayorista;
    private BigDecimal precioTotal;
    private String observacion;
    private String fecCre;
    private Long idLote;
}