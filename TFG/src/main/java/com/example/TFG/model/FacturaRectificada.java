package com.example.TFG.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura_rectificada")
public class FacturaRectificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRectificacion;

    @ManyToOne
    @JoinColumn(name = "id_factura_original", nullable = false)
    private Factura facturaOriginal;

    @ManyToOne
    @JoinColumn(name = "id_factura_rectificada", nullable = false)
    private Factura facturaRectificada;

    private String motivo;

    private LocalDateTime fecha;

    // Getters y setters
}
