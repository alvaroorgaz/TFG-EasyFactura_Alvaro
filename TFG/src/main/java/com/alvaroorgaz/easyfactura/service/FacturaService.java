package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.dto.FacturaDetalleForm;
import com.alvaroorgaz.easyfactura.dto.FacturaForm;
import com.alvaroorgaz.easyfactura.model.Cliente;
import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.EstadoFactura;
import com.alvaroorgaz.easyfactura.model.Factura;
import com.alvaroorgaz.easyfactura.model.FacturaDetalle;
import com.alvaroorgaz.easyfactura.model.FacturaRectificada;
import com.alvaroorgaz.easyfactura.model.Producto;
import com.alvaroorgaz.easyfactura.repository.ClienteRepository;
import com.alvaroorgaz.easyfactura.repository.EmpresaRepository;
import com.alvaroorgaz.easyfactura.repository.FacturaDetalleRepository;
import com.alvaroorgaz.easyfactura.repository.FacturaRectificadaRepository;
import com.alvaroorgaz.easyfactura.repository.FacturaRepository;
import com.alvaroorgaz.easyfactura.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaDetalleRepository facturaDetalleRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FacturaRectificadaRepository facturaRectificadaRepository;
    private final VerifactuHashService verifactuHashService;

    public FacturaService(FacturaRepository facturaRepository,
                          FacturaDetalleRepository facturaDetalleRepository,
                          EmpresaRepository empresaRepository,
                          ClienteRepository clienteRepository,
                          ProductoRepository productoRepository,
                          FacturaRectificadaRepository facturaRectificadaRepository,
                          VerifactuHashService verifactuHashService) {
        this.facturaRepository = facturaRepository;
        this.facturaDetalleRepository = facturaDetalleRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.facturaRectificadaRepository = facturaRectificadaRepository;
        this.verifactuHashService = verifactuHashService;
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

    public FacturaForm crearFormularioRectificacion(Factura factura) {
        FacturaForm form = crearFormularioDesdeFactura(factura);
        form.setIdFactura(null);
        form.setFacturaOriginalId(factura.getIdFactura());
        form.setEstado(EstadoFactura.rectificada);
        form.setHashVerifactu("");
        form.setMotivoRectificacion("");
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

    public BigDecimal calcularBaseImponibleFactura(Integer idFactura) {
        List<FacturaDetalle> detalles = facturaDetalleRepository.findByFacturaId(idFactura);
        BigDecimal baseImponible = BigDecimal.ZERO;

        for (FacturaDetalle detalle : detalles) {
            BigDecimal baseLinea = detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));
            baseImponible = baseImponible.add(baseLinea);
        }

        return baseImponible.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalIvaFactura(Integer idFactura) {
        List<FacturaDetalle> detalles = facturaDetalleRepository.findByFacturaId(idFactura);
        BigDecimal totalIva = BigDecimal.ZERO;

        for (FacturaDetalle detalle : detalles) {
            BigDecimal baseLinea = detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));
            BigDecimal ivaLinea = baseLinea.multiply(BigDecimal.valueOf(detalle.getIva()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalIva = totalIva.add(ivaLinea);
        }

        return totalIva.setScale(2, RoundingMode.HALF_UP);
    }

    public Factura obtenerFacturaOriginalParaHistorico(Integer idFactura) {
        FacturaRectificada relacion = facturaRectificadaRepository.findByFacturaRectificadaId(idFactura);
        if (relacion != null) {
            return relacion.getFacturaOriginal();
        }
        return obtenerFacturaPorId(idFactura);
    }

    public List<FacturaRectificada> obtenerHistoricoFactura(Integer idFactura) {
        Factura facturaOriginal = obtenerFacturaOriginalParaHistorico(idFactura);
        if (facturaOriginal == null) {
            return new ArrayList<>();
        }
        return facturaRectificadaRepository.findByFacturaOriginalId(facturaOriginal.getIdFactura());
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
            throw new RuntimeException("Debes anadir al menos una linea de producto.");
        }

        Factura factura = new Factura();
        factura.setFecha(LocalDateTime.now());

        Factura facturaOriginal = null;
        if (form.getFacturaOriginalId() != null) {
            facturaOriginal = facturaRepository.findById(form.getFacturaOriginalId()).orElse(null);

            if (facturaOriginal == null) {
                throw new RuntimeException("La factura original no existe.");
            }

            if (!esAdmin && !facturaOriginal.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
                throw new RuntimeException("No tienes permiso para rectificar esta factura.");
            }

            if (form.getMotivoRectificacion() == null || form.getMotivoRectificacion().isBlank()) {
                throw new RuntimeException("Debes indicar el motivo de la rectificacion.");
            }
        }

        factura.setEmpresa(empresa);
        factura.setCliente(cliente);
        factura.setEstado(form.getEstado() != null ? form.getEstado() : EstadoFactura.activa);
        factura.setHashVerifactu("PENDIENTE");

        factura = facturaRepository.save(factura);

        BigDecimal totalFactura = BigDecimal.ZERO;

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
            totalFactura = totalFactura.add(total);
        }

        Factura facturaAnterior = facturaRepository.findUltimaFacturaAnterior(empresa.getId_empresa(), factura.getIdFactura());
        String hashAnterior = facturaAnterior != null ? facturaAnterior.getHashVerifactu() : null;
        factura.setHashVerifactu(verifactuHashService.generarHashFactura(
                factura,
                totalFactura.setScale(2, RoundingMode.HALF_UP),
                hashAnterior
        ));
        factura = facturaRepository.save(factura);

        if (facturaOriginal != null) {
            FacturaRectificada relacion = new FacturaRectificada();
            relacion.setFacturaOriginal(facturaOriginal);
            relacion.setFacturaRectificada(factura);
            relacion.setMotivo(form.getMotivoRectificacion().trim());
            relacion.setFecha(LocalDateTime.now());
            facturaRectificadaRepository.save(relacion);
        }
    }
}
