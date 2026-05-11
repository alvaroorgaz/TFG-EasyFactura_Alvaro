package com.alvaroorgaz.easyfactura.controller;

import com.alvaroorgaz.easyfactura.model.Empresa;
import com.alvaroorgaz.easyfactura.service.AuthService;
import com.alvaroorgaz.easyfactura.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empresa")
@PreAuthorize("hasRole('ADMIN')")
public class EmpresaController {
    private final EmpresaService empresaService;
    private final AuthService authService;

    public EmpresaController(EmpresaService empresaService, AuthService authService) {
        this.empresaService = empresaService;
        this.authService = authService;
    }

    @GetMapping
    public String ListaEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.obtenerEmpresas());
        return "empresa/lista";
    }
    @GetMapping("/crear")
    public String crearEmpresaForm(Model model) {
        model.addAttribute("empresa", new Empresa());

        return "empresa/crear";
    }
    @PostMapping
    public String guardarEmpresa(@Valid @ModelAttribute Empresa empresa,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (empresa.getId_empresa() == null && (empresa.getPassword() == null || empresa.getPassword().isBlank())) {
                bindingResult.rejectValue("password", "password.blank", "La contrasena es obligatoria.");
            }

            if (bindingResult.hasErrors()) {
                model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
                return "empresa/crear";
            }

            boolean nueva = (empresa.getId_empresa() == null);
        empresaService.crearEmpresa(empresa);
        if (nueva) {
            redirectAttributes.addFlashAttribute("success", "Empresa creada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Empresa actualizada exitosamente.");
        }
        return "redirect:/empresa";
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("key 'empresa.cif'")){
                model.addAttribute("error", "Error al crear la empresa: El CIF ya existe.");
            }
            else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("key 'empresa.email'")){
                model.addAttribute("error", "Error al crear la empresa: El nombre ya existe.");
            }
            else{
                model.addAttribute("error", "Error al crear la empresa: " + e.getMessage());
            }
            return "empresa/crear";
        }
    }
    @GetMapping("/editar/{id}")
    public String editarEmpresaForm(@PathVariable Long id, Model model) {
        model.addAttribute("empresa", empresaService.obtenerEmpresaPorId(id));
        return "empresa/crear";
    }
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (authService.esMismaEmpresa(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propio usuario administrador.");
            return "redirect:/empresa";
        }

        if (authService.esAdminPorId(id)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar una empresa con rol ADMIN.");
            return "redirect:/empresa";
        }

        empresaService.eliminarEmpresa(id);
        redirectAttributes.addFlashAttribute("success", "Empresa eliminada exitosamente.");
        return "redirect:/empresa";
    }
}
