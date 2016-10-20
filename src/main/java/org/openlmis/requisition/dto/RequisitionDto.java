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
  public void setComments(List<Comment> comments) {
    List<CommentDto> commentDtos = new ArrayList<>();
    for (Comment comment: comments) {
      CommentDto commentDto = new CommentDto();
      comment.export(commentDto);
      commentDtos.add(commentDto);
    }
    this.comments = commentDtos;
  }

  @Override
  public List<Comment.Importer> getComments() {
    List<Comment.Importer> comments = new ArrayList<>();
    comments.addAll(this.comments);
    return comments;
  }


  @Override
  public void setRequisitionLineItems(List<RequisitionLineItem> requisitionLineItems) {
    List<RequisitionLineItemDto> requisitionLineItemDtos = new ArrayList<>();
    for (RequisitionLineItem requisitionLineItem: requisitionLineItems) {
      RequisitionLineItemDto requisitionLineItemDto = new RequisitionLineItemDto();
      requisitionLineItem.export(requisitionLineItemDto);
      requisitionLineItemDtos.add(requisitionLineItemDto);
    }
    this.requisitionLineItems = requisitionLineItemDtos;
  }

  @Override
  public List<RequisitionLineItem.Importer> getRequisitionLineItems() {
    List<RequisitionLineItem.Importer> requisitionLineItems = new ArrayList<>();
    requisitionLineItems.addAll(this.requisitionLineItems);
    return requisitionLineItems;
  }

  public UUID getFacilityId() {
    return facility.getId();
  }

  public UUID getProgramId() {
    return program.getId();
  }

  public UUID getProcessingPeriodId() {
    return processingPeriod.getId();
  }
}
