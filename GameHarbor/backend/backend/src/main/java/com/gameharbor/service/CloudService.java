package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.CloudDtos.FinalizarSesionRequest;
import com.gameharbor.model.dto.CloudDtos.SesionDto;
import com.gameharbor.model.entity.Juego;
import com.gameharbor.model.entity.ServidorCloud;
import com.gameharbor.model.entity.SesionJuego;
import com.gameharbor.repository.*;
import com.gameharbor.util.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Cloud gaming SIMULADO: no se transmite video, solo se gestionan sesiones y servidores. */
@Service
public class CloudService {

    private static final String ACTIVA = "ACTIVA";

    private final SesionJuegoRepository sesiones;
    private final ServidorCloudRepository servidores;
    private final BibliotecaRepository biblioteca;
    private final JuegoRepository juegos;
    private final UsuarioRepository usuarios;

    public CloudService(SesionJuegoRepository sesiones, ServidorCloudRepository servidores,
                        BibliotecaRepository biblioteca, JuegoRepository juegos, UsuarioRepository usuarios) {
        this.sesiones = sesiones;
        this.servidores = servidores;
        this.biblioteca = biblioteca;
        this.juegos = juegos;
        this.usuarios = usuarios;
    }

    /**
     * Equivale a SP_INICIAR_SESION_CLOUD: valida que el usuario posea el juego y lo asigna al
     * servidor ACTIVO con menos sesiones (y con cupo). Si ya tenia una sesion activa de ese
     * juego, la devuelve en lugar de abrir otra.
     */
    @Transactional
    public SesionDto iniciar(Integer usuarioId, Integer juegoId) {
        if (!biblioteca.existsByUsuarioIdAndJuegoId(usuarioId, juegoId)) {
            throw ApiException.forbidden("No tienes este juego en tu biblioteca");
        }
        Juego juego = juegos.findById(juegoId).orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        if (!"DISPONIBLE".equals(juego.getEstado())) {
            throw ApiException.conflict("El juego no está disponible para jugar");
        }

        Optional<SesionJuego> existente = sesiones.findByUsuarioIdAndJuegoIdAndEstado(usuarioId, juegoId, ACTIVA);
        if (existente.isPresent()) {
            return Mapper.sesion(existente.get());
        }

        ServidorCloud servidor = servidores.findByEstado("ACTIVO").stream()
                .filter(s -> carga(s) < s.getCapacidad())
                .min(Comparator.comparingLong(this::carga))
                .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "No hay servidores disponibles en este momento"));

        SesionJuego sesion = new SesionJuego();
        sesion.setUsuario(usuarios.getReferenceById(usuarioId));
        sesion.setJuego(juego);
        sesion.setServidor(servidor);
        sesion.setEstado(ACTIVA);
        sesiones.save(sesion);
        return Mapper.sesion(sesion);
    }

    /** Equivale a SP_FINALIZAR_SESION_CLOUD. Si no llegan metricas, se simulan valores plausibles. */
    @Transactional
    public SesionDto finalizar(Integer usuarioId, Integer sesionId, FinalizarSesionRequest req) {
        SesionJuego s = sesiones.findByIdAndUsuarioId(sesionId, usuarioId)
                .orElseThrow(() -> ApiException.notFound("Sesión no encontrada"));
        if (!ACTIVA.equals(s.getEstado())) {
            throw ApiException.conflict("La sesión ya fue finalizada");
        }
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        BigDecimal bitrate = (req != null && req.bitrateMbps() != null)
                ? req.bitrateMbps()
                : BigDecimal.valueOf(rnd.nextDouble(15.0, 45.0)).setScale(2, RoundingMode.HALF_UP);
        Integer latencia = (req != null && req.latenciaMs() != null) ? req.latenciaMs() : rnd.nextInt(12, 71);

        s.setEstado("FINALIZADA");
        s.setFechaFin(LocalDateTime.now());
        s.setBitrateMbps(bitrate);
        s.setLatenciaMs(latencia);
        return Mapper.sesion(s);
    }

    @Transactional(readOnly = true)
    public List<SesionDto> activas(Integer usuarioId) {
        return sesiones.findByUsuarioAndEstado(usuarioId, ACTIVA).stream().map(Mapper::sesion).toList();
    }

    private long carga(ServidorCloud s) {
        return sesiones.countByServidorIdAndEstado(s.getId(), ACTIVA);
    }
}
