package bo.babylon_gate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoDto {
    private Long id;
    private String codigo;
    private String detalle;
    private Integer idCategoria;
    private Integer idMarca;
    private Integer idUnidad;
    private BigDecimal precioVenta;
    private BigDecimal precioMayorista;
    private Integer stockActual;
    private String idCodBarras;
    private String apiEstado;
    private String apiTransaccion;
    private String usuCre;
    private String fecCre;
    private String usuMod;
    private String fecMod;
    private String codigoBarras;

    private List<ProductoProveedorDto> proveedores;
    private List<ProductoEmpaqueDto> empaques;
}