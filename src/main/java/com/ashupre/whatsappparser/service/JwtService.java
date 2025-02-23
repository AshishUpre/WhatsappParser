package com.ashupre.whatsappparser.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final String jwtSecret;// Replace with your secret key

    public String createJwt(String email) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("https://ashupre.com")
                .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000)); // 1 hour from now


        SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
        JWSSigner signer = new MACSigner(jwtSecret);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public JWTClaimsSet validateJwt(String jwtString) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        JWSVerifier verifier = new MACVerifier(jwtSecret);

        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Invalid JWT signature");
        }

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        if (claimsSet.getExpirationTime().before(new Date())) {
            throw new RuntimeException("JWT has expired");
        }

        return claimsSet;
    }

    public String extractEmail(String jwt) {
        try {
            return validateJwt(jwt).getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}