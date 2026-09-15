package org.salesync.tenantbslacknotify.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActivationRequestDto {
    private String realmName;
    private String offeringKey;
    private String config;
}
