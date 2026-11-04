package com.isil.edu.pe.spedidos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteJdbcRepositoryPa3Test {

  @Autowired
  private PedidoReporteJdbcRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("DELETE FROM pedido");

    // Ana Torres: 2 pedidos, 4 unidades, 5100.00
    insertarPedido("Ana Torres", "LAP-001", "Laptop", "2500.00", 2, "5000.00");
    insertarPedido("Ana Torres", "MOU-001", "Mouse", "50.00", 2, "100.00");
    // Carlos Perez: 1 pedido
    insertarPedido("Carlos Perez", "MOU-001", "Mouse", "50.00", 3, "150.00");
    // Luis Rojas: 2 pedidos, 10 unidades, 500.00
    insertarPedido("Luis Rojas", "TEC-001", "Teclado", "50.00", 4, "200.00");
    insertarPedido("Luis Rojas", "TEC-001", "Teclado", "50.00", 6, "300.00");
  }

  @Test
  @DisplayName("Return only matching orders from buscarPedidosPorClienteYTotalMinimo when customer and minimum total are provided")
  void returnOnlyMatchingOrdersFromBuscarPedidosPorClienteYTotalMinimoWhenCustomerAndMinimumTotalAreProvided() {
    // Arrange
    String cliente = "ana";
    BigDecimal totalMinimo = new BigDecimal("1000.00");

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo);

    // Assert: solo el pedido de 5000.00 de Ana (el de 100.00 y los de otros clientes quedan fuera)
    assertEquals(1, pedidos.size());
    assertEquals("Ana Torres", pedidos.get(0).getCliente());
    assertEquals(0, new BigDecimal("5000.00").compareTo(pedidos.get(0).getTotal()));
  }

  @Test
  @DisplayName("Return orders by id descending including the boundary from buscarPedidosPorClienteYTotalMinimo when total equals minimum")
  void returnOrdersByIdDescendingIncludingBoundaryFromBuscarPedidosPorClienteYTotalMinimoWhenTotalEqualsMinimum() {
    // Arrange
    BigDecimal totalMinimo = new BigDecimal("100.00");

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo("Ana", totalMinimo);

    // Assert: el límite es inclusivo (>=) y el orden es id descendente
    assertEquals(2, pedidos.size());
    assertTrue(pedidos.get(0).getId() > pedidos.get(1).getId());
    assertEquals(0, new BigDecimal("100.00").compareTo(pedidos.get(0).getTotal()));
  }

  @Test
  @DisplayName("Return aggregated customers from listarClientesFrecuentes when minPedidos is 2")
  void returnAggregatedCustomersFromListarClientesFrecuentesWhenMinPedidosIsTwo() {
    // Arrange: Ana y Luis tienen 2 pedidos; Carlos solo 1 y debe quedar fuera por HAVING.

    // Act
    List<ClienteFrecuenteProjection> clientes = repository.listarClientesFrecuentes(2);

    // Assert
    assertEquals(2, clientes.size());

    ClienteFrecuenteProjection ana = clientes.get(0); // mayor importe acumulado primero
    assertEquals("Ana Torres", ana.cliente());
    assertEquals(2L, ana.totalPedidos());
    assertEquals(4L, ana.totalUnidades());
    assertEquals(0, new BigDecimal("5100.00").compareTo(ana.importeAcumulado()));
    assertEquals(0, new BigDecimal("2550.00").compareTo(ana.ticketPromedio()));

    ClienteFrecuenteProjection luis = clientes.get(1);
    assertEquals("Luis Rojas", luis.cliente());
    assertEquals(10L, luis.totalUnidades());
    assertEquals(0, new BigDecimal("250.00").compareTo(luis.ticketPromedio()));
  }

  @Test
  @DisplayName("Return empty list from listarClientesFrecuentes when no customer reaches minPedidos")
  void returnEmptyListFromListarClientesFrecuentesWhenNoCustomerReachesMinPedidos() {
    // Arrange / Act
    List<ClienteFrecuenteProjection> clientes = repository.listarClientesFrecuentes(3);

    // Assert
    assertTrue(clientes.isEmpty());
  }

  private void insertarPedido(String cliente, String productoCodigo, String productoNombre,
      String precioUnitario, int cantidad, String total) {
    jdbcTemplate.update(
        """
        INSERT INTO pedido (
          cliente, producto_codigo, producto_nombre,
          precio_unitario, cantidad, total, fecha
        ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        cliente, productoCodigo, productoNombre,
        new BigDecimal(precioUnitario), cantidad, new BigDecimal(total)
    );
  }
}
