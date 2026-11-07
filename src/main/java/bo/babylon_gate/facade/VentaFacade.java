package bo.babylon_gate.facade;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.repository.CompraRepository;
import bo.babylon_gate.repository.VentaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
public class VentaFacade {

    @Inject
    VentaRepository ventaRepository;

    @Inject
    ProcesosTemporalesVentaFacade procesosTemporalesFacade;

    public void guardarVenta(Long numeroProceso, java.math.BigDecimal totalManual, String metodoPago, java.math.BigDecimal montoRecibido, java.math.BigDecimal cambioEntregado) {
        VentaDto cabecera = procesosTemporalesFacade.obtenerCabeceraProcesoVentaTemporal(numeroProceso)
                .orElseThrow(() -> new IllegalArgumentException("El número de proceso de venta " + numeroProceso + " no existe."));

        List<TempVentaDetalleDto> detalles = procesosTemporalesFacade.obtenerListaDetallesVentaTemporal(cabecera.getId());
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La venta no contiene ningún producto.");
        }
        ventaRepository.registrarVentaCompleta(cabecera, detalles, totalManual, metodoPago, montoRecibido, cambioEntregado);
    }

    public List<VentaHistorialGeneralDto> obtenerHistorialGeneral() {
        return ventaRepository.listarVentasGeneral();
    }

    public VentaCompletaDetalladaDto obtenerVentaDetallada(Long idVenta) {
        VentaCompletaDetalladaDto venta = ventaRepository.buscarVentaCabeceraPorId(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("La venta con ID " + idVenta + " no existe."));

        List<ItemDetalleVentaDto> detalles = ventaRepository.listarDetallesPorVentaId(idVenta);
        venta.setDetalles(detalles);
        return venta;
    }

    public void actualizarVentaCompleta(VentaCompletaDetalladaDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("El ID de la venta es obligatorio.");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un detalle.");
        }
        ventaRepository.actualizarVentaYStock(dto);
    }

    public void anularVenta(Long idVenta) {
        ventaRepository.anularVentaYRevertirStock(idVenta);
    }
}