package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.TiendaDtos.CarritoDto;
import com.gameharbor.model.dto.TiendaDtos.CarritoItemDto;
import com.gameharbor.model.entity.Carrito;
import com.gameharbor.model.entity.CarritoItem;
import com.gameharbor.model.entity.Juego;
import com.gameharbor.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritos;
    private final CarritoItemRepository items;
    private final JuegoRepository juegos;
    private final BibliotecaRepository biblioteca;
    private final UsuarioRepository usuarios;

    public CarritoService(CarritoRepository carritos, CarritoItemRepository items, JuegoRepository juegos,
                          BibliotecaRepository biblioteca, UsuarioRepository usuarios) {
        this.carritos = carritos;
        this.items = items;
        this.juegos = juegos;
        this.biblioteca = biblioteca;
        this.usuarios = usuarios;
    }

    @Transactional
    public CarritoDto obtener(Integer usuarioId) {
        return armar(carritoDe(usuarioId));
    }

    /** Juegos digitales: cantidad siempre 1. Agregar dos veces el mismo juego no duplica el item. */
    @Transactional
    public CarritoDto agregar(Integer usuarioId, Integer juegoId) {
        Juego juego = juegos.findById(juegoId).orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        if (!"DISPONIBLE".equals(juego.getEstado())) {
            throw ApiException.conflict("El juego no está disponible para la venta");
        }
        if (biblioteca.existsByUsuarioIdAndJuegoId(usuarioId, juegoId)) {
            throw ApiException.conflict("Ya tienes este juego en tu biblioteca");
        }
        Carrito carrito = carritoDe(usuarioId);
        if (items.findByCarritoIdAndJuegoId(carrito.getId(), juegoId).isEmpty()) {
            CarritoItem item = new CarritoItem();
            item.setCarrito(carrito);
            item.setJuego(juego);
            item.setCantidad(1);
            items.save(item);
        }
        return armar(carrito);
    }

    @Transactional
    public CarritoDto quitar(Integer usuarioId, Integer juegoId) {
        Carrito carrito = carritoDe(usuarioId);
        items.findByCarritoIdAndJuegoId(carrito.getId(), juegoId).ifPresent(items::delete);
        items.flush();
        return armar(carrito);
    }

    @Transactional
    public CarritoDto vaciar(Integer usuarioId) {
        Carrito carrito = carritoDe(usuarioId);
        items.deleteByCarritoId(carrito.getId());
        return armar(carrito);
    }

    /** Devuelve el carrito del usuario; lo crea si no existe (p. ej. usuarios cargados por script). */
    Carrito carritoDe(Integer usuarioId) {
        return carritos.findByUsuarioId(usuarioId).orElseGet(() -> {
            Carrito c = new Carrito();
            c.setUsuario(usuarios.getReferenceById(usuarioId));
            return carritos.save(c);
        });
    }

    private CarritoDto armar(Carrito carrito) {
        List<CarritoItemDto> lista = items.findByCarritoId(carrito.getId()).stream()
                .map(ci -> new CarritoItemDto(ci.getJuego().getId(), ci.getJuego().getTitulo(),
                        ci.getJuego().getImagenUrl(), ci.getJuego().getPrecio(), ci.getCantidad()))
                .toList();
        BigDecimal total = lista.stream()
                .map(i -> i.precio().multiply(BigDecimal.valueOf(i.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoDto(lista, total);
    }
}
