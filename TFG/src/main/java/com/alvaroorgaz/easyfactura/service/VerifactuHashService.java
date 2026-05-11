package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Factura;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;

@Service
public class VerifactuHashService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String generarHashFactura(Factura factura,
                                     BigDecimal totalFactura,
                                     String hashAnterior) {
        try {
            String cadenaCanonica = String.join("|",
                    valorSeguro(factura.getEmpresa().getCif()),
                    valorSeguro(factura.getIdFactura()),
                    factura.getFecha() != null ? factura.getFecha().format(FORMATTER) : "",
                    valorSeguro(factura.getCliente().getNif()),
                    totalFactura.setScale(2).toPlainString(),
                    factura.getEstado() != null ? factura.getEstado().name() : "",
                    hashAnterior != null ? hashAnterior : ""
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadenaCanonica.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el hash VeriFactu.", e);
        }
    }

    private String valorSeguro(Object valor) {
        return valor != null ? valor.toString() : "";
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
