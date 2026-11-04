package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import com.isil.edu.pe.spedidos.service.domain.ProductoVendido;
import com.isil.edu.pe.spedidos.service.domain.ResumenPedidos;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PedidoReporteServiceImpl implements PedidoReporteService {

  private final PedidoReporteJdbcRepository jdbcRepository;

  public PedidoReporteServiceImpl(PedidoReporteJdbcRepository jdbcRepository) {
      this.jdbcRepository = jdbcRepository;
  }

  @Override
  public ResumenPedidos obtenerResumen() {
    ResumenPedidosProjection resumenPedidos = jdbcRepository.obtenerResumen();
    return new ResumenPedidos(resumenPedidos.totalPedidos(), resumenPedidos.totalUnidades(),
        resumenPedidos.importeAcumulado(), resumenPedidos.ticketPromedio());
  }

  @Override
  public List<Pedido> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }

    return jdbcRepository.buscarPorCliente(cliente);
  }

  @Override
  public List<ProductoVendido> listarProductosMasVendidos() {
    return jdbcRepository.listarProductosMasVendidos()
        .stream()
        .map(item ->
            new ProductoVendido(
                item.productoCodigo(),
                item.productoNombre(),
                item.totalPedidos(),
                item.totalUnidades(),
                item.importeAcumulado()
            )
        )
        .toList();

  }

  @Override
  public List<Pedido> buscarPedidosPorClienteYTotalMinimo(String cliente, BigDecimal totalMinimo) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio para la búsqueda avanzada.");
    }
    if (totalMinimo == null || totalMinimo.signum() < 0) {
      throw new PedidoException("El total mínimo es obligatorio y debe ser mayor o igual a 0.");
    }

    return jdbcRepository.buscarPedidosPorClienteYTotalMinimo(cliente.trim(), totalMinimo);
  }

  @Override
  public List<ClienteFrecuente> listarClientesFrecuentes(Integer minPedidos) {
    if (minPedidos == null || minPedidos < 1) {
      throw new PedidoException("minPedidos es obligatorio y debe ser un entero mayor o igual a 1.");
    }

    return jdbcRepository.listarClientesFrecuentes(minPedidos)
        .stream()
        .map(item -> new ClienteFrecuente(
            item.cliente(),
            item.totalPedidos(),
            item.totalUnidades(),
            item.importeAcumulado(),
            item.ticketPromedio()
        ))
        .toList();
  }
}
