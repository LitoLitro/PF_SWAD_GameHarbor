package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.CatalogoDtos.*;
import com.gameharbor.model.entity.Juego;
import com.gameharbor.repository.*;
import com.gameharbor.util.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private static final int TAMANO_MAXIMO = 50;

    private final JuegoRepository juegos;
    private final CategoriaRepository categorias;
    private final MetodoPagoRepository metodosPago;
    private final ResenaRepository resenas;

    public CatalogoService(JuegoRepository juegos, CategoriaRepository categorias,
                           MetodoPagoRepository metodosPago, ResenaRepository resenas) {
        this.juegos = juegos;
        this.categorias = categorias;
        this.metodosPago = metodosPago;
        this.resenas = resenas;
    }

    public PageDto<JuegoResumenDto> buscar(String texto, Integer categoriaId, int pagina, int tamano) {
        int size = Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
        PageRequest pageable = PageRequest.of(Math.max(0, pagina), size, Sort.by("titulo"));
        Page<Juego> page = juegos.findAll(JuegoRepository.catalogo(texto, categoriaId), pageable);
        List<JuegoResumenDto> contenido = page.getContent().stream().map(Mapper::juegoResumen).toList();
        return new PageDto<>(contenido, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public JuegoDetalleDto detalle(Integer id) {
        Juego j = juegos.findById(id).orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        return Mapper.juegoDetalle(j, resenas.promedioPorJuego(id), resenas.countByJuegoId(id));
    }

    public List<ResenaDto> resenas(Integer juegoId) {
        if (!juegos.existsById(juegoId)) throw ApiException.notFound("Juego no encontrado");
        return resenas.findByJuego(juegoId).stream().map(Mapper::resena).toList();
    }

    public List<CategoriaDto> categorias() {
        return categorias.findAll(Sort.by("nombre")).stream().map(Mapper::categoria).toList();
    }

    public List<MetodoPagoDto> metodosPago() {
        return metodosPago.findByActivoTrue().stream().map(Mapper::metodoPago).toList();
    }
}
