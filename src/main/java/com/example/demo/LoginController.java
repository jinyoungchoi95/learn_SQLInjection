package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/test")
public class LoginController {

    private final LoginService loginService;

    @RequestMapping
    public String index() {
        return "login-form";
    }
    @RequestMapping("/injection")
    public String indexToInjection() {
        return "login-error-form";
    }

    @PostMapping("/user/login/injection")
    public String userLoginWithInjection(@RequestParam String id, @RequestParam String password) throws Exception {
        if(loginService.findUserByIdAndPwError(id, password)) return "login-yes";
        return "login-no";
    }

    @PostMapping("/user/login")
    public String userLogin(@RequestParam String id, @RequestParam String password) throws Exception {
        if(loginService.findUserByIdAndPw(id, password)) return "login-yes";
        return "login-no";
    }
}
