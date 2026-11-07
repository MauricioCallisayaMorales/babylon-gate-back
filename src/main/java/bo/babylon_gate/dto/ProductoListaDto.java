package bo.babylon_gate.dto;

import java.math.BigDecimal;

public class ProductoListaDto {
    public Integer id;
    public String codigo;
    public String categoria;
    public String detalle;
    public String marca;
    public String unidad;
    public String proveedor;
    public BigDecimal costo;
    public BigDecimal precioVenta;
    public BigDecimal precioMayorista;
    public String estado;

    public ProductoListaDto() {
    }

    public ProductoListaDto(Integer id, String codigo, String categoria, String detalle,
                            String marca, String unidad, String proveedor,
                            BigDecimal costo, BigDecimal precioVenta,
                            BigDecimal precioMayorista, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.categoria = categoria;
        this.detalle = detalle;
        this.marca = marca;
        this.unidad = unidad;
        this.proveedor = proveedor;
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.precioMayorista = precioMayorista;
        this.estado = estado;
    }
}