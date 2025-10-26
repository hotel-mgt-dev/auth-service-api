package com.hotel_mgt_system.auth_service_api.dto.request;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PasswordRequestDto {
    private String email;
    private String password;
    private String code;
}
