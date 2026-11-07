package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaDto {
    private Long id;
    private String nroProceso;
    private String fechaVenta;
    private BigDecimal totalVenta;
    private String clienteNombre;
    private String observacion;
    private String apiEstado;
    private String apiTransaccion;
    private String usuCre;
    private String fecCre;
}