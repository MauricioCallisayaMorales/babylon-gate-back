package bo.babylon_gate.facade;

import bo.babylon_gate.dto.CompraDto;
import bo.babylon_gate.dto.TempIngresoDetalleDto;
import bo.babylon_gate.dto.TempVentaDetalleDto;
import bo.babylon_gate.dto.VentaDto;
import bo.babylon_gate.repository.ProcesosTemporalesRepository;
import bo.babylon_gate.repository.ProcesosTemporalesVentaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProcesosTemporalesVentaFacade {

    @Inject
    ProcesosTemporalesVentaRepository repository;

    public Long registrarNuevaVentaTemporal() {
        return repository.registrarNuevaVentaTemporal();
    }

    public void registrarNuevoDetalleVentaTemporal(TempVentaDetalleDto detalle) {
        repository.registrarNuevoDetalleVentaTemporal(detalle);
    }

    public List<Long> obtenerListadoProcesosActivosVenta() {
        return repository.obtenerListadoProcesosActivosVenta();
    }

    public Optional<VentaDto> obtenerCabeceraProcesoVentaTemporal(Long nroProceso) {
        return repository.obtenerCabeceraProcesoVentaTemporal(nroProceso);
    }

    public List<TempVentaDetalleDto> obtenerListaDetallesVentaTemporal(Long idProcesoTemporal) {
        return repository.obtenerListaDetallesVentaTemporal(idProcesoTemporal);
    }

    public void cancelarProcesoVenta(Long nroProceso) { repository.cancelarProcesoVenta(nroProceso); }

    public void eliminarDetalleVentaTemporal(Long idDetalle) {
        repository.eliminarDetalleVentaTemporal(idDetalle);
    }

    public void actualizarPrecioDetalleVentaTemporal(Long idDetalle, java.math.BigDecimal nuevoTotal, java.math.BigDecimal nuevoPrecioUnitario) {
        repository.actualizarPrecioDetalleVentaTemporal(idDetalle, nuevoTotal, nuevoPrecioUnitario);
    }
}