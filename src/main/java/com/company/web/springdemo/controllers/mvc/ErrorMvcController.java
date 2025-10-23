package com.company.web.springdemo.controllers.mvc;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorMvcController implements ErrorController {

    @GetMapping
    public String errorPage(Model model){
        model.addAttribute("statusCode",
                HttpStatus.NOT_FOUND.getReasonPhrase());
        return "ErrorView";
    }
}
