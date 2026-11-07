package bo.babylon_gate.service;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.facade.CompraFacade;
import bo.babylon_gate.repository.VentaRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/compras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompraService {
    @Inject
    CompraFacade compraFacade;

    @POST
    @Path("/crear/ingreso/{numeroProceso}")
    public Response crear(@PathParam("numeroProceso") Long numeroProceso, @QueryParam("totalManual") java.math.BigDecimal totalManual) {
        try {
            compraFacade.guardarCompra(numeroProceso, totalManual);
            return Response.ok("Ingreso de mercadería registrado correctamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/historial/general")
    public List<CompraHistorialGeneralDto> obtenerHistorialGeneral() {
        return compraFacade.obtenerHistorialGeneral();
    }

    @GET
    @Path("/historial/detalle/{idCompra}")
    public Response obtenerDetalleCompraCompleta(@PathParam("idCompra") Long idCompra) {
        try {
            CompraCompletaDetalladaDto dto = compraFacade.obtenerCompraDetallada(idCompra);
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/actualizar")
    public Response actualizar(CompraCompletaDetalladaDto dto) {
        try {
            compraFacade.actualizarCompraCompleta(dto);
            return Response.ok("Registro de ingreso actualizado con éxito").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno al procesar la actualización: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/anular/{idCompra}")
    public Response anularCompra(@PathParam("idCompra") Long idCompra) {
        try {
            compraFacade.anularCompra(idCompra);
            return Response.ok("Compra anulada y stock descontado correctamente").build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
}