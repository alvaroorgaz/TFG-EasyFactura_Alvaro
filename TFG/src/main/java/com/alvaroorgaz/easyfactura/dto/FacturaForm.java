package com.alvaroorgaz.easyfactura.dto;

import com.alvaroorgaz.easyfactura.model.EstadoFactura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class FacturaForm {

    private Integer idFactura;
    private Integer facturaOriginalId;
    private Long empresaId;

    @NotNull(message = "Debes seleccionar un cliente.")
    private Integer clienteId;

    private EstadoFactura estado;
    private String hashVerifactu;

    @Size(max = 500, message = "El motivo de rectificacion no puede superar los 500 caracteres.")
    private String motivoRectificacion;

    @Valid
    @NotEmpty(message = "Debes anadir al menos una linea de factura.")
    private List<FacturaDetalleForm> detalles = new ArrayList<>();

    public Integer getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }

    public Integer getFacturaOriginalId() {
        return facturaOriginalId;
    }

    public void setFacturaOriginalId(Integer facturaOriginalId) {
        this.facturaOriginalId = facturaOriginalId;
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

    public String getMotivoRectificacion() {
        return motivoRectificacion;
    }

    public void setMotivoRectificacion(String motivoRectificacion) {
        this.motivoRectificacion = motivoRectificacion;
    }

    public List<FacturaDetalleForm> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<FacturaDetalleForm> detalles) {
        this.detalles = detalles;
    }
}
