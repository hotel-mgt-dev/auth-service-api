package com.hotel_mgt_system.auth_service_api.repository;

import com.hotel_mgt_system.auth_service_api.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemRepository extends JpaRepository<SystemUser,String> {
}
