package com.example.TFG.controller;

import com.example.TFG.model.Cliente;
import com.example.TFG.model.Empresa;
import com.example.TFG.model.EstadoFactura;
import com.example.TFG.model.Factura;
import com.example.TFG.service.ClienteService;
import com.example.TFG.service.EmpresaService;
import com.example.TFG.service.FacturaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/factura")
public class FacturaController {

    private final FacturaService facturaService;
    private final EmpresaService empresaService;
    private final ClienteService clienteService;

    public FacturaController(FacturaService facturaService,
                             EmpresaService empresaService,
                             ClienteService clienteService) {
        this.facturaService = facturaService;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listaFacturas(Model model, Authentication authentication) {
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);
        boolean esAdmin = esAdmin(authentication);

        if (esAdmin) {
            model.addAttribute("facturas", facturaService.obtenerFacturas());
        } else {
            model.addAttribute("facturas", facturaService.obtenerFacturasPorEmpresa(empresaLogueada.getId_empresa()));
        }

        model.addAttribute("esAdmin", esAdmin);
        return "factura/lista";
    }

    @GetMapping("/crear")
    public String crearFacturaForm(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);

        model.addAttribute("factura", new Factura());
        model.addAttribute("estados", EstadoFactura.values());
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("clientes", clienteService.obtenerClientes());
        } else {
            Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
        }

        return "factura/crear";
    }

    @PostMapping
    public String guardarFactura(@ModelAttribute Factura factura,
                                 @RequestParam Integer clienteId,
                                 @RequestParam(required = false) Long empresaId,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            boolean esAdmin = esAdmin(authentication);
            Empresa empresa;
            Cliente cliente = clienteService.obtenerClientePorId(clienteId);

            if (esAdmin) {
                empresa = empresaService.obtenerEmpresaPorId(empresaId);
            } else {
                empresa = obtenerEmpresaLogueada(authentication);
            }

            if (empresa == null) {
                cargarDatosFormulario(model, factura, authentication, "La empresa seleccionada no existe.");
                return "factura/crear";
            }

            if (cliente == null) {
                cargarDatosFormulario(model, factura, authentication, "El cliente seleccionado no existe.");
                return "factura/crear";
            }

            if (!cliente.getEmpresa().getId_empresa().equals(empresa.getId_empresa())) {
                cargarDatosFormulario(model, factura, authentication, "El cliente no pertenece a la empresa seleccionada.");
                return "factura/crear";
            }

            boolean nueva = (factura.getIdFactura() == null);

            factura.setEmpresa(empresa);
            factura.setCliente(cliente);

            if (factura.getEstado() == null) {
                factura.setEstado(EstadoFactura.activa);
            }

            facturaService.guardarFactura(factura);

            if (nueva) {
                redirectAttributes.addFlashAttribute("success", "Factura creada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Factura actualizada exitosamente.");
            }

            return "redirect:/factura";

        } catch (Exception e) {
            cargarDatosFormulario(model, factura, authentication, "Error al guardar la factura: " + e.getMessage());
            return "factura/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFacturaForm(@PathVariable Long id,
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

        model.addAttribute("factura", factura);
        model.addAttribute("estados", EstadoFactura.values());
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("clientes", clienteService.obtenerClientes());
        } else {
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
        }

        return "factura/crear";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarFactura(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerFacturaPorId(id);

        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada.");
            return "redirect:/factura";
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !factura.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta factura.");
            return "redirect:/factura";
        }

        try {
            facturaService.eliminarFactura(id);
            redirectAttributes.addFlashAttribute("success", "Factura eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la factura: " + e.getMessage());
        }

        return "redirect:/factura";
    }

    private void cargarDatosFormulario(Model model,
                                       Factura factura,
                                       Authentication authentication,
                                       String error) {
        boolean esAdmin = esAdmin(authentication);

        model.addAttribute("factura", factura);
        model.addAttribute("estados", EstadoFactura.values());
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("error", error);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("clientes", clienteService.obtenerClientes());
        } else {
            Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
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
