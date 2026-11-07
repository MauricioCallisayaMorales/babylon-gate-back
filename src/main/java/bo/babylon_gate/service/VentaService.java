package bo.babylon_gate.service;

import bo.babylon_gate.dto.*;
import bo.babylon_gate.facade.ProductoFacade;
import bo.babylon_gate.facade.VentaFacade;
import bo.babylon_gate.repository.VentaRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaService {

    @Inject
    VentaFacade ventaFacade;

    @POST
    @Path("/crear/venta/{numeroProceso}")
    public Response crear(@PathParam("numeroProceso") Long numeroProceso,
                          @QueryParam("totalManual") BigDecimal totalManual,
                          @QueryParam("metodoPago") String metodoPago,
                          @QueryParam("montoRecibido") BigDecimal montoRecibido,
                          @QueryParam("cambioEntregado") BigDecimal cambioEntregado) {
        try {
            ventaFacade.guardarVenta(numeroProceso, totalManual, metodoPago, montoRecibido, cambioEntregado);
            return Response.ok("Venta oficial registrada y stock descontado").build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/historial/general")
    public List<VentaHistorialGeneralDto> obtenerHistorialGeneral() {
        return ventaFacade.obtenerHistorialGeneral();
    }

    @GET
    @Path("/historial/detalle/{idVenta}")
    public Response obtenerDetalleVentaCompleta(@PathParam("idVenta") Long idVenta) {
        try {
            VentaCompletaDetalladaDto dto = ventaFacade.obtenerVentaDetallada(idVenta);
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/actualizar")
    public Response actualizar(VentaCompletaDetalladaDto dto) {
        try {
            ventaFacade.actualizarVentaCompleta(dto);
            return Response.ok("Registro de venta actualizado con éxito").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/anular/{idVenta}")
    public Response anularVenta(@PathParam("idVenta") Long idVenta) {
        try {
            ventaFacade.anularVenta(idVenta);
            return Response.ok("Venta anulada y stock revertido correctamente").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
}