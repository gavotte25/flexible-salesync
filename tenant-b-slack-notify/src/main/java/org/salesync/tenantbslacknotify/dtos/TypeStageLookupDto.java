package org.salesync.tenantbslacknotify.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TypeStageLookupDto {
    private UUID typeId;
    private UUID stageId;
}
