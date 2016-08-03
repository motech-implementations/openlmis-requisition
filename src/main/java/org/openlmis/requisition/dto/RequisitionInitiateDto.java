package org.openlmis.requisition.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class RequisitionInitiateDto {

  @Getter
  @Setter
  private UUID facilityId;

  @Getter
  @Setter
  private UUID programId;

  @Getter
  @Setter
  private UUID processingPeriodId;

  @Getter
  @Setter
  private Boolean emergency;
}
