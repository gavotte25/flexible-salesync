package org.salesync.tenantbslacknotify.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RecordChangedEvent {
    private UUID recordId;
    private String recordName;
    private String companyName;
    private UUID typeId;
    private Map<String, String> properties;
    private Date changedAt;
}
