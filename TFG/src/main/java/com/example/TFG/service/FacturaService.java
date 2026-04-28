package com.example.TFG.service;

import com.example.TFG.dto.FacturaDetalleForm;
import com.example.TFG.dto.FacturaForm;
import com.example.TFG.model.Cliente;
import com.example.TFG.model.Empresa;
import com.example.TFG.model.EstadoFactura;
import com.example.TFG.model.Factura;
import com.example.TFG.model.FacturaDetalle;
import com.example.TFG.model.Producto;
import com.example.TFG.repository.ClienteRepository;
import com.example.TFG.repository.EmpresaRepository;
import com.example.TFG.repository.FacturaDetalleRepository;
import com.example.TFG.repository.FacturaRepository;
import com.example.TFG.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaDetalleRepository facturaDetalleRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public FacturaService(FacturaRepository facturaRepository,
                          FacturaDetalleRepository facturaDetalleRepository,
                          EmpresaRepository empresaRepository,
                          ClienteRepository clienteRepository,
                          ProductoRepository productoRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaDetalleRepository = facturaDetalleRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    public List<Factura> obtenerFacturas(boolean esAdmin, Empresa empresaLogueada) {
        if (esAdmin) {
            return facturaRepository.findAll();
        }
        return facturaRepository.findFacturasByEmpresaId(empresaLogueada.getId_empresa());
    }

    public Factura obtenerFacturaPorId(Integer id) {
        return facturaRepository.findById(id).orElse(null);
    }

    public List<FacturaDetalle> obtenerDetallesPorFactura(Integer idFactura) {
        return facturaDetalleRepository.findByFacturaId(idFactura);
    }

    public FacturaForm crearFormularioDesdeFactura(Factura factura) {
        FacturaForm form = new FacturaForm();
        form.setIdFactura(factura.getIdFactura());
        form.setEmpresaId(factura.getEmpresa().getId_empresa());
        form.setClienteId(factura.getCliente().getIdCliente());
        form.setEstado(factura.getEstado());
        form.setHashVerifactu(factura.getHashVerifactu());

        List<FacturaDetalle> detalles = facturaDetalleRepository.findByFacturaId(factura.getIdFactura());
        List<FacturaDetalleForm> detallesForm = new ArrayList<>();

        for (FacturaDetalle detalle : detalles) {
            FacturaDetalleForm detalleForm = new FacturaDetalleForm();
            detalleForm.setProductoId(detalle.getProducto().getIdProducto());
            detalleForm.setCantidad(detalle.getCantidad());
            detallesForm.add(detalleForm);
        }

        form.setDetalles(detallesForm);
        return form;
    }

    public BigDecimal calcularTotalFactura(Integer idFactura) {
        List<FacturaDetalle> detalles = facturaDetalleRepository.findByFacturaId(idFactura);
        BigDecimal total = BigDecimal.ZERO;

        for (FacturaDetalle detalle : detalles) {
            total = total.add(detalle.getTotal());
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void guardarFactura(FacturaForm form, boolean esAdmin, Empresa empresaLogueada) {
        Empresa empresa;

        if (esAdmin) {
            empresa = empresaRepository.findById(form.getEmpresaId()).orElse(null);
        } else {
            empresa = empresaLogueada;
        }

        if (empresa == null) {
            throw new RuntimeException("La empresa seleccionada no existe.");
        }

        Cliente cliente = clienteRepository.findById(form.getClienteId()).orElse(null);

        if (cliente == null) {
            throw new RuntimeException("El cliente seleccionado no existe.");
        }

        if (!cliente.getEmpresa().getId_empresa().equals(empresa.getId_empresa())) {
            throw new RuntimeException("El cliente no pertenece a la empresa seleccionada.");
        }

        List<FacturaDetalleForm> detallesValidos = new ArrayList<>();

        if (form.getDetalles() != null) {
            for (FacturaDetalleForm detalle : form.getDetalles()) {
                if (detalle.getProductoId() != null && detalle.getCantidad() != null) {
                    detallesValidos.add(detalle);
                }
            }
        }

        if (detallesValidos.isEmpty()) {
            throw new RuntimeException("Debes añadir al menos una línea de producto.");
        }

        Factura factura;

        if (form.getIdFactura() == null) {
            factura = new Factura();
            factura.setFecha(LocalDateTime.now());
        } else {
            factura = facturaRepository.findById(form.getIdFactura()).orElse(null);

            if (factura == null) {
                throw new RuntimeException("La factura no existe.");
            }

            if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
                throw new RuntimeException("No tienes permiso para modificar esta factura.");
            }
        }

        factura.setEmpresa(empresa);
        factura.setCliente(cliente);
        factura.setEstado(form.getEstado() != null ? form.getEstado() : EstadoFactura.activa);

        if (form.getHashVerifactu() == null || form.getHashVerifactu().isBlank()) {
            factura.setHashVerifactu(UUID.randomUUID().toString());
        } else {
            factura.setHashVerifactu(form.getHashVerifactu().trim());
        }

        factura = facturaRepository.save(factura);

        if (form.getIdFactura() != null) {
            facturaDetalleRepository.deleteByFacturaId(factura.getIdFactura());
        }

        for (FacturaDetalleForm detalleForm : detallesValidos) {
            Producto producto = productoRepository.findById(detalleForm.getProductoId()).orElse(null);

            if (producto == null) {
                throw new RuntimeException("Uno de los productos seleccionados no existe.");
            }

            if (!producto.getEmpresa().getId_empresa().equals(empresa.getId_empresa())) {
                throw new RuntimeException("Uno de los productos no pertenece a la empresa seleccionada.");
            }

            if (detalleForm.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad de cada producto debe ser mayor que 0.");
            }

            BigDecimal precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
            Integer iva = producto.getIva();
            BigDecimal cantidad = BigDecimal.valueOf(detalleForm.getCantidad());
            BigDecimal base = precioUnitario.multiply(cantidad);
            BigDecimal ivaCalculado = base.multiply(BigDecimal.valueOf(iva)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(ivaCalculado).setScale(2, RoundingMode.HALF_UP);

            FacturaDetalle detalle = new FacturaDetalle();
            detalle.setFactura(factura);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleForm.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setIva(iva);
            detalle.setTotal(total);

            facturaDetalleRepository.save(detalle);
        }
    }

    public void eliminarFactura(Integer id) {
        facturaRepository.deleteById(id);
    }
}
