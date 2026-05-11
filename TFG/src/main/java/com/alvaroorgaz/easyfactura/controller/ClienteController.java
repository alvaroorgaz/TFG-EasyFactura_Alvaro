package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Cliente;
import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.service.AuthService;
import com.alvaroorgaz.easyfactura.service.ClienteService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final EmpresaService empresaService;
    private final AuthService authService;

    public ClienteController(ClienteService clienteService,
                             EmpresaService empresaService,
                             AuthService authService) {
        this.clienteService = clienteService;
        this.empresaService = empresaService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String listaClientes(Model model, Authentication authentication) {
        Empresa empresaLogueada = authService.getEmpresaLogin();
        boolean esAdmin = authService.isAdmin();

        if (esAdmin) {
            model.addAttribute("clientes", clienteService.obtenerClientes());
        } else {
            model.addAttribute("clientes", clienteService.obtenerClientesPorEmpresa(empresaLogueada.getId_empresa()));
        }

        model.addAttribute("esAdmin", esAdmin);
        return "cliente/lista";
    }

    @GetMapping("/crear")
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String crearClienteForm(Model model, Authentication authentication) {
        boolean esAdmin = authService.isAdmin();

        model.addAttribute("cliente", new Cliente());
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
        }

        return "cliente/crear";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPRESA')")
    public String guardarCliente(@Valid @ModelAttribute Cliente cliente,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) Long empresaId,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                boolean esAdmin = authService.isAdmin();
                model.addAttribute("cliente", cliente);
                model.addAttribute("esAdmin", esAdmin);
                if (esAdmin) {
                    model.addAttribute("empresas", empresaService.obtenerEmpresas());
                }
                model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
                return "cliente/crear";
            }

            boolean esAdmin = authService.isAdmin();
            Empresa empresa;

            if (esAdmin) {
                empresa = empresaService.obtenerEmpresaPorId(empresaId);
            } else {
                empresa = authService.getEmpresaLogin();
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
            boolean esAdmin = authService.isAdmin();
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
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioCliente(#id)")
    public String editarClienteForm(@PathVariable Long id,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/cliente";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

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
    @PreAuthorize("hasRole('ADMIN') or @authService.esPropietarioCliente(#id)")
    public String eliminarCliente(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/cliente";
        }

        boolean esAdmin = authService.isAdmin();
        Empresa empresaLogueada = authService.getEmpresaLogin();

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
}
