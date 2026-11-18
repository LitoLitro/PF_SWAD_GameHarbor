package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.CatalogoDtos.ResenaDto;
import com.gameharbor.model.dto.TiendaDtos.ResenaRequest;
import com.gameharbor.model.entity.Resena;
import com.gameharbor.repository.BibliotecaRepository;
import com.gameharbor.repository.JuegoRepository;
import com.gameharbor.repository.ResenaRepository;
import com.gameharbor.repository.UsuarioRepository;
import com.gameharbor.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResenaService {

    private final ResenaRepository resenas;
    private final BibliotecaRepository biblioteca;
    private final JuegoRepository juegos;
    private final UsuarioRepository usuarios;

    public ResenaService(ResenaRepository resenas, BibliotecaRepository biblioteca,
                         JuegoRepository juegos, UsuarioRepository usuarios) {
        this.resenas = resenas;
        this.biblioteca = biblioteca;
        this.juegos = juegos;
        this.usuarios = usuarios;
    }

    /** Equivale a SP_AGREGAR_RESENA: solo quien posee el juego puede reseñarlo. Si ya reseñó, se actualiza. */
    @Transactional
    public ResenaDto agregarOActualizar(Integer usuarioId, Integer juegoId, ResenaRequest req) {
        if (!juegos.existsById(juegoId)) throw ApiException.notFound("Juego no encontrado");
        if (!biblioteca.existsByUsuarioIdAndJuegoId(usuarioId, juegoId)) {
            throw ApiException.forbidden("Solo puedes reseñar juegos que tienes en tu biblioteca");
        }
        Resena r = resenas.findByUsuarioIdAndJuegoId(usuarioId, juegoId).orElseGet(() -> {
            Resena nueva = new Resena();
            nueva.setUsuario(usuarios.getReferenceById(usuarioId));
            nueva.setJuego(juegos.getReferenceById(juegoId));
            return nueva;
        });
        r.setCalificacion(req.calificacion());
        r.setComentario(req.comentario());
        resenas.save(r);
        return Mapper.resena(r);
    }
}
