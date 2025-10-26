package com.hotel_mgt_system.auth_service_api.service.impl;

import com.hotel_mgt_system.auth_service_api.config.KeycloakSecurityUtil;
import com.hotel_mgt_system.auth_service_api.dto.request.LoginRequestDto;
import com.hotel_mgt_system.auth_service_api.dto.request.PasswordRequestDto;
import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;
import com.hotel_mgt_system.auth_service_api.entity.Otp;
import com.hotel_mgt_system.auth_service_api.entity.SystemUser;
import com.hotel_mgt_system.auth_service_api.exception.BadRequestException;
import com.hotel_mgt_system.auth_service_api.exception.DuplicateEntryException;
import com.hotel_mgt_system.auth_service_api.exception.EntryNotFoundException;
import com.hotel_mgt_system.auth_service_api.exception.UnAuthorizedException;
import com.hotel_mgt_system.auth_service_api.repository.OtpRepository;
import com.hotel_mgt_system.auth_service_api.repository.SystemUserRepository;
import com.hotel_mgt_system.auth_service_api.service.EmailService;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import com.hotel_mgt_system.auth_service_api.util.OtpGenerator;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {


    private final OtpGenerator otpGenerator;
    @Value("${keycloak.config.realm}")
    private String realm;


    private final SystemUserRepository systemUserRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Override
    public void createSystemUser(SystemUserRequestDto systemUserRequestDto) throws IOException {
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

        UserRepresentation userRepresentation = mapUserRepository(systemUserRequestDto, false, false);
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
                    .isEnabled(false)
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
            emailService.sendUserSignUpVerificationCode(systemUserRequestDto.getEmail(), "Verify Your Email", createdOtp.getCode(), systemUserRequestDto.getFirstName());
        }
    }

    //the user will be created in keycloak

    private UserRepresentation mapUserRepository(SystemUserRequestDto systemUserRequestDto, boolean isEmailVerified, boolean isEnabled) {
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(systemUserRequestDto.getFirstName());
        user.setLastName(systemUserRequestDto.getLastName());
        user.setEmail(systemUserRequestDto.getEmail());
        user.setUsername(systemUserRequestDto.getEmail());
        user.setEnabled(isEnabled);
        user.setEmailVerified(isEmailVerified);
        List<CredentialRepresentation> credList = new ArrayList<>();
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setValue(systemUserRequestDto.getPassword());
        credList.add(cred);
        user.setCredentials(credList);
        return user;
    }


    @Override
    public void initializeHosts(List<SystemUserRequestDto> users) throws IOException {
        for (SystemUserRequestDto systemUserRequestDto : users) {
            Optional<SystemUser> selectedSystemUser = systemUserRepository.findByEmail(systemUserRequestDto.getEmail());
            if (selectedSystemUser.isPresent()) {
                continue;
            }


            String userId = "";
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

            UserRepresentation userRepresentation = mapUserRepository(systemUserRequestDto, true, true);
            Response response = keycloak.realm(realm).users().create(userRepresentation);
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                RoleRepresentation userRole = keycloak.realm(realm).roles().get("host").toRepresentation();
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
                        .isActive(true)
                        .isAccountNonExpired(true)
                        .isAccountNonLocked(true)
                        .isCredentialsNonExpired(true)
                        .isEnabled(true)
                        .isEmailVerified(true)
                        .createdAt(new Date().toInstant())
                        .updatedAt(new Date().toInstant())
                        .build();

                SystemUser savedUser = systemUserRepository.save(sUser);
                emailService.sendHostPassword(systemUserRequestDto.getEmail(), "Access system by using above password", systemUserRequestDto.getPassword(), systemUserRequestDto.getFirstName());


            }
        }
    }

    /// send Otp
    @Override
    public void resend(String email, String type) {
        try {
            Optional<SystemUser> selectedUser = systemUserRepository.findByEmail(email);
            if (selectedUser.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
            }

            SystemUser systemUser = selectedUser.get();

            if (type.equalsIgnoreCase("SIGNUP")) {

                if (systemUser.isEmailVerified()) {
                    throw new DuplicateEntryException("The email is already activated");
                }
            }

            ///send new otp again
            Otp selectedOtpObj = systemUser.getOtp();
            String code = otpGenerator.generateOtp(5);

            emailService.sendUserSignUpVerificationCode(systemUser.getEmail(), "Verify your email", code, systemUser.getFirstName());
            selectedOtpObj.setAttempts(0);
            selectedOtpObj.setCode(code);
            selectedOtpObj.setIsVerified(false);
            selectedOtpObj.setUpdatedAt(new Date().toInstant());


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void forgotPasswordSendVerificationCode(String email, String password) {

        try {
            Optional<SystemUser> selectedUser = systemUserRepository.findByEmail(email);
            if (selectedUser.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
            }

            SystemUser systemUser = selectedUser.get();

            Keycloak keycloak = null;
            keycloak = keycloakSecurityUtil.getKeycloakInstance();
            UserRepresentation existingUser = keycloak.realm(realm).users().search(email).stream().findFirst().orElse(null);

            if (existingUser != null) {
                throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
            }

            ///send new otp again
            Otp selectedOtpObj = systemUser.getOtp();
            String code = otpGenerator.generateOtp(5);


            selectedOtpObj.setAttempts(0);
            selectedOtpObj.setCode(code);
            selectedOtpObj.setIsVerified(false);
            selectedOtpObj.setUpdatedAt(new Date().toInstant());

            emailService.sendUserSignUpVerificationCode(systemUser.getEmail(), "Verify your email to reset password", code, systemUser.getFirstName());


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public boolean verifyReset(String email, String otp) {
        try {
            Optional<SystemUser> selectedUser = systemUserRepository.findByEmail(email);
            if (selectedUser.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
            }

            SystemUser systemUser = selectedUser.get();
            Otp otpObj = systemUser.getOtp();
            if (otpObj.getCode().equals(otp)) {

                //otpRepository.deleteById(otpObj.getPropertyId());
                otpObj.setAttempts(otpObj.getAttempts() + 1);
                otpObj.setUpdatedAt(new Date().toInstant());
                otpObj.setIsVerified(true);
                otpRepository.save(otpObj);
                return true;
            } else {
                if (otpObj.getAttempts() >= 5) {
                    resend(email, "PASSWORD");
                    throw new BadRequestException("You have a new verification code");
                }
                otpObj.setAttempts(otpObj.getAttempts() + 1);
                otpObj.setUpdatedAt(new Date().toInstant());
                otpRepository.save(otpObj);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean passwordReset(PasswordRequestDto passwordRequestDto) {
        Optional<SystemUser> selectedUserObj = systemUserRepository.findByEmail(passwordRequestDto.getEmail());
        if (selectedUserObj.isPresent()) {
            SystemUser systemUser = selectedUserObj.get();
            Otp otpObj = systemUser.getOtp();
            Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
            List<UserRepresentation> KeyCloakUsers = keycloak.realm(realm).users().search(systemUser.getEmail());
            if (!KeyCloakUsers.isEmpty() && otpObj.getCode().equals(passwordRequestDto.getCode())) {
                UserRepresentation keyCloakUser = KeyCloakUsers.get(0);
                UserResource userResource = keycloak.realm(realm).users().get(keyCloakUser.getId());
                CredentialRepresentation newPass = new CredentialRepresentation();
                newPass.setType(CredentialRepresentation.PASSWORD);
                newPass.setValue(passwordRequestDto.getPassword());
                newPass.setTemporary(false);
                userResource.resetPassword(newPass);

                systemUser.setUpdatedAt(new Date().toInstant());
                systemUserRepository.save(systemUser);

                return true;

            }
            throw new BadRequestException("Try Again");
        }
        throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        Optional<SystemUser> selectedUserObj = systemUserRepository.findByEmail(email);
        if (selectedUserObj.isEmpty()) {
            throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
        }

        SystemUser systemUser = selectedUserObj.get();
        Otp otpObj = systemUser.getOtp();

        if (otpObj.getIsVerified()) {
            throw new BadRequestException("This Otp is already used");
        }

        if (otpObj.getAttempts() >= 5) {
            resend(email, "SIGNUP");
            return false;
        }

        if (otpObj.getCode().equals(otp)) {
            UserRepresentation keycloakUser = keycloakSecurityUtil.getKeycloakInstance().realm(realm)
                    .users().search(email).stream().findFirst()
                    .orElseThrow(() -> new EntryNotFoundException("Unable to find any user associate with the provided email address"));

            keycloakUser.setEmailVerified(true);
            keycloakUser.setEnabled(true);

            keycloakSecurityUtil.getKeycloakInstance().realm(realm).users().get(keycloakUser.getId()).update(keycloakUser);

            systemUser.setEmailVerified(true);
            systemUser.setEnabled(true);
            systemUser.setActive(true);
//            systemUser.setUpdatedAt(new Date().toInstant());

            systemUserRepository.save(systemUser);

            otpObj.setIsVerified(true);
            otpObj.setAttempts(otpObj.getAttempts() + 1);
            otpRepository.save(otpObj);
            return true;


        } else {

            if (otpObj.getAttempts() >= 5) {
                resend(email, "SIGNUP");
                return false;
            }
            otpObj.setAttempts(otpObj.getAttempts() + 1);
            otpRepository.save(otpObj);

        }
        return false;

    }

    @Override
    public Object userLogin(LoginRequestDto loginRequestDto) {
        Optional<SystemUser> selectedUserObj = systemUserRepository.findByEmail(loginRequestDto.getEmail());
        if (selectedUserObj.isEmpty()) {
            throw new EntryNotFoundException("Unable to find any user associate with the provided email address");
        }
        SystemUser systemUser = selectedUserObj.get();
        if (!systemUser.isEmailVerified()) {
            resend(loginRequestDto.getEmail(), "SIGNUP");
            throw new UnAuthorizedException("Please verify your email");
        }

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", "");
        requestBody.add("grant_type", OAuth2Constants.PASSWORD);
        requestBody.add("username", loginRequestDto.getEmail());
        requestBody.add("client_secret", "");
        requestBody.add("password", loginRequestDto.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = restTemplate.postForEntity("keycloak api url", requestBody, Object.class);
        return  response.getBody();

    }
}

