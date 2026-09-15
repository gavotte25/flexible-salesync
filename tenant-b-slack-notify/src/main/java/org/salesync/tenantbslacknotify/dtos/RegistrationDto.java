package org.salesync.tenantbslacknotify.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationDto {
    private String realmName;
    private String customizationPoint;
    private String config;
    private boolean active;
}
