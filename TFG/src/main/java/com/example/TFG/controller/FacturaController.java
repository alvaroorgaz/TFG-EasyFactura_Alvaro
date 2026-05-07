package com.example.TFG.controller;

import com.example.TFG.dto.FacturaDetalleForm;
import com.example.TFG.dto.FacturaForm;
import com.example.TFG.model.Empresa;
import com.example.TFG.model.EstadoFactura;
import com.example.TFG.model.Factura;
import com.example.TFG.model.FacturaRectificada;
import com.example.TFG.service.ClienteService;
import com.example.TFG.service.EmpresaService;
import com.example.TFG.service.FacturaPdfService;
import com.example.TFG.service.FirmaPdfService;
import com.example.TFG.service.FacturaService;
import com.example.TFG.service.ProductoService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/factura")
public class FacturaController {

    private final FacturaService facturaService;
    private final FacturaPdfService facturaPdfService;
    private final FirmaPdfService firmaPdfService;
    private final EmpresaService empresaService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public FacturaController(FacturaService facturaService,
                             FacturaPdfService facturaPdfService,
                             FirmaPdfService firmaPdfService,
                             EmpresaService empresaService,
                             ClienteService clienteService,
                             ProductoService productoService) {
        this.facturaService = facturaService;
        this.facturaPdfService = facturaPdfService;
        this.firmaPdfService = firmaPdfService;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    @GetMapping
    public String listaFacturas(Model model, Authentication authentication) {
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);
        boolean esAdmin = esAdmin(authentication);

        List<Factura> facturas = facturaService.obtenerFacturas(esAdmin, empresaLogueada);
        Map<Integer, BigDecimal> totalesFacturas = new HashMap<>();

        for (Factura factura : facturas) {
            totalesFacturas.put(factura.getIdFactura(), facturaService.calcularTotalFactura(factura.getIdFactura()));
        }

        model.addAttribute("facturas", facturas);
        model.addAttribute("totalesFacturas", totalesFacturas);
        model.addAttribute("esAdmin", esAdmin);
        return "factura/lista";
    }

    @GetMapping("/crear")
    public String crearFacturaForm(Model model, Authentication authentication) {
        FacturaForm facturaForm = new FacturaForm();
        facturaForm.getDetalles().add(new FacturaDetalleForm());

        cargarDatosFormulario(model, facturaForm, authentication, null);
        return "factura/crear";
    }

    @PostMapping
    public String guardarFactura(@ModelAttribute("facturaForm") FacturaForm facturaForm,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            facturaService.guardarFactura(facturaForm, esAdmin(authentication), obtenerEmpresaLogueada(authentication));

            if (facturaForm.getFacturaOriginalId() != null) {
                redirectAttributes.addFlashAttribute("success", "Nueva version de la factura creada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Factura creada exitosamente.");
            }

            return "redirect:/factura";

        } catch (Exception e) {
            if (facturaForm.getDetalles() == null || facturaForm.getDetalles().isEmpty()) {
                facturaForm.getDetalles().add(new FacturaDetalleForm());
            }
            cargarDatosFormulario(model, facturaForm, authentication, "Error al guardar la factura: " + e.getMessage());
            return "factura/crear";
        }
    }

    @GetMapping("/rectificar/{id}")
    public String rectificarFacturaForm(@PathVariable Integer id,
                                        Authentication authentication,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar esta factura.");
            return "redirect:/factura";
        }

        FacturaForm facturaForm = facturaService.crearFormularioRectificacion(factura);
        if (facturaForm.getDetalles().isEmpty()) {
            facturaForm.getDetalles().add(new FacturaDetalleForm());
        }

        cargarDatosFormulario(model, facturaForm, authentication, null);
        return "factura/crear";
    }

    @GetMapping("/historico/{id}")
    public String verHistoricoFactura(@PathVariable Integer id,
                                      Authentication authentication,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para consultar el historico de esta factura.");
            return "redirect:/factura";
        }

        Factura facturaOriginal = facturaService.obtenerFacturaOriginalParaHistorico(id);
        List<FacturaRectificada> historico = facturaService.obtenerHistoricoFactura(id);

        model.addAttribute("facturaActual", factura);
        model.addAttribute("facturaOriginal", facturaOriginal);
        model.addAttribute("historico", historico);
        model.addAttribute("totalFacturaOriginal", facturaService.calcularTotalFactura(facturaOriginal.getIdFactura()));
        return "factura/historico";
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> descargarPdfFactura(@PathVariable Integer id,
                                                      Authentication authentication) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            return ResponseEntity.notFound().build();
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            return ResponseEntity.status(403).build();
        }

        List<com.example.TFG.model.FacturaDetalle> detalles = facturaService.obtenerDetallesPorFactura(id);
        BigDecimal baseImponible = facturaService.calcularBaseImponibleFactura(id);
        BigDecimal totalIva = facturaService.calcularTotalIvaFactura(id);
        BigDecimal totalFactura = facturaService.calcularTotalFactura(id);
        byte[] pdf = facturaPdfService.generarPdf(factura, detalles, baseImponible, totalIva, totalFactura);
        byte[] pdfFirmado = firmaPdfService.firmarPdf(pdf, factura.getEmpresa());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura-" + factura.getIdFactura() + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfFirmado);
    }

    private void cargarDatosFormulario(Model model,
                                       FacturaForm facturaForm,
                                       Authentication authentication,
                                       String error) {
        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        model.addAttribute("facturaForm", facturaForm);
        model.addAttribute("estados", EstadoFactura.values());
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esRectificacion", facturaForm.getFacturaOriginalId() != null);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("clientes", clienteService.obtenerClientes());
            model.addAttribute("productos", productoService.obtenerProductos());
        } else {
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
            model.addAttribute("productos", productoService.obtenerProductosPorEmpresa(empresaLogueada.getId_empresa()));
        }

        if (error != null) {
            model.addAttribute("error", error);
        }
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private Empresa obtenerEmpresaLogueada(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return empresaService.obtenerEmpresas().stream()
                .filter(e -> e.getEmail().equals(authentication.getName()))
                .findFirst()
                .orElse(null);
    }
}
