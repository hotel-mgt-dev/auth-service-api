package com.hotel_mgt_system.auth_service_api.controller;

import com.hotel_mgt_system.auth_service_api.config.JwtService;
import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import com.hotel_mgt_system.auth_service_api.util.StandardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("user-service/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SystemUserService systemUserService;
    private final JwtService jwtService;

    @PostMapping("/visitors/signup")
    public ResponseEntity<StandardResponseDto> createSystemUser(@RequestBody SystemUserRequestDto systemUserRequestDto) throws IOException {
        systemUserService.createSystemUser(systemUserRequestDto);
        return new ResponseEntity<>(
                new StandardResponseDto(201, "User account was created", null),
                HttpStatus.CREATED);
    }

    @PostMapping("/visitors/resend")
    public ResponseEntity<StandardResponseDto> resend(@RequestParam String email, @RequestParam String type) throws IOException {
        systemUserService.resend(email,type);
        return new ResponseEntity<>(
                new StandardResponseDto(200, "Please check your email", null),
                HttpStatus.OK);
    }

    @PostMapping("/visitors/forgot-password-request-code")
    public ResponseEntity<StandardResponseDto> forgotPasswordRequest(@RequestParam String email) throws IOException {
        systemUserService.forgotPasswordSendVerificationCode(email);
        return new ResponseEntity<>(
                new StandardResponseDto(200, "Please check your email", null),
                HttpStatus.OK);
    }

    @PostMapping("/visitors/verify-reset")
    public ResponseEntity<StandardResponseDto> verifyReset(@RequestParam String email, @RequestParam String otp) throws IOException {
        boolean isVerified = systemUserService.verifyReset(email,otp);
        return new ResponseEntity<>(
                new StandardResponseDto(isVerified?200:400, isVerified?"Verified":"Try again",isVerified),
                isVerified?HttpStatus.OK:HttpStatus.BAD_REQUEST);
    }



}
