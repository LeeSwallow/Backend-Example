package com.pnu.chattest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/chat")
    public String chat() {
        return "forward:/chat.html";
    }
}
