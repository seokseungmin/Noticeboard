package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join")
    public String joinProcess(@RequestBody JoinDTO joinDTO) {

        joinService.joinProcess(joinDTO);

        return "ok";
    }
}