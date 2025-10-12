package com.hotel_mgt_system.auth_service_api.repository;

import com.hotel_mgt_system.auth_service_api.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpReppository extends JpaRepository<Otp,String> {
}
