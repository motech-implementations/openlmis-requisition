package org.openlmis.requisition.dto;

import org.openlmis.requisition.domain.Comment;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionLineItem;
import org.openlmis.requisition.domain.RequisitionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionDto implements Requisition.Importer, Requisition.Exporter {
  private UUID id;
  private LocalDateTime createdDate;
  private List<RequisitionLineItemDto> requisitionLineItems;
  private List<CommentDto> comments;
  private FacilityDto facility;
  private ProgramDto program;
  private ProcessingPeriodDto processingPeriod;
  private RequisitionStatus status;
  private Boolean emergency;
  private UUID supplyingFacility;
  private UUID supervisoryNode;



  @Override
  public List<CommentDto> getComments() {
    List<CommentDto> comments = new ArrayList<>();
    comments.addAll(this.comments);
    return comments;
  }

  @Override
  public List<RequisitionLineItemDto> getRequisitionLineItems() {
    List<RequisitionLineItemDto> requisitionLineItems = new ArrayList<>();
    requisitionLineItems.addAll(this.requisitionLineItems);
    return requisitionLineItems;
  }
}
