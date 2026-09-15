package org.salesync.tenantbslacknotify.services;

import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.clients.TenantManagerClient;
import org.salesync.tenantbslacknotify.dtos.OfferingRegistrationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfferingRegistrar implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(OfferingRegistrar.class);
    private static final int MAX_ATTEMPTS = 10;
    private static final long RETRY_DELAY_MS = 3000;

    private final TenantManagerClient tenantManagerClient;

    @Value("${offering.key}")
    private String offeringKey;

    @Value("${offering.customization-point}")
    private String customizationPoint;

    @Value("${offering.version}")
    private String version;

    @Value("${server.port}")
    private int selfPort;

    @Value("${spring.application.name}")
    private String selfDns;

    @Override
    public void run(String... args) throws InterruptedException {
        OfferingRegistrationDto offering = OfferingRegistrationDto.builder()
                .offeringKey(offeringKey)
                .name("Slack Notification")
                .description("Posts a Slack message when a record reaches a configured type:stage.")
                .customizationPoint(customizationPoint)
                .targetServiceDns(selfDns)
                .targetPort(selfPort)
                .version(version)
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                tenantManagerClient.registerOffering(offering);
                logger.info("Registered offering '{}' with Tenant Manager", offeringKey);
                return;
            } catch (Exception e) {
                logger.warn("Attempt {}/{} to register offering '{}' failed: {}", attempt, MAX_ATTEMPTS, offeringKey, e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    Thread.sleep(RETRY_DELAY_MS);
                }
            }
        }
        logger.error("Giving up registering offering '{}' after {} attempts - restart the deployment once Tenant Manager is reachable.", offeringKey, MAX_ATTEMPTS);
    }
}
