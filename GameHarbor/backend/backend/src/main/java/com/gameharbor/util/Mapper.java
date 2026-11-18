package com.gameharbor.util;

import com.gameharbor.model.dto.AuthDtos.UsuarioDto;
import com.gameharbor.model.dto.CatalogoDtos.*;
import com.gameharbor.model.dto.CloudDtos.SesionDto;
import com.gameharbor.model.dto.TiendaDtos.*;
import com.gameharbor.model.entity.*;

import java.util.Comparator;
import java.util.List;

/** Conversiones entidad -> DTO. Debe llamarse dentro de una transaccion (hay relaciones LAZY). */
public final class Mapper {

    private Mapper() {}

    public static UsuarioDto usuario(Usuario u) {
        List<String> roles = u.getRoles().stream().map(Rol::getNombre).sorted().toList();
        return new UsuarioDto(u.getId(), u.getNombre(), u.getEmail(), u.getSaldo(), roles);
    }

    public static CategoriaDto categoria(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre(), c.getDescripcion());
    }

    public static MetodoPagoDto metodoPago(MetodoPago m) {
        return new MetodoPagoDto(m.getId(), m.getNombre());
    }

    private static List<String> nombresCategorias(Juego j) {
        return j.getCategorias().stream().map(Categoria::getNombre).sorted(Comparator.naturalOrder()).toList();
    }

    public static JuegoResumenDto juegoResumen(Juego j) {
        return new JuegoResumenDto(j.getId(), j.getTitulo(), j.getPrecio(), j.getImagenUrl(),
                j.isRequiereCloud(), j.getDesarrollador().getNombre(), nombresCategorias(j));
    }

    public static JuegoDetalleDto juegoDetalle(Juego j, Double promedio, long totalResenas) {
        Double redondeado = promedio == null ? null : Math.round(promedio * 10.0) / 10.0;
        return new JuegoDetalleDto(j.getId(), j.getTitulo(), j.getDescripcion(), j.getPrecio(),
                j.getFechaLanzamiento(), j.getImagenUrl(), j.isRequiereCloud(), j.getEstado(),
                j.getDesarrollador().getNombre(), nombresCategorias(j), redondeado, totalResenas);
    }

    public static ResenaDto resena(Resena r) {
        return new ResenaDto(r.getId(), r.getUsuario().getNombre(), r.getCalificacion(), r.getComentario(), r.getFecha());
    }

    public static OrdenDto orden(Orden o) {
        List<OrdenDetalleDto> detalles = o.getDetalles().stream()
                .map(d -> new OrdenDetalleDto(d.getJuego().getId(), d.getJuego().getTitulo(),
                        d.getPrecioUnitario(), d.getCantidad()))
                .toList();
        return new OrdenDto(o.getId(), o.getFecha(), o.getTotal(), o.getEstado(), o.getMetodoPago().getNombre(), detalles);
    }

    public static BibliotecaItemDto biblioteca(Biblioteca b) {
        Juego j = b.getJuego();
        return new BibliotecaItemDto(j.getId(), j.getTitulo(), j.getImagenUrl(), j.isRequiereCloud(), b.getFechaAdquisicion());
    }

    public static SesionDto sesion(SesionJuego s) {
        return new SesionDto(s.getId(), s.getJuego().getId(), s.getJuego().getTitulo(),
                s.getServidor().getNombre(), s.getServidor().getUbicacion(), s.getEstado(),
                s.getFechaInicio(), s.getFechaFin(), s.getBitrateMbps(), s.getLatenciaMs());
    }
}
