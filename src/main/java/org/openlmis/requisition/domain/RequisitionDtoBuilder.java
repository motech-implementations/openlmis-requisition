package org.openlmis.requisition.domain;


import org.openlmis.requisition.dto.RequisitionDto;
import org.openlmis.requisition.service.referencedata.FacilityReferenceDataService;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class RequisitionDtoBuilder {
  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  /**
   * Create a new instance of RequisitionDto based on data from {@link Requisition}.
   *
   * @param requisition instance used to create {@link RequisitionDto}
   * @return new instance of {@link RequisitionDto}
   */
  public RequisitionDto build(Requisition requisition) {
    RequisitionDto requisitionDto = new RequisitionDto();

    requisition.export(requisitionDto);


    requisitionDto.setFacility(facilityReferenceDataService.findOne(requisition.getFacilityId()));
    requisitionDto.setProgram(programReferenceDataService.findOne(requisition.getProgramId()));
    requisitionDto.setProcessingPeriod(periodReferenceDataService.findOne(requisition
        .getProcessingPeriodId()));

    return requisitionDto;
  }

}
