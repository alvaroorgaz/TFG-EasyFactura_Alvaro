package com.example.TFG.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFactura;

    @ManyToOne
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;

    @Column(nullable = false)
    private String hashVerifactu;

    @OneToMany(mappedBy = "factura")
    private List<FacturaDetalle> detalles;

    // Getters y setters
}