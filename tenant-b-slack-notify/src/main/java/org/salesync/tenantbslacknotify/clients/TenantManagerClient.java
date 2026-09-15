package org.salesync.tenantbslacknotify.clients;

import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.dtos.ActivationRequestDto;
import org.salesync.tenantbslacknotify.dtos.OfferingRegistrationDto;
import org.salesync.tenantbslacknotify.dtos.RegistrationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantManagerClient {
    private final RestTemplate restTemplate;

    @Value("${tenant-manager.base-url}")
    private String baseUrl;

    public void registerOffering(OfferingRegistrationDto offering) {
        restTemplate.postForEntity(baseUrl + "/offerings", offering, Void.class);
    }

    public Optional<RegistrationDto> findActiveRegistration(String realm, String customizationPoint) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    baseUrl + "/registrations/{realm}/{point}", RegistrationDto.class, realm, customizationPoint));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public void activate(String realm, String offeringKey, String config) {
        restTemplate.postForEntity(baseUrl + "/activations", new ActivationRequestDto(realm, offeringKey, config), Void.class);
    }
}
