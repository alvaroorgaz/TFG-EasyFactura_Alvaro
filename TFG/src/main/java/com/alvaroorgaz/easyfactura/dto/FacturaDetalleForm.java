package com.alvaroorgaz.easyfactura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FacturaDetalleForm {

    @NotNull(message = "Debes seleccionar un producto.")
    private Long productoId;

    @NotNull(message = "Debes indicar una cantidad.")
    @Min(value = 1, message = "La cantidad debe ser mayor que 0.")
    private Integer cantidad;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
