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
import com.hotel_mgt_system.auth_service_api.util.OtpGenerator;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    @Value("${keycloak.config.realm}")
    private String realm;


    private final SystemUserRepository systemUserRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;

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
        } else {
            Optional<SystemUser> selectedSystemUserFromAuthService = systemUserRepository.findByEmail(systemUserRequestDto.getEmail());
            if (selectedSystemUserFromAuthService.isPresent()) {
                Optional<Otp> selectedOtp = otpRepository.findBySystemUserId(selectedSystemUserFromAuthService.get().getUserId());
                if (selectedOtp.isPresent()) {
                    otpRepository.deleteById(selectedOtp.get().getPropertyId());
                }
                systemUserRepository.deleteById(selectedSystemUserFromAuthService.get().getUserId());
            }
        }

        //create user in keycloak

        UserRepresentation userRepresentation = mapUserRepository(systemUserRequestDto);
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            RoleRepresentation userRole = keycloak.realm(realm).roles().get("user").toRepresentation();
            userId = response.getLocation().getPath().replace(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Arrays.asList(userRole));
            UserRepresentation createdUser = keycloak.realm(realm).users().get(userId).toRepresentation();

            SystemUser sUser = SystemUser.builder()
                    .userId(userId)
                    .keycloakId(createdUser.getId())
                    .firstName(systemUserRequestDto.getFirstName())
                    .lastName(systemUserRequestDto.getLastName())
                    .email(systemUserRequestDto.getEmail())
                    .contact(systemUserRequestDto.getContact())
                    .isActive(false)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .isEmailVerified(false)
                    .createdAt(new Date().toInstant())
                    .updatedAt(new Date().toInstant())
                    .build();

            SystemUser savedUser = systemUserRepository.save(sUser);
            Otp createdOtp = Otp.builder()
                    .propertyId(UUID.randomUUID().toString())
                    .code(otpGenerator.generateOtp(5))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .isVerified(false)
                    .attempts(0)
                    .build();
            otpRepository.save(createdOtp);

            //Send Email
        }
    }

    //the user will be created in keycloak

    private UserRepresentation mapUserRepository(SystemUserRequestDto systemUserRequestDto) {
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(systemUserRequestDto.getFirstName());
        user.setLastName(systemUserRequestDto.getLastName());
        user.setEmail(systemUserRequestDto.getEmail());
        user.setUsername(systemUserRequestDto.getEmail());
        user.setEnabled(false);
        user.setEmailVerified(false);
        List<CredentialRepresentation> credList = new ArrayList<>();
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setValue(systemUserRequestDto.getPassword());
        credList.add(cred);
        user.setCredentials(credList);
        return user;
    }
}
