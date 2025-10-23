package com.hotel_mgt_system.auth_service_api.service;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;

import java.io.IOException;

public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) throws IOException;

}
