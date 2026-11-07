package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompraCompletaDetalladaDto {
    private Long id;
    private String nroProceso;
    private String fechaCompra;
    private BigDecimal totalCompra;
    private String observacion;
    private String apiEstado;
    private List<ItemDetalleCompraDto> detalles;
}