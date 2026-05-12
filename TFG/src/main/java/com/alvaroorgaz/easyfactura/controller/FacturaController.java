package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.dto.FacturaDetalleForm;
import com.alvaroorgaz.easyfactura.dto.FacturaForm;
import com.alvaroorgaz.easyfactura.dto.ResumenFactura;
import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.model.EstadoFactura;
import com.alvaroorgaz.easyfactura.model.Factura;
import com.alvaroorgaz.easyfactura.model.FacturaRectificada;
import com.alvaroorgaz.easyfactura.service.AuthService;
import com.alvaroorgaz.easyfactura.service.ClienteService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import com.alvaroorgaz.easyfactura.service.FacturaPdfService;
import com.alvaroorgaz.easyfactura.service.FirmaPdfService;
import com.alvaroorgaz.easyfactura.service.FacturaEmailService;
import com.alvaroorgaz.easyfactura.service.FacturaService;
import com.alvaroorgaz.easyfactura.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private final FacturaEmailService facturaEmailService;
    private final AuthService authService;
    private final EmpresaService empresaService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public FacturaController(FacturaService facturaService,
                             FacturaPdfService facturaPdfService,
                             FirmaPdfService firmaPdfService,
                             FacturaEmailService facturaEmailService,
                             AuthService authService,
                             EmpresaService empresaService,
                             ClienteService clienteService,
                             ProductoService productoService) {
        this.facturaService = facturaService;
        this.facturaPdfService = facturaPdfService;
        this.firmaPdfService = firmaPdfService;
        this.facturaEmailService = facturaEmailService;
        this.authService = authService;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String listaFacturas(Model model, Authentication authentication) {
        Empresa empresaLogueada = authService.getEmpresaLogin();
        boolean esAdmin = authService.isAdmin();

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
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String crearFacturaForm(Model model, Authentication authentication) {
        FacturaForm facturaForm = new FacturaForm();
        facturaForm.getDetalles().add(new FacturaDetalleForm());

        cargarDatosFormulario(model, facturaForm, authentication, null);
        return "factura/crear";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String guardarFactura(@Valid @ModelAttribute("facturaForm") FacturaForm facturaForm,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                if (facturaForm.getDetalles() == null || facturaForm.getDetalles().isEmpty()) {
                    facturaForm.getDetalles().add(new FacturaDetalleForm());
                }
                cargarDatosFormulario(model, facturaForm, authentication, bindingResult.getAllErrors().get(0).getDefaultMessage());
                return "factura/crear";
            }

            facturaService.guardarFactura(facturaForm, authService.isAdmin(), authService.getEmpresaLogin());
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
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioFactura(#id)")
    public String rectificarFacturaForm(@PathVariable Integer id,
                                        Authentication authentication,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

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
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioFactura(#id)")
    public String verHistoricoFactura(@PathVariable Integer id,
                                      Authentication authentication,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

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
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioFactura(#id)")
    public ResponseEntity<byte[]> descargarPdfFactura(@PathVariable Integer id,
                                                      Authentication authentication) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            return ResponseEntity.notFound().build();
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

        if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            return ResponseEntity.status(403).build();
        }

        List<com.alvaroorgaz.easyfactura.model.FacturaDetalle> detalles = facturaService.obtenerDetallesPorFactura(id);
        ResumenFactura resumenFactura = facturaService.calcularResumenFactura(id);
        byte[] pdf = facturaPdfService.generarPdf(factura, detalles, resumenFactura);
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

    @PostMapping("/email/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioFactura(#id)")
    public String enviarFacturaPorEmail(@PathVariable Integer id,
                                        RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        try {
            List<com.alvaroorgaz.easyfactura.model.FacturaDetalle> detalles = facturaService.obtenerDetallesPorFactura(id);
            ResumenFactura resumenFactura = facturaService.calcularResumenFactura(id);
            byte[] pdf = facturaPdfService.generarPdf(factura, detalles, resumenFactura);
            byte[] pdfFirmado = firmaPdfService.firmarPdf(pdf, factura.getEmpresa());

            facturaEmailService.enviarFacturaFirmada(factura, pdfFirmado);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Factura enviada por email a " + factura.getEmpresa().getEmail() + " y " + factura.getCliente().getEmail() + "."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo enviar la factura: " + e.getMessage());
        }

        return "redirect:/factura";
    }

    private void cargarDatosFormulario(Model model,
                                       FacturaForm facturaForm,
                                       Authentication authentication,
                                       String error) {
        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

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
}
