package com.hotel_mgt_system.auth_service_api.controller;

import com.hotel_mgt_system.auth_service_api.config.JwtService;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user-service/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SystemUserService systemUserService;
    private final JwtService jwtService;



}
