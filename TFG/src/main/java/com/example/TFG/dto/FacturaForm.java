package com.example.TFG.dto;

import com.example.TFG.model.EstadoFactura;

import java.util.ArrayList;
import java.util.List;

public class FacturaForm {

    private Integer idFactura;
    private Long empresaId;
    private Integer clienteId;
    private EstadoFactura estado;
    private String hashVerifactu;
    private List<FacturaDetalleForm> detalles = new ArrayList<>();

    public Integer getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public String getHashVerifactu() {
        return hashVerifactu;
    }

    public void setHashVerifactu(String hashVerifactu) {
        this.hashVerifactu = hashVerifactu;
    }

    public List<FacturaDetalleForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalleForm> detalles) {
        this.detalles = detalles;
    }
}
