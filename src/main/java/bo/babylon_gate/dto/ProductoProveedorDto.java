package bo.babylon_gate.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class ProductoProveedorDto {
    private Long id;
    private Long idProducto;
    private Integer idProveedor;
    private BigDecimal costoReferencial;
    private String codigoProveedor;
    private Boolean proveedorPrincipal;
    private String apiEstado;
    private String apiTransaccion;
    private String usuCre;
    private String fecCre;
    private String usuMod;
    private String fecMod;

    private Integer idUnidad;
    public Integer getIdUnidad() { return idUnidad; }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }
}