package org.salesync.tenantbslacknotify.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.salesync.tenantbslacknotify.clients.TenantManagerClient;
import org.salesync.tenantbslacknotify.clients.TypeServiceClient;
import org.salesync.tenantbslacknotify.dtos.RecordChangedEvent;
import org.salesync.tenantbslacknotify.dtos.RegistrationDto;
import org.salesync.tenantbslacknotify.dtos.SlackConfig;
import org.salesync.tenantbslacknotify.dtos.TypeStageLookupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlackSettingRecordConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SlackSettingRecordConsumer.class);
    private static final String SETTING_RECORD_NAME = "Slack Setting";
    private static final String JSON_CONFIG_PROPERTY = "Json Config";
    private static final String WEBHOOK_URL_PROPERTY = "Webhook Url";

    private final TenantManagerClient tenantManagerClient;
    private final TypeServiceClient typeServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${offering.key}")
    private String offeringKey;

    @Value("${offering.customization-point}")
    private String customizationPoint;

    @RabbitListener(queues = "tenant-b-slack-notify-setting-queue", ackMode = "AUTO")
    public void onRecordChanged(RecordChangedEvent event) {
        if (!SETTING_RECORD_NAME.equals(event.getRecordName()) || event.getProperties() == null) {
            return;
        }

        Optional<RegistrationDto> registration = tenantManagerClient.findActiveRegistration(event.getCompanyName(), customizationPoint);
        if (registration.isEmpty()) {
            return;
        }

        String webhookUrl = event.getProperties().get(WEBHOOK_URL_PROPERTY);
        Map<String, String> namedTemplates = parseNamedTemplates(event.getProperties().get(JSON_CONFIG_PROPERTY), event.getCompanyName());
        if (webhookUrl == null || webhookUrl.isBlank() || namedTemplates == null) {
            return;
        }

        Map<String, String> resolvedTemplates = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : namedTemplates.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            if (parts.length != 2) {
                logger.warn("Skipping malformed template key '{}' for tenant '{}'", entry.getKey(), event.getCompanyName());
                continue;
            }
            typeServiceClient.lookupByName(event.getCompanyName(), parts[0], parts[1]).ifPresentOrElse(
                    lookup -> resolvedTemplates.put(lookup.getTypeId() + ":" + lookup.getStageId(), entry.getValue()),
                    () -> logger.warn("Could not resolve '{}' for tenant '{}' - skipping that template", entry.getKey(), event.getCompanyName()));
        }

        SlackConfig config = new SlackConfig();
        config.setWebhookUrl(webhookUrl);
        config.setTemplates(resolvedTemplates);
        String configJson;
        try {
            configJson = objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            logger.warn("Failed to serialize resolved slack-notify config for tenant '{}': {}", event.getCompanyName(), e.getMessage());
            return;
        }

        tenantManagerClient.activate(event.getCompanyName(), offeringKey, configJson);
    }

    private Map<String, String> parseNamedTemplates(String rawJsonConfig, String companyName) {
        if (rawJsonConfig == null || rawJsonConfig.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rawJsonConfig, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            logger.warn("Malformed '{}' property on Slack Setting record for tenant '{}': {}", JSON_CONFIG_PROPERTY, companyName, e.getMessage());
            return null;
        }
    }
}
