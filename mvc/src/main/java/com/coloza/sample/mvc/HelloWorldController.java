package com.coloza.sample.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/helloworld")
public class HelloWorldController {

    @RequestMapping("/show-form")
    public String showForm() {
        return "helloworld/show-form";
    }

    @RequestMapping("/process-form")
    public String processForm(@RequestParam("studentName") String name, Model model) {
        model.addAttribute("studentName", name.toUpperCase());
        return "helloworld/process-form";
    }

}
