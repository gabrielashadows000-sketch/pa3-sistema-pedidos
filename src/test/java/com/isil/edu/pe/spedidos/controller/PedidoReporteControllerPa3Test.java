package com.isil.edu.pe.spedidos.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PedidoReporteControllerPa3Test {

  private PedidoReporteService service;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    service = mock(PedidoReporteService.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new PedidoReporteController(service)).build();
  }

  @Test
  @DisplayName("Return HTTP 400 JSON from clientes-frecuentes endpoint when Service rejects minPedidos")
  void returnHttp400JsonFromClientesFrecuentesEndpointWhenServiceRejectsMinPedidos() throws Exception {
    // Arrange
    when(service.listarClientesFrecuentes(0))
        .thenThrow(new PedidoException("minPedidos es obligatorio y debe ser un entero mayor o igual a 1."));

    // Act
    ResultActions response = mockMvc.perform(
        get("/api/v1/pedidos/clientes-frecuentes").param("minPedidos", "0"));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  @DisplayName("Return HTTP 400 from busqueda-avanzada endpoint when totalMinimo is not a number")
  void returnHttp400JsonFromBusquedaAvanzadaEndpointWhenTotalMinimoIsNotANumber() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .param("cliente", "Ana")
        .param("totalMinimo", "abc"));

    // Assert
    response.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Return aggregated JSON from clientes-frecuentes endpoint when minPedidos is valid")
  void returnAggregatedJsonFromClientesFrecuentesEndpointWhenMinPedidosIsValid() throws Exception {
    // Arrange
    when(service.listarClientesFrecuentes(eq(2))).thenReturn(List.of(
        new ClienteFrecuente("Ana Torres", 2, 4, new BigDecimal("5100.00"), new BigDecimal("2550.00"))));

    // Act
    ResultActions response = mockMvc.perform(
        get("/api/v1/pedidos/clientes-frecuentes").param("minPedidos", "2"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
        .andExpect(jsonPath("$[0].totalPedidos").value(2))
        .andExpect(jsonPath("$[0].totalUnidades").value(4))
        .andExpect(jsonPath("$[0].importeAcumulado").value(5100.00))
        .andExpect(jsonPath("$[0].ticketPromedio").value(2550.00));
  }
}
