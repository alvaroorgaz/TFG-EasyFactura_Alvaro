package com.example.TFG.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Index {

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        boolean esAdmin = false;

        if (authentication != null && authentication.getAuthorities() != null) {
            esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }

        model.addAttribute("esAdmin", esAdmin);
        return "index";
    }
}
