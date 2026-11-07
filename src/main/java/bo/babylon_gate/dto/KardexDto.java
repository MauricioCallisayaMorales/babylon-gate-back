package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KardexDto {
    private Long id;
    private String fechaMovimiento;
    private String nombreProducto;
    private String tipoMovimiento;
    private Integer cantidad;
    private Integer saldoActual;
    private String referencia;
}