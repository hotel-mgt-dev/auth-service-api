package com.hotel_mgt_system.auth_service_api.repository;

import com.hotel_mgt_system.auth_service_api.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser,String> {

    //@Query(value = "SELECT * FROM system_user WHERE email=?1", nativeQuery = true)

    public Optional<SystemUser> findByEmail(String email);
}
