package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaHistorialGeneralDto {
    private Long id;
    private String nroProceso;
    private String fechaVenta;
    private BigDecimal totalVenta;
    private String clienteNombre;
    private String apiEstado;
}