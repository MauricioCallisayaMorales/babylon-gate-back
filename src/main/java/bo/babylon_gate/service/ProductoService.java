package bo.babylon_gate.service;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.facade.ProductoFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoService {

    @Inject
    ProductoFacade productoFacade;

    @POST
    @Path("/crear")
    public Response crearProducto(ProductoDto dto) {
        try {
            productoFacade.registrarNuevoProducto(dto);
            return Response.status(Response.Status.CREATED).entity("Producto registrado con éxito").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/listado")
    public Response listarTodosLosProductos() {
        try {
            List<ListaGeneralProductoDto> productos = productoFacade.obtenerListadoProductos();
            return Response.status(Response.Status.OK).entity(productos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @GET
    @Path("/inventario")
    public Response obtenerInventarioProductos() {
        try {
            List<ProductoInventarioDto> productos = productoFacade.obtenerListadoPrincipalInventarioProductos();
            return Response.status(Response.Status.OK).entity(productos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/scan/{codigoBarras}")
    public Response escanearProducto(@PathParam("codigoBarras") String codigoBarras) {
        ProductoScannerDto resultado = productoFacade.procesarEscaneo(codigoBarras);
        return Response.ok(resultado).build();
    }

    @POST
    @Path("/actualizar")
    public Response actualizarProducto(ProductoDto producto) {
        try {
            productoFacade.actualizarRegistroProducto(producto);
            return Response.ok("Registro de producto actualizado con éxito").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/asociar-codigo/{idProducto}/{codigoBarras}")
    public Response asociarCodigo(@PathParam("idProducto") Long idProducto, @PathParam("codigoBarras") String codigoBarras) {
        try {
            productoFacade.asociarCodigoBarrasAProducto(idProducto, codigoBarras);
            return Response.ok("Código de barras asociado correctamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{idProducto}/lotes")
    public Response listarLotes(@PathParam("idProducto") Long idProducto) {
        try {
            return Response.ok(productoFacade.obtenerLotesActivos(idProducto)).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/lote/baja")
    public Response darDeBajaLote(BajaLoteDto baja) {
        try {
            productoFacade.darDeBajaLote(baja);
            return Response.ok("Baja de inventario registrada con éxito").build();
        } catch (Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{idProducto}/empaques")
    public Response obtenerEmpaques(@PathParam("idProducto") Long idProducto) {
        try {
            return Response.ok(productoFacade.obtenerEmpaquesPorProducto(idProducto)).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/empaques/todos")
    public Response obtenerTodosLosEmpaques() {
        try {
            return Response.ok(productoFacade.obtenerTodosLosEmpaques()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{idProducto}/estado")
    public Response cambiarEstadoProducto(@PathParam("idProducto") Long idProducto, @QueryParam("nuevoEstado") String nuevoEstado) {
        try {
            productoFacade.cambiarEstadoProducto(idProducto, nuevoEstado);
            return Response.ok("Estado actualizado correctamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{idProducto}/proveedores")
    public Response obtenerProveedores(@PathParam("idProducto") Long idProducto) {
        try {
            return Response.ok(productoFacade.obtenerProveedoresPorProducto(idProducto)).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/kardex")
    public Response obtenerKardex(@QueryParam("idProducto") Long idProducto) {
        try {
            return Response.ok(productoFacade.obtenerKardex(idProducto)).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{idProducto}/precios")
    public Response actualizarPrecios(@PathParam("idProducto") Long idProducto, @QueryParam("venta") java.math.BigDecimal precioVenta, @QueryParam("mayorista") java.math.BigDecimal precioMayorista) {
        try {
            productoFacade.actualizarPreciosRapido(idProducto, precioVenta, precioMayorista);
            return Response.ok("Precios actualizados").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/lote/{idLote}/oferta")
    public Response aplicarOfertaLote(@PathParam("idLote") Long idLote, @QueryParam("precio") java.math.BigDecimal precioOferta) {
        try {
            productoFacade.aplicarOfertaLote(idLote, precioOferta);
            return Response.ok("Oferta aplicada correctamente").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/almacenes")
    public Response obtenerAlmacenes() {
        return Response.ok(productoFacade.listarAlmacenes()).build();
    }

    @POST
    @Path("/lote/trasladar")
    public Response trasladarLote(@QueryParam("idLote") Long idLote, @QueryParam("cantidad") Integer cantidad, @QueryParam("idAlmacenDestino") Integer idAlmacenDestino) {
        try {
            productoFacade.procesarTrasladoLote(idLote, cantidad, idAlmacenDestino);
            return Response.ok("Traslado de almacén exitoso").build();
        } catch (Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}