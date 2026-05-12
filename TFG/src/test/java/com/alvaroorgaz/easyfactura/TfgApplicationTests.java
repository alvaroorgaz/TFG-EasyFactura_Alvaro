package com.alvaroorgaz.easyfactura;

import com.alvaroorgaz.easyfactura.model.Cliente;
import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.EstadoFactura;
import com.alvaroorgaz.easyfactura.model.Factura;
import com.alvaroorgaz.easyfactura.service.VerifactuHashService;
import org.junit.jupiter.api.Test;

class TfgApplicationTests {

	private final VerifactuHashService verifactuHashService = new VerifactuHashService();

	@Test
	void generaHashDeterministaParaLaMismaFactura() {
		Empresa empresa = new Empresa();
		empresa.setIdEmpresa(1);
		empresa.setCif("B12345678");

		Cliente cliente = new Cliente();
		cliente.setIdCliente(1);
		cliente.setNif("12345678A");

		Factura factura = new Factura();
		factura.setIdFactura(5);
		factura.setEmpresa(empresa);
		factura.setCliente(cliente);
		factura.setEstado(EstadoFactura.activa);
		factura.setFecha(java.time.LocalDateTime.of(2026, 5, 11, 12, 30));

		String hash1 = verifactuHashService.generarHashFactura(
				factura,
				new java.math.BigDecimal("121.00"),
				"hash-anterior"
		);

		String hash2 = verifactuHashService.generarHashFactura(
				factura,
				new java.math.BigDecimal("121.00"),
				"hash-anterior"
		);

		org.junit.jupiter.api.Assertions.assertEquals(hash1, hash2);
		org.junit.jupiter.api.Assertions.assertEquals(64, hash1.length());
	}

}
