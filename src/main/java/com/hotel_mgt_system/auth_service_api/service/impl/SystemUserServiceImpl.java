package com.hotel_mgt_system.auth_service_api.service.impl;

import com.hotel_mgt_system.auth_service_api.config.KeycloakSecurityUtil;
import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;
import com.hotel_mgt_system.auth_service_api.entity.Otp;
import com.hotel_mgt_system.auth_service_api.entity.SystemUser;
import com.hotel_mgt_system.auth_service_api.exception.BadRequestException;
import com.hotel_mgt_system.auth_service_api.exception.DuplicateEntryException;
import com.hotel_mgt_system.auth_service_api.repository.OtpRepository;
import com.hotel_mgt_system.auth_service_api.repository.SystemUserRepository;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    @Value("${keycloak.config.realm}")
    private String realm;


    private final SystemUserRepository systemUserRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final OtpRepository otpRepository;

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

        UserRepresentation existingUser = null;
        keycloak = keycloakSecurityUtil.getKeycloakInstance();    //access keycloak using instance

        existingUser = keycloak.realm(realm).users().search(systemUserRequestDto.getEmail()).stream()
                .findFirst().orElse(null);

        if (existingUser != null) {
            Optional<SystemUser> selectedSystemUserFromAuthService = systemUserRepository.findByEmail(systemUserRequestDto.getEmail());
            if (selectedSystemUserFromAuthService.isEmpty()) {
                keycloak.realm(realm).users().delete(existingUser.getId());
            } else {
                throw new DuplicateEntryException("User already exists");
            }
        }else {
            Optional<SystemUser> selectedSystemUserFromAuthService = systemUserRepository.findByEmail(systemUserRequestDto.getEmail());
            if (selectedSystemUserFromAuthService.isPresent()) {
                Optional<Otp> selectedOtp = otpRepository.findBySystemUserId(selectedSystemUserFromAuthService.get().getUserid());
                if (selectedOtp.isPresent()) {
                    otpRepository.deleteById(selectedOtp.get().getPropertyId());
                }
                systemUserRepository.deleteById(selectedSystemUserFromAuthService.get().getUserid());
            }
        }


    }
}
