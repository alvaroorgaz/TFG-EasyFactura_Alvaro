package com.alvaroorgaz.easyfactura.dto;

import java.math.BigDecimal;

public record ResumenFactura(BigDecimal baseImponible, BigDecimal totalIva, BigDecimal totalFactura) {
}
