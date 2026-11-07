package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VentaCompletaDetalladaDto {
    private Long id;
    private String nroProceso;
    private String fechaVenta;
    private BigDecimal totalVenta;
    private String clienteNombre;
    private String observacion;
    private String apiEstado;
    private List<ItemDetalleVentaDto> detalles;
}