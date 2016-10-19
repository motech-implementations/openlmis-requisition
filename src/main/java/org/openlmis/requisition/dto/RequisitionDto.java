package org.openlmis.requisition.dto;

import org.openlmis.requisition.domain.Comment;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionLineItem;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.service.referencedata.FacilityReferenceDataService;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

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
  public void setFacility(UUID facilityId) {
    this.facility = facilityReferenceDataService
        .findOne(facilityId);

  }

  @Override
  public void setProcessingPeriod(UUID processingPeriodId) {
    this.processingPeriod = periodReferenceDataService
        .findOne(processingPeriodId);
  }

  @Override
  public void setProgram(UUID programId) {
    this.program = programReferenceDataService
        .findOne(programId);
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

  public UUID getFacility() {
    return facility.getId();
  }

  public UUID getProgram() {
    return program.getId();
  }

  public UUID getProcessingPeriod() {
    return processingPeriod.getId();
  }
}
