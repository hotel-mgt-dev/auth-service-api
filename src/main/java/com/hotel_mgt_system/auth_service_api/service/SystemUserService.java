package com.hotel_mgt_system.auth_service_api.service;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;

public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto);

}
