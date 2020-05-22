package com.leyou.page.web;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class hello {

    @GetMapping("hello")
    public String toHello(Model model){
        model.addAttribute("name","你好");
        return "hello";
    }
}
