package com.hotel_mgt_system.auth_service_api.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${public.key.string}")
    public String publicKeyString;

    public String getEmail(String token){
        try{
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey =keyFactory.generatePublic(keySpec);

            Jws<Claims> claimsJwt = Jwts.parserBuilder().setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJwt.getBody();
            return body.get("email", String.class);



        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getRole(String token){
        try{
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey =factory.generatePublic(keySpec);

            Jws<Claims> claimsJwt = Jwts.parserBuilder().setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJwt.getBody();
            Map<String, List<String>> realmAccess = (Map<String, List<String>>) body.get("realm_access");
            return realmAccess.get("roles");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
