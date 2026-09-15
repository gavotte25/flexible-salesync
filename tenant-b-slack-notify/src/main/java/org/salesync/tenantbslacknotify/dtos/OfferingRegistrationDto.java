package org.salesync.tenantbslacknotify.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OfferingRegistrationDto {
    private String offeringKey;
    private String name;
    private String description;
    private String customizationPoint;
    private String targetServiceDns;
    private int targetPort;
    private String version;
}
