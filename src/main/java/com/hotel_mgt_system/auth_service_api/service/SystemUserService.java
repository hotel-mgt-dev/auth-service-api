package com.hotel_mgt_system.auth_service_api.service;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;

import java.io.IOException;
import java.util.List;


public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) throws IOException;
    public void initializeHosts(List<SystemUserRequestDto> systemUserRequestDtoList) throws IOException;
    public void resend(String email,String type) throws IOException;

}
