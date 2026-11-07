package bo.babylon_gate.dto;

import lombok.Data;

@Data
public class CategoriaDto {
    private String nombre;
    private Integer vidaUtilDias;
    private Integer alertaAmarillaPorc;
    private Integer alertaRojaPorc;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getVidaUtilDias() { return vidaUtilDias; }
    public void setVidaUtilDias(Integer vidaUtilDias) { this.vidaUtilDias = vidaUtilDias; }
    public Integer getAlertaAmarillaPorc() { return alertaAmarillaPorc; }
    public void setAlertaAmarillaPorc(Integer alertaAmarillaPorc) { this.alertaAmarillaPorc = alertaAmarillaPorc; }
    public Integer getAlertaRojaPorc() { return alertaRojaPorc; }
    public void setAlertaRojaPorc(Integer alertaRojaPorc) { this.alertaRojaPorc = alertaRojaPorc; }
}