package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class PedidoReporteServiceImplPa3Test {

  private PedidoReporteJdbcRepository repository;

  private PedidoReporteServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(PedidoReporteJdbcRepository.class);
    service = new PedidoReporteServiceImpl(repository);
  }

  @Test
  @DisplayName("Reject listarClientesFrecuentes in PedidoReporteServiceImpl when minPedidos is lower than 1")
  void rejectListarClientesFrecuentesInPedidoReporteServiceImplWhenMinPedidosIsLowerThanOne() {
    // Arrange / Act / Assert
    assertThrows(PedidoException.class, () -> service.listarClientesFrecuentes(0));
    assertThrows(PedidoException.class, () -> service.listarClientesFrecuentes(null));
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Reject buscarPedidosPorClienteYTotalMinimo in PedidoReporteServiceImpl when totalMinimo is negative")
  void rejectBuscarPedidosPorClienteYTotalMinimoInPedidoReporteServiceImplWhenTotalMinimoIsNegative() {
    // Arrange
    BigDecimal negativo = new BigDecimal("-0.01");

    // Act
    PedidoException e = assertThrows(PedidoException.class,
        () -> service.buscarPedidosPorClienteYTotalMinimo("Ana", negativo));

    // Assert
    assertEquals("El total mínimo es obligatorio y debe ser mayor o igual a 0.", e.getMessage());
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Reject buscarPedidosPorClienteYTotalMinimo in PedidoReporteServiceImpl when cliente is blank")
  void rejectBuscarPedidosPorClienteYTotalMinimoInPedidoReporteServiceImplWhenClienteIsBlank() {
    // Arrange / Act / Assert
    assertThrows(PedidoException.class,
        () -> service.buscarPedidosPorClienteYTotalMinimo("   ", BigDecimal.TEN));
    assertThrows(PedidoException.class,
        () -> service.buscarPedidosPorClienteYTotalMinimo(null, BigDecimal.TEN));
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Delegate trimmed cliente to repository from buscarPedidosPorClienteYTotalMinimo when parameters are valid")
  void delegateTrimmedClienteToRepositoryFromBuscarPedidosPorClienteYTotalMinimoWhenParametersAreValid() {
    // Arrange
    when(repository.buscarPedidosPorClienteYTotalMinimo("Ana", BigDecimal.ZERO))
        .thenReturn(List.of());

    // Act
    var resultado = service.buscarPedidosPorClienteYTotalMinimo("  Ana  ", BigDecimal.ZERO);

    // Assert
    assertEquals(0, resultado.size());
    verify(repository).buscarPedidosPorClienteYTotalMinimo("Ana", BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Map projections to domain objects from listarClientesFrecuentes when minPedidos is valid")
  void mapProjectionsToDomainObjectsFromListarClientesFrecuentesWhenMinPedidosIsValid() {
    // Arrange
    when(repository.listarClientesFrecuentes(anyInt())).thenReturn(List.of(
        new ClienteFrecuenteProjection("Ana Torres", 2, 4,
            new BigDecimal("5100.00"), new BigDecimal("2550.00"))));

    // Act
    List<ClienteFrecuente> clientes = service.listarClientesFrecuentes(2);

    // Assert
    assertEquals(1, clientes.size());
    assertEquals("Ana Torres", clientes.get(0).cliente());
    assertEquals(2L, clientes.get(0).totalPedidos());
    assertEquals(0, new BigDecimal("2550.00").compareTo(clientes.get(0).ticketPromedio()));
    verify(repository).listarClientesFrecuentes(2);
  }
}
