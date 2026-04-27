package com.hrms.recruitment.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hrms.recruitment.common.BusinessException;

@Service
public class JwtService {
    private final String secret;
    private final long ttlSeconds;

    public JwtService(
            @Value("${app.jwt.secret:hrms-local-secret-change-me}") String secret,
            @Value("${app.jwt.ttl-seconds:86400}") long ttlSeconds) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(String username) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"" + username + "\",\"exp\":" + expiresAt + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public String parseUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                throw new BusinessException("登录令牌无效");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long exp = Long.parseLong(payload.replaceAll(".*\"exp\":([0-9]+).*", "$1"));
            if (Instant.now().getEpochSecond() > exp) {
                throw new BusinessException("登录已过期");
            }
            return payload.replaceAll(".*\"sub\":\"([^\"]+)\".*", "$1");
        } catch (RuntimeException ex) {
            if (ex instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("登录令牌解析失败");
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT 签名失败", ex);
        }
    }
}
