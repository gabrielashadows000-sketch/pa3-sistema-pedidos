package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import com.isil.edu.pe.spedidos.service.domain.ProductoVendido;
import com.isil.edu.pe.spedidos.service.domain.ResumenPedidos;
import java.math.BigDecimal;
import java.util.List;

public interface PedidoReporteService {

  ResumenPedidos obtenerResumen();

  List<Pedido> buscarPorCliente(String cliente);

  List<ProductoVendido> listarProductosMasVendidos();

  /**
   * PA3: pedidos cuyo cliente coincide y cuyo total es >= totalMinimo.
   *
   * @throws com.isil.edu.pe.spedidos.exception.PedidoException si cliente está vacío
   *     o totalMinimo es nulo o negativo
   */
  List<Pedido> buscarPedidosPorClienteYTotalMinimo(String cliente, BigDecimal totalMinimo);

  /**
   * PA3: clientes con al menos minPedidos pedidos y sus consolidados.
   *
   * @throws com.isil.edu.pe.spedidos.exception.PedidoException si minPedidos es nulo o menor que 1
   */
  List<ClienteFrecuente> listarClientesFrecuentes(Integer minPedidos);

}
