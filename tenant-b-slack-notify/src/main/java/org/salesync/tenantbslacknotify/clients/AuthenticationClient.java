package org.salesync.tenantbslacknotify.clients;

import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.dtos.SimpleUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationClient {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationClient.class);

    private final RestTemplate restTemplate;

    @Value("${authentication.base-url}")
    private String baseUrl;

    public Optional<SimpleUserDto> findUser(String realm, String userId) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(baseUrl + "/{realm}/user/{userId}", SimpleUserDto.class, realm, userId));
        } catch (Exception e) {
            logger.warn("Could not resolve display name for user '{}' in realm '{}': {}", userId, realm, e.getMessage());
            return Optional.empty();
        }
    }
}
