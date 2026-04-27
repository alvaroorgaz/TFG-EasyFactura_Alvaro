package com.example.TFG.service;

import com.example.TFG.model.EstadoFactura;
import com.example.TFG.model.Factura;
import com.example.TFG.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public Factura guardarFactura(Factura factura) {
        if (factura.getIdFactura() == null) {
            if (factura.getFecha() == null) {
                factura.setFecha(LocalDateTime.now());
            }
            if (factura.getEstado() == null) {
                factura.setEstado(EstadoFactura.activa);
            }
        } else {
            Factura facturaExistente = facturaRepository.findById(factura.getIdFactura()).orElse(null);

            if (facturaExistente != null && factura.getFecha() == null) {
                factura.setFecha(facturaExistente.getFecha());
            }
        }

        return facturaRepository.save(factura);
    }

    public List<Factura> obtenerFacturas() {
        return facturaRepository.findAll();
    }

    public List<Factura> obtenerFacturasPorEmpresa(Long idEmpresa) {
        return facturaRepository.findFacturasByEmpresaId(idEmpresa);
    }

    public Factura obtenerFacturaPorId(Long id) {
        return facturaRepository.findById(id).orElse(null);
    }

    public void eliminarFactura(Long id) {
        facturaRepository.deleteById(id);
    }
}
