package com.hotel_mgt_system.auth_service_api.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequestDto {
    private String email;
    private String password;
}
