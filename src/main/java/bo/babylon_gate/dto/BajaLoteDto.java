package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BajaLoteDto {
    private Long idLote;
    private Long idProducto;
    private Integer cantidadBaja;
    private String motivo;
}