package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Cliente;
import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.service.ClienteService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final EmpresaService empresaService;

    public ClienteController(ClienteService clienteService, EmpresaService empresaService) {
        this.clienteService = clienteService;
        this.empresaService = empresaService;
    }

    @GetMapping
    public String listaClientes(Model model, Authentication authentication) {
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);
        boolean esAdmin = esAdmin(authentication);

        if (esAdmin) {
            model.addAttribute("clientes", clienteService.obtenerClientes());
        } else {
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
        }

        model.addAttribute("esAdmin", esAdmin);
        return "cliente/lista";
    }

    @GetMapping("/crear")
    public String crearClienteForm(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);

        model.addAttribute("cliente", new Cliente());
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
        }

        return "cliente/crear";
    }

    @PostMapping
    public String guardarCliente(@ModelAttribute Cliente cliente,
                                 @RequestParam(required = false) Long empresaId,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            boolean esAdmin = esAdmin(authentication);
            Empresa empresa;

            if (esAdmin) {
                empresa = empresaService.obtenerEmpresaPorId(empresaId);
            } else {
                empresa = obtenerEmpresaLogueada(authentication);
            }

            if (empresa == null) {
                model.addAttribute("cliente", cliente);
                model.addAttribute("esAdmin", esAdmin);
                if (esAdmin) {
                    model.addAttribute("empresas", empresaService.obtenerEmpresas());
                }
                model.addAttribute("error", "La empresa seleccionada no existe.");
                return "cliente/crear";
            }

            boolean nuevo = (cliente.getIdCliente() == null);
            cliente.setEmpresa(empresa);
            clienteService.guardarCliente(cliente);

            if (nuevo) {
                redirectAttributes.addFlashAttribute("success", "Cliente creado exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Cliente actualizado exitosamente.");
            }

            return "redirect:/cliente";

        } catch (Exception e) {
            boolean esAdmin = esAdmin(authentication);
            model.addAttribute("cliente", cliente);
            model.addAttribute("esAdmin", esAdmin);
            if (esAdmin) {
                model.addAttribute("empresas", empresaService.obtenerEmpresas());
            }
            model.addAttribute("error", "Error al guardar el cliente: " + e.getMessage());
            return "cliente/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarClienteForm(@PathVariable Integer id,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/cliente";
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !cliente.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este cliente.");
            return "redirect:/cliente";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
        }

        return "cliente/crear";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/cliente";
        }

        boolean esAdmin = esAdmin(authentication);
        Empresa empresaLogueada = obtenerEmpresaLogueada(authentication);

        if (!esAdmin && !cliente.getEmpresa().getId_empresa().equals(empresaLogueada.getId_empresa())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este cliente.");
            return "redirect:/cliente";
        }

        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el cliente: " + e.getMessage());
        }

        return "redirect:/cliente";
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
