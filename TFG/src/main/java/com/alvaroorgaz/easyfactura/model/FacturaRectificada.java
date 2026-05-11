package com.alvaroorgaz.easyfactura.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "factura_rectificada")
public class FacturaRectificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rectificacion")
    private Integer idRectificacion;

    @ManyToOne
    @JoinColumn(name = "id_factura_original", nullable = false)
    private Factura facturaOriginal;

    @ManyToOne
    @JoinColumn(name = "id_factura_rectificada", nullable = false)
    private Factura facturaRectificada;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    private LocalDateTime fecha;

    public Integer getIdRectificacion() {
        return idRectificacion;
    }

    public void setIdRectificacion(Integer idRectificacion) {
        this.idRectificacion = idRectificacion;
    }

    public Factura getFacturaOriginal() {
        return facturaOriginal;
    }

    public void setFacturaOriginal(Factura facturaOriginal) {
        this.facturaOriginal = facturaOriginal;
    }

    public Factura getFacturaRectificada() {
        return facturaRectificada;
    }

    public void setFacturaRectificada(Factura facturaRectificada) {
        this.facturaRectificada = facturaRectificada;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
