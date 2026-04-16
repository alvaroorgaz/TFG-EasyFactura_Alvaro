package com.example.TFG.controller;

import com.example.TFG.model.Cliente;
import com.example.TFG.model.Empresa;
import com.example.TFG.service.ClienteService;
import com.example.TFG.service.EmpresaService;
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
    public String listaClientes(Model model) {
        model.addAttribute("clientes", clienteService.obtenerClientes());
        return "cliente/lista";
    }

    @GetMapping("/crear")
    public String crearClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("empresas", empresaService.obtenerEmpresas());
        return "cliente/crear";
    }

    @PostMapping
    public String guardarCliente(@ModelAttribute Cliente cliente,
                                 @RequestParam Long empresaId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.obtenerEmpresaPorId(empresaId);

            if (empresa == null) {
                model.addAttribute("cliente", cliente);
                model.addAttribute("empresas", empresaService.obtenerEmpresas());
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
            model.addAttribute("cliente", cliente);
            model.addAttribute("empresas", empresaService.obtenerEmpresas());
            model.addAttribute("error", "Error al guardar el cliente: " + e.getMessage());
            return "cliente/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarClienteForm(@PathVariable Integer id,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.obtenerClientePorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/cliente";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("empresas", empresaService.obtenerEmpresas());
        return "cliente/crear";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id,
                                  RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el cliente: " + e.getMessage());
        }

        return "redirect:/cliente";
    }
}
