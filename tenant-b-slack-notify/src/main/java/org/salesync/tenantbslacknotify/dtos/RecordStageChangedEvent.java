package org.salesync.tenantbslacknotify.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RecordStageChangedEvent {
    private UUID recordId;
    private String recordName;
    private String companyName;
    private UUID typeId;
    private UUID stageId;
    private String userId;
    private String userName;
    private Date changedAt;
}
