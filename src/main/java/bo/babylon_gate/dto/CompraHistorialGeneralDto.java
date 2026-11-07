package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompraHistorialGeneralDto {
    private Long id;
    private String nroProceso;
    private String fechaCompra;
    private BigDecimal totalCompra;
    private String observacion;
    private String apiEstado;
}