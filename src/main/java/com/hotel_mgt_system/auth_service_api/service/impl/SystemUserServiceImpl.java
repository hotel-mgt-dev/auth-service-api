package com.hotel_mgt_system.auth_service_api.service.impl;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;
import com.hotel_mgt_system.auth_service_api.exception.BadRequestException;
import com.hotel_mgt_system.auth_service_api.repository.SystemUserRepository;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserRepository systemUserRepository;

    @Override
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) {
        if (systemUserRequestDto.getFirstName() == null || systemUserRequestDto.getFirstName().trim().isEmpty()) {
            throw new BadRequestException("First name is required");
        }

        if (systemUserRequestDto.getLastName() == null || systemUserRequestDto.getLastName().trim().isEmpty()) {
            throw new BadRequestException("Last name is required");
        }

        if (systemUserRequestDto.getEmail() == null || systemUserRequestDto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }

        String userId = "";     //when save user in keycloak
        String otp = "";
        Keycloak keycloak = null;


    }
}
