package bo.babylon_gate.facade;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.repository.ProcesosTemporalesRepository;
import bo.babylon_gate.repository.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProcesosTemporalesFacade {

    @Inject
    ProcesosTemporalesRepository procesosTemporalesRepository;

    public Long registrarNuevoIngresoTemporal() {
        return procesosTemporalesRepository.registrarNuevoIngresoTemporal();
    }

    public void registrarNuevoDetalleIngresoTemporal(TempIngresoDetalleDto detalleIngresoTemp) {
        procesosTemporalesRepository.registrarNuevoDetalleIngresoTemporal(detalleIngresoTemp);
    }

    public List<Long> obtenerListadoProcesosActivosIngreso() {
        return procesosTemporalesRepository.obtenerListadoProcesosActivosIngreso();
    }

    public Optional<CompraDto> obtenerCabeceraProcesoIngresoTemporal(Long nroProceso) {
        return procesosTemporalesRepository.obtenerCabeceraProcesoIngresoTemporal(nroProceso);
    };

    public List<TempIngresoDetalleDto> obtenerListaDetallesIngresoTemporal(Long idPorcesoTemporal) {
        return procesosTemporalesRepository.obtenerListaDetallesIngresoTemporal(idPorcesoTemporal);
    };

    public void cancelarProcesoIngreso(Long nroProceso) { procesosTemporalesRepository.cancelarProcesoIngreso(nroProceso); }}