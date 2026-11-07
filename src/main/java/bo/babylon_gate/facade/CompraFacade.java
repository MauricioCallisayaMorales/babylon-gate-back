package bo.babylon_gate.facade;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.repository.CompraRepository;
import bo.babylon_gate.repository.ProcesosTemporalesRepository;
import bo.babylon_gate.repository.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
public class CompraFacade {
    @Inject
    CompraRepository compraRepository;
    @Inject
    ProcesosTemporalesFacade procesosTemporalesFacade;

    public void guardarCompra(Long numeroProceso, java.math.BigDecimal totalManual) {
        CompraDto cabeceraCompra = procesosTemporalesFacade.obtenerCabeceraProcesoIngresoTemporal(numeroProceso)
                .orElseThrow(() -> new IllegalArgumentException("El número de proceso " + numeroProceso + " no existe."));

        List<TempIngresoDetalleDto> detallesCompra = procesosTemporalesFacade.obtenerListaDetallesIngresoTemporal(cabeceraCompra.getId());

        if (detallesCompra.isEmpty()) {
            throw new IllegalStateException("El proceso no contiene ningún producto.");
        }

        compraRepository.registrarCompraCompleta(cabeceraCompra, detallesCompra, totalManual);
    }

    public List<CompraHistorialGeneralDto> obtenerHistorialGeneral() {
        return compraRepository.listarComprasGeneral();
    }

    public CompraCompletaDetalladaDto obtenerCompraDetallada(Long idCompra) {
        CompraCompletaDetalladaDto compra = compraRepository.buscarCompraCabeceraPorId(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("La compra con ID " + idCompra + " no existe."));

        List<ItemDetalleCompraDto> detalles = compraRepository.listarDetallesPorCompraId(idCompra);
        compra.setDetalles(detalles);

        return compra;
    }

    public void actualizarCompraCompleta(CompraCompletaDetalladaDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("El ID de la compra es obligatorio para la actualización.");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un detalle de producto.");
        }

        compraRepository.actualizarCompraYStock(dto);
    }

    public void anularCompra(Long idCompra) {
        compraRepository.anularCompraYRevertirStock(idCompra);
    }
}