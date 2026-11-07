package bo.babylon_gate.facade;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.repository.ProductoProveedorRepository;
import bo.babylon_gate.repository.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoProveedorFacade {
    @Inject
    ProductoProveedorRepository repository;

    public BigDecimal obtenerCostoReferencial(Long idProducto, Integer idProveedor, Integer idUnidad) {
        return repository.obtenerCostoReferencial(idProducto, idProveedor, idUnidad).orElse(null);
    }

    public void crearRelacion(ProductoProveedorDto dto) {
        repository.crearRelacion(dto);
    }

    public List<ComparativaCostoDto> compararPorProducto(Long idProducto) {
        return repository.listarComparativaPorProducto(idProducto);
    }
    public List<ComparativaCostoDto> compararPorProveedor(Integer idProveedor) {
        return repository.listarComparativaPorProveedor(idProveedor);
    }
}