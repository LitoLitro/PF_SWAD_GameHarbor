package com.gameharbor.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "JUEGO")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JUEGO_ID")
    private Integer id;

    @Column(name = "JUEGO_TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "JUEGO_DESCRIPCION", length = 1000)
    private String descripcion;

    @Column(name = "JUEGO_PRECIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "JUEGO_FECHA_LANZAMIENTO")
    private LocalDate fechaLanzamiento;

    @Column(name = "JUEGO_IMAGEN_URL", length = 300)
    private String imagenUrl;

    @Column(name = "JUEGO_REQUIERE_CLOUD", nullable = false)
    private boolean requiereCloud = true;

    @Column(name = "JUEGO_ESTADO", nullable = false, length = 20)
    private String estado = "DISPONIBLE";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DESARROLLADOR_ID")
    private Desarrollador desarrollador;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "JUEGO_CATEGORIA",
            joinColumns = @JoinColumn(name = "JUEGO_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORIA_ID"))
    private Set<Categoria> categorias = new HashSet<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public LocalDate getFechaLanzamiento() { return fechaLanzamiento; }
    public void setFechaLanzamiento(LocalDate fechaLanzamiento) { this.fechaLanzamiento = fechaLanzamiento; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public boolean isRequiereCloud() { return requiereCloud; }
    public void setRequiereCloud(boolean requiereCloud) { this.requiereCloud = requiereCloud; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Desarrollador getDesarrollador() { return desarrollador; }
    public void setDesarrollador(Desarrollador desarrollador) { this.desarrollador = desarrollador; }
    public Set<Categoria> getCategorias() { return categorias; }
    public void setCategorias(Set<Categoria> categorias) { this.categorias = categorias; }
}
