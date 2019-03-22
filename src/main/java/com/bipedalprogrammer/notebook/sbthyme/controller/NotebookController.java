package com.bipedalprogrammer.notebook.sbthyme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notebook")
public class NotebookController {
    @GetMapping
    public String getNotebooks(Model model) {

        return "notebook";
    }
}
