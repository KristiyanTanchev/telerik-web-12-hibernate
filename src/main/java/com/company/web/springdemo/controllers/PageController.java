package com.company.web.springdemo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/home")
    public String home(Model model) {
        return "HomeView"; // renders templates/about.html
    }

    @GetMapping("/beers")
    public String beersView(Model model) {
        return "BeersView"; // renders templates/about.html
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "About"; // renders templates/about.html
    }
}
