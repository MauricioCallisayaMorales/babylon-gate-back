package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TempIngresoDetalleDto {
    private Long id;
    private Long idTempIngreso;
    private Long idProducto;
    private Integer idProveedor;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private String fechaVencimiento;
    private BigDecimal costoTotal;
    private String observacion;
    private String fecCre;
}