package com.example.Smartspend_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/expenses")
    public String expensesPage() {
        return "forward:/expenses.html";
    }

    @GetMapping("/budgets")
    public String budgetspage() {
        return "forward:/budgets.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forward:/forgot-password.html";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "forward:/reset-password.html";
    }
}
