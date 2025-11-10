package com.api.voting.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {

    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.expiration-minutes}")
    private long expirationMinutes;

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        var builder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp));

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        try {
            var header = new JWSHeader(JWSAlgorithm.HS256);
            var signed = new SignedJWT(header, builder.build());
            signed.sign(new MACSigner(secret.getBytes()));
            return signed.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Falha ao gerar token JWT", e);
        }
    }

    public JWTClaimsSet validateAndGetClaims(String token) {
        try {
            var signed = SignedJWT.parse(token);
            var verifier = new MACVerifier(secret.getBytes());
            if (!signed.verify(verifier)) throw new RuntimeException("Assinatura inválida");

            var claims = signed.getJWTClaimsSet();
            var now = new Date();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(now)) {
                throw new RuntimeException("Token expirado");
            }
            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Token inválido", e);
        }
    }
}
