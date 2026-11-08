package bo.babylon_gate.facade;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.repository.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@ApplicationScoped
public class ProductoFacade {

    @Inject
    ProductoRepository productoRepository;

    public void registrarNuevoProducto(ProductoDto producto) {
        productoRepository.insertarNuevoProducto(producto);
    }

    public List<ListaGeneralProductoDto> obtenerListadoProductos() {
        List<ListaGeneralProductoDto> resultadoProductos = productoRepository.listarProductosCompletos();
        return resultadoProductos;
    }

    public List<ProductoInventarioDto> obtenerListadoPrincipalInventarioProductos() {
        List<ProductoInventarioDto> resultados = productoRepository.listarProductosParaInventario();
        return resultados;
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductoScannerDto procesarEscaneo(String codigoBarras) {
        Optional<ProductoScannerDto> resultadoLocal = productoRepository.buscarPorCodigoBarrasEnBD(codigoBarras);
        if (resultadoLocal.isPresent()) {
            return resultadoLocal.get();
        }

        ProductoScannerDto dto = new ProductoScannerDto();
        dto.setExisteEnSistema(false);
        dto.setCodigo(codigoBarras);
        dto.setIdCodBarras(codigoBarras);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://world.openfoodfacts.org/api/v0/product/" + codigoBarras + ".json"))
                    .header("User-Agent", "TiendaGladysApp/1.0")
                    .GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());

                if (root.path("status").asInt() == 1) {
                    JsonNode product = root.path("product");

                    dto.setDetalle(product.path("product_name").asText("Producto desconocido"));
                    dto.setMarca(product.path("brands").asText("S/M"));
                    dto.setCantidad(product.path("quantity").asText(null));
                    dto.setImagenUrl(product.path("image_url").asText(null));
                    dto.setPrecioVenta(java.math.BigDecimal.ZERO);
                    dto.setPrecioMayorista(java.math.BigDecimal.ZERO);
                    dto.setStockActual(0);
                } else {
                    dto.setDetalle("Producto no encontrado en red mundial");
                    dto.setMarca("N/A");
                }
            }
        } catch (Exception e) {
            dto.setDetalle("Error de conexión con servicio externo");
        }
        return dto;
    }

    public void actualizarRegistroProducto(ProductoDto producto) {
        productoRepository.actualizarRegistroProducto(producto);
    }

    public void asociarCodigoBarrasAProducto(Long idProducto, String codigoBarras) {
        productoRepository.asociarCodigoBarras(idProducto, codigoBarras);
    }

    public List<ProductoLoteDto> obtenerLotesActivos(Long idProducto) {
        return productoRepository.listarLotesActivosPorProducto(idProducto);
    }

    public void darDeBajaLote(BajaLoteDto baja) {
        if (baja.getCantidadBaja() == null || baja.getCantidadBaja() <= 0) {
            throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor a cero");
        }
        productoRepository.procesarBajaDeLote(baja);
    }

    public List<ProductoEmpaqueDto> obtenerEmpaquesPorProducto(Long idProducto) {
        return productoRepository.listarEmpaquesPorProducto(idProducto);
    }

    public List<ProductoEmpaqueDto> obtenerTodosLosEmpaques() {
        return productoRepository.listarTodosLosEmpaques();
    }

    public void cambiarEstadoProducto(Long idProducto, String nuevoEstado) {
        productoRepository.cambiarEstadoProducto(idProducto, nuevoEstado);
    }

    public List<ProductoProveedorDto> obtenerProveedoresPorProducto(Long idProducto) {
        return productoRepository.listarProveedoresPorProducto(idProducto);
    }

    public List<KardexDto> obtenerKardex(Long idProducto) {
        return productoRepository.obtenerKardexInventario(idProducto);
    }

    public void actualizarPreciosRapido(Long idProducto, java.math.BigDecimal precioVenta, java.math.BigDecimal precioMayorista) {
        productoRepository.actualizarPreciosBase(idProducto, precioVenta, precioMayorista);
    }

    public void aplicarOfertaLote(Long idLote, java.math.BigDecimal precioOferta) {
        productoRepository.aplicarOfertaLote(idLote, precioOferta);
    }

    public List<Map<String, Object>> listarAlmacenes() {
        List<Object[]> filas = productoRepository.listarAlmacenesActivos();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] f : filas) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f[0]);
            map.put("nombre", f[1]);
            lista.add(map);
        }
        return lista;
    }

    public void procesarTrasladoLote(Long idLote, Integer cantidad, Integer idAlmacenDestino) {
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        productoRepository.trasladarLoteDeAlmacen(idLote, cantidad, idAlmacenDestino);
    }

    public String obtenerImagenEstandar(Long idProducto) {
        return productoRepository.obtenerImagenEstandar(idProducto);
    }
}