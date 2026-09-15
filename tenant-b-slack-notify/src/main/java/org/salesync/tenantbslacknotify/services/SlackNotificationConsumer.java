package org.salesync.tenantbslacknotify.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.clients.AuthenticationClient;
import org.salesync.tenantbslacknotify.clients.TenantManagerClient;
import org.salesync.tenantbslacknotify.dtos.RecordStageChangedEvent;
import org.salesync.tenantbslacknotify.dtos.RegistrationDto;
import org.salesync.tenantbslacknotify.dtos.SimpleUserDto;
import org.salesync.tenantbslacknotify.dtos.SlackConfig;
import org.salesync.tenantbslacknotify.dtos.SlackMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlackNotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationConsumer.class);

    private final TenantManagerClient tenantManagerClient;
    private final AuthenticationClient authenticationClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${offering.customization-point}")
    private String customizationPoint;

    @RabbitListener(queues = "tenant-b-slack-notify-queue", ackMode = "AUTO")
    public void onStageChanged(RecordStageChangedEvent event) {
        Optional<RegistrationDto> registration = tenantManagerClient.findActiveRegistration(event.getCompanyName(), customizationPoint);
        if (registration.isEmpty()) {
            return;
        }

        SlackConfig config = parseConfig(registration.get().getConfig(), event.getCompanyName());
        if (config == null || config.getWebhookUrl() == null || config.getTemplates() == null) {
            return;
        }

        String templateKey = event.getTypeId() + ":" + event.getStageId();
        String template = config.getTemplates().get(templateKey);
        if (template == null) {
            return;
        }

        String message = template
                .replace("<USER_NAME_PLACEHOLDER>", resolveDisplayName(event))
                .replace("<RECORD_NAME_PLACEHOLDER>", event.getRecordName());

        restTemplate.postForEntity(config.getWebhookUrl(), new SlackMessageDto(message), String.class);
    }

    private String resolveDisplayName(RecordStageChangedEvent event) {
        return authenticationClient.findUser(event.getCompanyName(), event.getUserId())
                .map(this::toDisplayName)
                .filter(name -> !name.isBlank())
                .orElse(event.getUserName());
    }

    private String toDisplayName(SimpleUserDto user) {
        String fullName = String.join(" ",
                Optional.ofNullable(user.getFirstName()).orElse(""),
                Optional.ofNullable(user.getLastName()).orElse("")).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return Optional.ofNullable(user.getUserName()).orElse("");
    }

    private SlackConfig parseConfig(String rawConfig, String companyName) {
        if (rawConfig == null || rawConfig.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rawConfig, SlackConfig.class);
        } catch (Exception e) {
            logger.warn("Malformed slack-notify config for tenant '{}': {}", companyName, e.getMessage());
            return null;
        }
    }
}
