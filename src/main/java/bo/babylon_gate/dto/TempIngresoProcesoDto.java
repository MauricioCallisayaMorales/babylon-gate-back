package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TempIngresoProcesoDto {
    private Long id;
    private Long nroProceso;
    private BigDecimal totalAcumulado;
    private String apiEstado;
    private String usuCre;
    private String fecCre;
}