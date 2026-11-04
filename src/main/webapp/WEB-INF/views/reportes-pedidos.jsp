<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reportes de pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
    <style>
        .form-reporte { display: grid; grid-template-columns: 2fr 1fr auto; gap: 16px; align-items: end; }
        .form-reporte.una { grid-template-columns: 1fr auto; max-width: 480px; }
        .kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
        .kpi { background: #f4f6f8; border-radius: 8px; padding: 14px; }
        .kpi strong { display: block; font-size: 1.4rem; }
        @media (max-width: 820px) { .form-reporte, .kpis { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">PA3 · Spring MVC · Spring Security · Spring JDBC</p>
            <h1>Reportes de pedidos</h1>
            <p class="subtitulo">
                Consulta y supervisión sobre la base de datos local. No depende de inventario-service.
            </p>
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}">Ir a registro de pedidos</a>
        </div>
        <c:url var="logoutUrl" value="/logout"/>
        <form method="post" action="${logoutUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit">Cerrar sesión</button>
        </form>
        <div class="flujo">
            Security → DispatcherServlet → Controller → Service → PedidoReporteJdbcRepository → MySQL
        </div>
    </header>

    <section class="tarjeta">
        <h2>Resumen general</h2>
        <div class="kpis">
            <div class="kpi">Pedidos<strong><c:out value="${resumen.totalPedidos}"/></strong></div>
            <div class="kpi">Unidades<strong><c:out value="${resumen.totalUnidades}"/></strong></div>
            <div class="kpi">Importe acumulado
                <strong>S/ <fmt:formatNumber value="${resumen.importeAcumulado}"
                                             minFractionDigits="2" maxFractionDigits="2"/></strong></div>
            <div class="kpi">Ticket promedio
                <strong>S/ <fmt:formatNumber value="${resumen.ticketPromedio}"
                                             minFractionDigits="2" maxFractionDigits="2"/></strong></div>
        </div>
    </section>

    <section class="tarjeta">
        <h2>Búsqueda avanzada</h2>
        <c:if test="${not empty errorBusqueda}">
            <div class="alerta error"><c:out value="${errorBusqueda}"/></div>
        </c:if>
        <c:url var="reporteUrl" value="/reportes/pedidos"/>
        <form method="get" action="${reporteUrl}" class="form-reporte">
            <label>
                Cliente
                <input name="cliente" maxlength="120" placeholder="Ej. Ana"
                       value="${fn:escapeXml(clienteIngresado)}">
            </label>
            <label>
                Total mínimo (S/)
                <input name="totalMinimo" type="number" step="0.01"
                       value="${fn:escapeXml(totalMinimoIngresado)}">
            </label>
            <button type="submit">Buscar</button>
        </form>

        <c:if test="${busquedaRealizada}">
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th><th>Cliente</th><th>Producto</th>
                        <th>Cantidad</th><th>Total</th><th>Fecha</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${pedidosBusqueda}">
                        <tr>
                            <td><c:out value="${pedido.id}"/></td>
                            <td><c:out value="${pedido.cliente}"/></td>
                            <td><c:out value="${pedido.productoNombre}"/></td>
                            <td><c:out value="${pedido.cantidad}"/></td>
                            <td>S/ <fmt:formatNumber value="${pedido.total}"
                                                     minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td><c:out value="${pedido.fecha}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pedidosBusqueda}">
                        <tr><td colspan="6" class="sin-datos">Sin resultados para los filtros indicados.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
    </section>

    <section class="tarjeta">
        <h2>Clientes frecuentes</h2>
        <c:if test="${not empty errorFrecuentes}">
            <div class="alerta error"><c:out value="${errorFrecuentes}"/></div>
        </c:if>
        <form method="get" action="${reporteUrl}" class="form-reporte una">
            <label>
                Mínimo de pedidos
                <input name="minPedidos" type="number" step="1"
                       value="${fn:escapeXml(minPedidosIngresado)}">
            </label>
            <button type="submit">Consultar</button>
        </form>

        <c:if test="${frecuentesRealizada}">
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr>
                        <th>Cliente</th><th>Pedidos</th><th>Unidades</th>
                        <th>Importe acumulado</th><th>Ticket promedio</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="cf" items="${clientesFrecuentes}">
                        <tr>
                            <td><c:out value="${cf.cliente}"/></td>
                            <td><c:out value="${cf.totalPedidos}"/></td>
                            <td><c:out value="${cf.totalUnidades}"/></td>
                            <td>S/ <fmt:formatNumber value="${cf.importeAcumulado}"
                                                     minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td>S/ <fmt:formatNumber value="${cf.ticketPromedio}"
                                                     minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty clientesFrecuentes}">
                        <tr><td colspan="5" class="sin-datos">Ningún cliente alcanza ese mínimo de pedidos.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
    </section>
</div>
</body>
</html>
