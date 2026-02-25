package com.boot.security.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/admin/users")
    public String userManagementPage(){
        return "admin/user_list";
    }
}
