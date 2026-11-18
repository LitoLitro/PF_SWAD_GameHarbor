package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.TiendaDtos.BibliotecaItemDto;
import com.gameharbor.model.dto.TiendaDtos.OrdenDto;
import com.gameharbor.model.entity.*;
import com.gameharbor.repository.*;
import com.gameharbor.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrdenService {

    private final CarritoService carritoService;
    private final CarritoItemRepository items;
    private final OrdenRepository ordenes;
    private final BibliotecaRepository biblioteca;
    private final MetodoPagoRepository metodosPago;
    private final UsuarioRepository usuarios;

    public OrdenService(CarritoService carritoService, CarritoItemRepository items, OrdenRepository ordenes,
                        BibliotecaRepository biblioteca, MetodoPagoRepository metodosPago, UsuarioRepository usuarios) {
        this.carritoService = carritoService;
        this.items = items;
        this.ordenes = ordenes;
        this.biblioteca = biblioteca;
        this.metodosPago = metodosPago;
        this.usuarios = usuarios;
    }

    /**
     * Equivale a SP_PROCESAR_COMPRA: crea la orden (PAGADA), su detalle, agrega los juegos a la
     * biblioteca sin duplicar y vacia el carrito. El pago es simulado (no se descuenta saldo).
     */
    @Transactional
    public OrdenDto comprar(Integer usuarioId, Integer metodoPagoId) {
        MetodoPago metodo = metodosPago.findById(metodoPagoId)
                .filter(MetodoPago::isActivo)
                .orElseThrow(() -> ApiException.badRequest("Método de pago inválido o inactivo"));

        Carrito carrito = carritoService.carritoDe(usuarioId);
        List<CarritoItem> lineas = items.findByCarritoId(carrito.getId());
        if (lineas.isEmpty()) {
            throw ApiException.badRequest("El carrito está vacío");
        }

        Orden orden = new Orden();
        orden.setUsuario(usuarios.getReferenceById(usuarioId));
        orden.setMetodoPago(metodo);
        orden.setEstado("PAGADA");

        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem linea : lineas) {
            Juego juego = linea.getJuego();
            if (!"DISPONIBLE".equals(juego.getEstado())) {
                throw ApiException.conflict("El juego \"" + juego.getTitulo() + "\" ya no está disponible");
            }
            OrdenDetalle detalle = new OrdenDetalle();
            detalle.setJuego(juego);
            detalle.setPrecioUnitario(juego.getPrecio());      // precio congelado al momento de la compra
            detalle.setCantidad(linea.getCantidad());
            orden.agregarDetalle(detalle);
            total = total.add(juego.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidad())));

            if (!biblioteca.existsByUsuarioIdAndJuegoId(usuarioId, juego.getId())) {
                Biblioteca b = new Biblioteca();
                b.setUsuario(usuarios.getReferenceById(usuarioId));
                b.setJuego(juego);
                biblioteca.save(b);
            }
        }
        orden.setTotal(total);
        ordenes.save(orden);

        items.deleteByCarritoId(carrito.getId());
        return Mapper.orden(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenDto> historial(Integer usuarioId) {
        return ordenes.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(Mapper::orden).toList();
    }

    @Transactional(readOnly = true)
    public List<BibliotecaItemDto> biblioteca(Integer usuarioId) {
        return biblioteca.findByUsuario(usuarioId).stream().map(Mapper::biblioteca).toList();
    }
}
