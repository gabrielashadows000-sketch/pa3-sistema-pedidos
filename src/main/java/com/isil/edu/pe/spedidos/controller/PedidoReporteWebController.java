package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.ClienteFrecuenteDto;
import com.isil.edu.pe.spedidos.controller.dto.PedidoResponse;
import com.isil.edu.pe.spedidos.controller.dto.ResumenPedidosDto;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PA3 - Vista MVC/JSP del módulo de reportes.
 *
 * <p>Flujo: Spring Security -> DispatcherServlet -> este Controller ->
 * {@link PedidoReporteService} -> PedidoReporteJdbcRepository (Spring JDBC) -> MySQL.
 * El Controller solo prepara el {@link Model} y devuelve el nombre lógico de la vista;
 * las reglas de validación y el SQL viven en el Service y el Repository, que son los
 * mismos que usa la API JSON.</p>
 */
@Controller
@RequestMapping("/reportes")
public class PedidoReporteWebController {

  private final PedidoReporteService reporteService;

  public PedidoReporteWebController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  /**
   * GET /reportes/pedidos
   *
   * <p>Siempre carga el resumen. La búsqueda avanzada se ejecuta si llega
   * {@code cliente} o {@code totalMinimo}; los clientes frecuentes, si llega
   * {@code minPedidos}.</p>
   */
  @GetMapping("/pedidos")
  public String reportePedidos(
      @RequestParam(required = false) String cliente,
      @RequestParam(required = false) BigDecimal totalMinimo,
      @RequestParam(required = false) Integer minPedidos,
      Model model,
      HttpServletResponse response) {

    model.addAttribute("resumen", ResumenPedidosDto.from(reporteService.obtenerResumen()));
    model.addAttribute("clienteIngresado", cliente);
    model.addAttribute("totalMinimoIngresado", totalMinimo);
    model.addAttribute("minPedidosIngresado", minPedidos);

    if (cliente != null || totalMinimo != null) {
      try {
        model.addAttribute("pedidosBusqueda",
            reporteService.buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo)
                .stream()
                .map(PedidoResponse::from)
                .toList());
        model.addAttribute("busquedaRealizada", true);
      } catch (PedidoException e) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorBusqueda", e.getMessage());
      }
    }

    if (minPedidos != null) {
      try {
        model.addAttribute("clientesFrecuentes",
            reporteService.listarClientesFrecuentes(minPedidos)
                .stream()
                .map(ClienteFrecuenteDto::from)
                .toList());
        model.addAttribute("frecuentesRealizada", true);
      } catch (PedidoException e) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorFrecuentes", e.getMessage());
      }
    }

    return "reportes-pedidos";
  }
}
