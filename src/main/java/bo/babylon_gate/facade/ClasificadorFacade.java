package bo.babylon_gate.facade;

import bo.babylon_gate.dto.ClasificadorIdNombreDto;
import bo.babylon_gate.dto.ProductoDto;
import bo.babylon_gate.repository.ClasificadorRepository;
import bo.babylon_gate.repository.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
public class ClasificadorFacade {

    @Inject
    ClasificadorRepository clasificadorRepository;

    public List<ClasificadorIdNombreDto> listarCategoriasProducto() {
        return clasificadorRepository.listarCategorias();
    }

    public List<ClasificadorIdNombreDto> listarMarcasProducto() {
        return clasificadorRepository.listarMarcas();
    }

    public List<ClasificadorIdNombreDto> listarProveedoresProducto() {
        return clasificadorRepository.listarProveedores();
    }

    public List<ClasificadorIdNombreDto> listarUnidadesVentaProducto() {
        return clasificadorRepository.listarUnidades();
    }

    public Integer crearCategoria(bo.babylon_gate.dto.CategoriaDto dto) {
        return clasificadorRepository.insertarCategoria(dto);
    }

    public Integer crearMarca(String nombre) {
        return clasificadorRepository.insertarMarca(nombre);
    }

    public Integer crearProveedor(String nombre) {
        return clasificadorRepository.insertarProveedor(nombre);
    }

    public Integer crearUnidad(String nombre) {
        return clasificadorRepository.insertarUnidad(nombre);
    }
}