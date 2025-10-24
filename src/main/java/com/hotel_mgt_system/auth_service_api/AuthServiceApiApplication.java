package com.hotel_mgt_system.auth_service_api;

import com.hotel_mgt_system.auth_service_api.dto.request.SystemUserRequestDto;
import com.hotel_mgt_system.auth_service_api.service.SystemUserService;
import com.hotel_mgt_system.auth_service_api.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class AuthServiceApiApplication implements CommandLineRunner {

    private final SystemUserService systemUserService;
    private final PasswordGenerator passwordGenerator;

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApiApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {

        SystemUserRequestDto user1 = new SystemUserRequestDto("ABC","XYZ","testintern999@gmail.com", passwordGenerator.generatePassword(), "0713996535");
        SystemUserRequestDto user2 = new SystemUserRequestDto("DEF","XRT","kariyapperumadarshana@gmail.com", passwordGenerator.generatePassword(), "0723996535");

        systemUserService.initializeHosts(Arrays.asList(user1,user2));
    }
}
