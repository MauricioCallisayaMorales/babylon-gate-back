package bo.babylon_gate.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class ProductoEmpaqueDto {
    private Integer idUnidad;
    private String unidadNombre;
    private String codigoBarras;
    private Integer cantidadBase;
    private BigDecimal precioVenta;
    private BigDecimal precioMayorista;

    private Long idProducto;
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
}