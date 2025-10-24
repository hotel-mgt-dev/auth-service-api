package com.hotel_mgt_system.auth_service_api.service;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;

import java.io.IOException;
import java.util.ArrayList;

public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) throws IOException;
    public void initializeHosts(ArrayList<SystemUserRequestDto> systemUserRequestDtoList) throws IOException;

}
