package com.springboot.noticeboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//
@RestController
public class AdminController {

    //관리자 컨트롤러 작성 예정.
    @GetMapping("/admin")
    public String admin() {
        return "Admin Controller";
    }
}