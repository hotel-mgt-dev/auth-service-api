package com.hotel_mgt_system.auth_service_api.service;

import com.hotel_mgt_system.auth_service_api.dto.request.PasswordRequestDto;
import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;

import java.io.IOException;
import java.util.List;


public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) throws IOException;
    public void initializeHosts(List<SystemUserRequestDto> systemUserRequestDtoList) throws IOException;
    public void resend(String email,String type) ;
    public void forgotPasswordSendVerificationCode(String email, String password);
    public boolean verifyReset(String email, String otp);
    public boolean passwordReset(PasswordRequestDto passwordRequestDto);

}
