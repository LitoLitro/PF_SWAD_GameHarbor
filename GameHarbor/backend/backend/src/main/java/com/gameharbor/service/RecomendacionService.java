package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.TiendaDtos.HistorialRequest;
import com.gameharbor.model.dto.TiendaDtos.RecomendacionDto;
import com.gameharbor.model.entity.HistorialVista;
import com.gameharbor.repository.HistorialVistaRepository;
import com.gameharbor.repository.JuegoRepository;
import com.gameharbor.repository.RecomendacionRepository;
import com.gameharbor.repository.UsuarioRepository;
import com.gameharbor.util.Mapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Por ahora solo LEE las recomendaciones que deja el modelo de ML en la tabla RECOMENDACION y
 * registra el historial que ese modelo usa como entrada. La llamada al servicio Python
 * (arbol de decision) se agrega despues.
 */
@Service
public class RecomendacionService {

    private final RecomendacionRepository recomendaciones;
    private final HistorialVistaRepository historial;
    private final JuegoRepository juegos;
    private final UsuarioRepository usuarios;

    public RecomendacionService(RecomendacionRepository recomendaciones, HistorialVistaRepository historial,
                                JuegoRepository juegos, UsuarioRepository usuarios) {
        this.recomendaciones = recomendaciones;
        this.historial = historial;
        this.juegos = juegos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<RecomendacionDto> para(Integer usuarioId, int limite) {
        int n = Math.max(1, Math.min(limite, 30));
        return recomendaciones.topParaUsuario(usuarioId, PageRequest.of(0, n)).stream()
                .map(r -> new RecomendacionDto(Mapper.juegoResumen(r.getJuego()), r.getPuntaje(), r.getOrigen()))
                .toList();
    }

    @Transactional
    public void registrarVista(Integer usuarioId, HistorialRequest req) {
        if (!juegos.existsById(req.juegoId())) throw ApiException.notFound("Juego no encontrado");
        HistorialVista h = new HistorialVista();
        h.setUsuario(usuarios.getReferenceById(usuarioId));
        h.setJuego(juegos.getReferenceById(req.juegoId()));
        h.setSegundos(req.segundos());
        h.setOrigen(req.origen());
        historial.save(h);
    }
}
