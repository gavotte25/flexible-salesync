package org.salesync.tenantbslacknotify.clients;

import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.dtos.TypeStageLookupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TypeServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(TypeServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${type-service.base-url}")
    private String baseUrl;

    public Optional<TypeStageLookupDto> lookupByName(String realm, String typeName, String stageName) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    baseUrl + "/{realm}/lookup?typeName={typeName}&stageName={stageName}",
                    TypeStageLookupDto.class, realm, typeName, stageName));
        } catch (Exception e) {
            logger.warn("Could not resolve type '{}' / stage '{}' for realm '{}': {}", typeName, stageName, realm, e.getMessage());
            return Optional.empty();
        }
    }
}
