package com.example.TFG.controller;

import com.example.TFG.model.Empresa;
import com.example.TFG.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {
    private final EmpresaService empresaService;
    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
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
    public String guardarEmpresa(@ModelAttribute Empresa empresa) {
        empresaService.crearEmpresa(empresa);
        return "redirect:/empresa";
    }
    @GetMapping("/editar/{id}")
    public String editarEmpresaForm(@PathVariable Long id, Model model) {
        model.addAttribute("empresa", empresaService.obtenerEmpresaPorId(id));
        return "empresa/editar";
    }
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpresa(@PathVariable Long id) {
        empresaService.eliminarEmpresa(id);
        return "redirect:/empresa";
    }
}
