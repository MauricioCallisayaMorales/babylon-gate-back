package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompraDetalleDto {
    private Long id;
    private Long idCompra;
    private Long idProducto;
    private Integer idProveedor;
    private Integer cantidad;
    private BigDecimal costoUnidad;
    private String fechaVencimiento;
    private BigDecimal costoTotal;
    private Integer stockLote;
    private String apiEstado;
    private String apiTransaccion;
    private String usuCre;
    private String fecCre;
    private String usuMod;
    private String fecMod;
}