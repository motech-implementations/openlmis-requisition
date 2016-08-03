package org.openlmis.requisition.web;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Period;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.dto.RequisitionInitiateDto;
import org.openlmis.requisition.exception.RequisitionException;
import org.openlmis.requisition.service.RequisitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class RequisitionControllerTest {

  @Mock
  private RequisitionService requisitionService;

  @InjectMocks
  private RequisitionController controller;
  
  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void shouldInitiateRequisition() throws RequisitionException {
    UUID facilityId = UUID.randomUUID();
    UUID programId = UUID.randomUUID();
    UUID processingPeriodId = UUID.randomUUID();
    RequisitionInitiateDto reqDto = new RequisitionInitiateDto();
    reqDto.setFacilityId(facilityId);
    reqDto.setProgramId(programId);
    reqDto.setProcessingPeriodId(processingPeriodId);
    reqDto.setEmergency(false);

    Requisition initiatedRequisition = new Requisition();
    initiatedRequisition.setFacility(Facility.getMockFacility());
    initiatedRequisition.setProgram(Program.getMockProgram());
    initiatedRequisition.setProcessingPeriod(Period.getMockProcessingPeriod());

    when(requisitionService.initiateRequisition(facilityId, programId, processingPeriodId, 
        false)).thenReturn(initiatedRequisition);

    ResponseEntity response = controller.initiateRequisition(reqDto, null);

    verify(requisitionService).initiateRequisition(facilityId, programId, processingPeriodId,
        false);
    assertThat(response.getBody(), is(initiatedRequisition));
    assertThat(response.getStatusCode(), is(equalTo(HttpStatus.CREATED)));
  }
}
