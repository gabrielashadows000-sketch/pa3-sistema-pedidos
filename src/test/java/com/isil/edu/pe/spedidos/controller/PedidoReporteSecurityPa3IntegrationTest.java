package com.isil.edu.pe.spedidos.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteSecurityPa3IntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    jdbcTemplate.update("DELETE FROM pedido");
    insertarPedido("Ana Torres", "5000.00", 2);
    insertarPedido("Ana Torres", "100.00", 2);
    insertarPedido("Carlos Perez", "150.00", 3);
  }

  @Test
  @DisplayName("Render reportes-pedidos view with model data when CONSULTA requests GET /reportes/pedidos")
  void renderReportesPedidosViewWithModelDataWhenConsultaRequestsGetReportesPedidos() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .param("cliente", "Ana")
        .param("totalMinimo", "1000")
        .param("minPedidos", "2")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attributeExists("resumen", "pedidosBusqueda", "clientesFrecuentes"));
  }

  @Test
  @DisplayName("Redirect anonymous user to login when requesting GET /reportes/pedidos")
  void redirectAnonymousUserToLoginWhenRequestingGetReportesPedidos() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos"));

    // Assert
    response.andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", endsWith("/login")));
  }

  @Test
  @DisplayName("Return HTTP 400 on view when CONSULTA sends empty cliente to GET /reportes/pedidos")
  void returnHttp400OnViewWhenConsultaSendsEmptyClienteToGetReportesPedidos() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .param("cliente", "")
        .param("totalMinimo", "0")
        .with(user("consulta").roles("CONSULTA")));

    // Assert: se muestra la vista con el error de validación, sin consulta SQL
    response.andExpect(status().isBadRequest())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attributeExists("errorBusqueda"))
        .andExpect(model().attributeDoesNotExist("pedidosBusqueda"));
  }

  @Test
  @DisplayName("Return filtered JSON from busqueda-avanzada endpoint when CONSULTA requests existing data")
  void returnFilteredJsonFromBusquedaAvanzadaEndpointWhenConsultaRequestsExistingData() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .param("cliente", "Ana")
        .param("totalMinimo", "1000")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
        .andExpect(jsonPath("$[0].total").value(5000.00));
  }

  @Test
  @DisplayName("Return HTTP 400 from clientes-frecuentes endpoint when OPERADOR sends minPedidos lower than 1")
  void returnHttp400FromClientesFrecuentesEndpointWhenOperadorSendsMinPedidosLowerThanOne() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .param("minPedidos", "0")
        .with(user("operador").roles("OPERADOR")));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @DisplayName("Reject POST /pedidos with HTTP 403 when CONSULTA sends valid CSRF token")
  void rejectPostPedidosWithHttp403WhenConsultaSendsValidCsrfToken() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/pedidos")
        .with(user("consulta").roles("CONSULTA"))
        .with(csrf())
        .param("cliente", "Ana")
        .param("productoCodigo", "P001")
        .param("cantidad", "1"));

    // Assert: la regla de rol rechaza la operación antes de llegar al Service
    response.andExpect(status().isForbidden());
  }

  private void insertarPedido(String cliente, String total, int cantidad) {
    jdbcTemplate.update(
        """
        INSERT INTO pedido (
          cliente, producto_codigo, producto_nombre,
          precio_unitario, cantidad, total, fecha
        ) VALUES (?, 'MOU-001', 'Mouse', ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        cliente, new BigDecimal("50.00"), cantidad, new BigDecimal(total)
    );
  }
}
