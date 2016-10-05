package org.openlmis.requisition.service;

import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionLineItem;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.domain.RequisitionTemplate;
import org.openlmis.requisition.dto.FacilityDto;
import org.openlmis.requisition.dto.FacilityTypeApprovedProductDto;
import org.openlmis.requisition.dto.ProcessingPeriodDto;
import org.openlmis.requisition.dto.ProgramDto;
import org.openlmis.requisition.dto.RequisitionDto;
import org.openlmis.requisition.dto.RequisitionGroupProgramScheduleDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.exception.InvalidPeriodException;
import org.openlmis.requisition.exception.InvalidRequisitionStateException;
import org.openlmis.requisition.exception.InvalidRequisitionStatusException;
import org.openlmis.requisition.exception.RequisitionAlreadyExistsException;
import org.openlmis.requisition.exception.RequisitionException;
import org.openlmis.requisition.exception.RequisitionInitializationException;
import org.openlmis.requisition.exception.RequisitionNotFoundException;
import org.openlmis.requisition.exception.RequisitionTemplateNotFoundException;
import org.openlmis.requisition.exception.SkipNotAllowedException;
import org.openlmis.requisition.repository.RequisitionRepository;
import org.openlmis.requisition.service.referencedata.FacilityReferenceDataService;
import org.openlmis.requisition.service.referencedata.FacilityTypeApprovedProductReferenceDataService;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.openlmis.requisition.service.referencedata.RequisitionGroupProgramScheduleReferenceDataService;
import org.openlmis.requisition.service.referencedata.UserReferenceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public class RequisitionService {
  private static final String REQUISITION_BAD_STATUS_MESSAGE = "requisition has bad status";

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionService.class);

  @Autowired
  private RequisitionRepository requisitionRepository;

  @Autowired
  private RequisitionTemplateService requisitionTemplateService;

  @Autowired
  private RequisitionLineCalculator requisitionLineCalculator;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private RequisitionGroupProgramScheduleReferenceDataService referenceDataService;

  @Autowired
  private FacilityTypeApprovedProductReferenceDataService facilityTypeApprovedProductService;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  /**
   * Return requisitionDto with information about facility, program and period.
   * @param requisitionId Id of the requisition to be returned
   * @return RequisitionDto object
   */
  public RequisitionDto getRequisition(UUID requisitionId) throws RequisitionNotFoundException {
    Requisition requisition = requisitionRepository.findOne(requisitionId);
    if (requisition == null) {
      return null;
    }
    ProgramDto program = programReferenceDataService
        .findOne(requisition.getProgram());
    FacilityDto facility = facilityReferenceDataService
        .findOne(requisition.getFacility());
    ProcessingPeriodDto processingPeriod = periodReferenceDataService
        .findOne(requisition.getProcessingPeriod());

    RequisitionDto requisitionDto = new RequisitionDto(
        requisition.getId(),
        requisition.getCreatedDate(),
        requisition.getRequisitionLineItems(),
        requisition.getComments(),
        facility,
        program,
        processingPeriod,
        requisition.getStatus(),
        requisition.getEmergency(),
        requisition.getSupplyingFacility(),
        requisition.getSupervisoryNode());

    return requisitionDto;
  }

  /**
   * Initiated given requisition if possible.
   *
   * @param programId         UUID of Program.
   * @param facilityId        UUID of Facility.
   * @param emergency         Emergency status.
   * @param suggestedPeriodId Period for requisition.
   * @return Initiated requisition.
   * @throws RequisitionException Exception thrown when it is not possible to initialize a
   *                              requisition.
   */
  public Requisition initiate(UUID programId, UUID facilityId, UUID suggestedPeriodId,
                              Boolean emergency) throws RequisitionException {
    if (facilityId == null || programId == null || emergency == null) {
      throw new RequisitionInitializationException(
          "Requisition cannot be initiated with null object"
      );
    }

    FacilityDto facility = facilityReferenceDataService.findOne(facilityId);
    ProgramDto program = programReferenceDataService.findOne(programId);

    if (null == facility || null == program) {
      throw new RequisitionAlreadyExistsException("Cannot initiate requisition."
          + " Requisition with such parameters already exists");
    }

    ProcessingPeriodDto period = findTheOldestPeriod(programId, facilityId);

    if (period == null || (null != suggestedPeriodId && suggestedPeriodId != period.getId())) {
      throw new InvalidPeriodException(
            "Period should be the oldest and not associated with any requisitions");
    }

    RequisitionGroupProgramScheduleDto dto =
          referenceDataService.searchByProgramAndFacility(programId, facilityId);

    if (dto == null) {
      throw new RequisitionInitializationException(
            "Cannot initiate requisition. Requisition group program schedule"
            + " with given program and facility does not exist");
    }

    if (!dto.getProcessingSchedule().getId().equals(
        period.getProcessingSchedule().getId())) {
      throw new InvalidPeriodException("Cannot initiate requisition."
          + " Period for the requisition must belong to the same schedule"
          + " that belongs to the program selected for that requisition");
    }

    Collection<FacilityTypeApprovedProductDto> facilityTypeApprovedProducts =
        facilityTypeApprovedProductService.getFullSupply(
            facility.getId(), program.getId()
        );

    Requisition requisition = new Requisition();
    requisition.setStatus(RequisitionStatus.INITIATED);
    requisition.setEmergency(emergency);
    requisition.setFacility(facilityId);
    requisition.setProgram(programId);
    requisition.setProcessingPeriod(period.getId());
    requisition.setRequisitionLineItems(
        facilityTypeApprovedProducts
            .stream()
            .map(RequisitionLineItem::new)
            .collect(Collectors.toList())
    );

    RequisitionTemplate requisitionTemplate = findRequisitionTemplate(programId);

    requisitionLineCalculator.initiateRequisitionLineItemFields(
        requisition, requisitionTemplate
    );

    requisitionRepository.save(requisition);
    return requisition;
  }

  private RequisitionTemplate findRequisitionTemplate(UUID programId) throws RequisitionException {
    if (null == programId) {
      throw new IllegalArgumentException("Program ID cannot be null");
    }

    RequisitionTemplate template =
        requisitionTemplateService.getTemplateForProgram(programId);

    if (null == template) {
      throw new RequisitionTemplateNotFoundException("RequisitionTemplate not found");
    }

    if (template.getColumnsMap().isEmpty()) {
      throw new RequisitionTemplateNotFoundException("RequisitionTemplate is not defined");
    } else {
      return template;
    }
  }

  /**
   * Delete given Requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be deleted.
   * @throws RequisitionException Exception thrown when it is not possible to delete a requisition.
   */
  public void delete(UUID requisitionId) throws RequisitionException {
    Requisition requisition = requisitionRepository.findOne(requisitionId);

    if (requisition == null) {
      throw new RequisitionNotFoundException(requisitionId);
    } else if (requisition.getStatus() != RequisitionStatus.INITIATED) {
      throw new InvalidRequisitionStatusException("Delete failed - "
          + REQUISITION_BAD_STATUS_MESSAGE);
    } else {
      requisitionRepository.delete(requisition);
      LOGGER.debug("Requisition deleted");
    }
  }

  /**
   * Skip given requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be skipped.
   * @return Skipped Requisition.
   * @throws RequisitionException Exception thrown when it is not possible to skip a requisition.
   */
  public Requisition skip(UUID requisitionId) throws RequisitionException {
    Requisition requisition = requisitionRepository.findOne(requisitionId);

    if (requisition == null) {
      throw new RequisitionNotFoundException(requisitionId);
    } else {
      ProgramDto program = programReferenceDataService.findOne(requisition.getProgram());
      if (requisition.getStatus() != RequisitionStatus.INITIATED) {
        throw new InvalidRequisitionStatusException("Skip failed - "
            + REQUISITION_BAD_STATUS_MESSAGE);
      } else if (!program.getPeriodsSkippable()) {
        throw new SkipNotAllowedException("Skip failed - "
            + "requisition program does not allow skipping");
      } else {
        LOGGER.debug("Requisition skipped");
        requisition.setStatus(RequisitionStatus.SKIPPED);
        return requisitionRepository.save(requisition);
      }
    }
  }

  /**
   * Reject given requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be rejected.
   * @throws RequisitionException Exception thrown when it is not possible to reject a requisition.
   */
  public Requisition reject(UUID requisitionId) throws RequisitionException {

    Requisition requisition = requisitionRepository.findOne(requisitionId);
    if (requisition == null) {
      throw new RequisitionNotFoundException(requisitionId);
    } else if (requisition.getStatus() == RequisitionStatus.AUTHORIZED
        || requisition.getStatus() == RequisitionStatus.SUBMITTED) {
      LOGGER.debug("Requisition rejected: " + requisitionId);
      requisition.setStatus(RequisitionStatus.INITIATED);
      return requisitionRepository.save(requisition);
    } else {
      throw new InvalidRequisitionStatusException("Cannot reject requisition: " + requisitionId
          + " .Requisition must be waiting for approval to be rejected");
    }
  }

  /**
   * Finds requisitions matching all of provided parameters.
   */
  public List<Requisition> searchRequisitions(UUID facility, UUID program,
                                              LocalDateTime createdDateFrom,
                                              LocalDateTime createdDateTo,
                                              UUID processingPeriod,
                                              UUID supervisoryNode,
                                              RequisitionStatus requisitionStatus) {
    return requisitionRepository.searchRequisitions(
        facility, program, createdDateFrom,
        createdDateTo, processingPeriod, supervisoryNode, requisitionStatus);
  }

  /**
   * Get requisitions to approve for specified user.
   */
  public List<Requisition> getRequisitionsForApproval(UUID userId) {
    UserDto user = userReferenceDataService.findOne(userId);
    List<Requisition> requisitionsForApproval = new ArrayList<>();
    Set<ProgramDto> supervisedPrograms = user.getSupervisedPrograms();

    if (supervisedPrograms != null) {
      for (ProgramDto program : supervisedPrograms) {
        requisitionsForApproval.addAll(getAuthorizedRequisitions(program));
      }
    }
    return requisitionsForApproval;
  }

  /**
   * Get authorized requisitions for specified program.
   */
  public List<Requisition> getAuthorizedRequisitions(ProgramDto program) {
    List<Requisition> requisitions = new ArrayList<>();
    List<Requisition> reqList = searchRequisitions(null, program.getId(),
        null, null, null, null, null);
    if (reqList != null) {
      for (Requisition req : reqList) {
        if (req.getStatus() == RequisitionStatus.AUTHORIZED) {
          requisitions.add(req);
        }
      }
    }
    return requisitions;
  }

  /**
   * Releases the list of given requisitions as order.
   *
   * @param requisitionList list of requisitions to be released as order
   * @return list of released requisitions
   */
  public List<Requisition> releaseRequisitionsAsOrder(
      List<Requisition> requisitionList, UserDto user) throws RequisitionException {
    List<Requisition> releasedRequisitions = new ArrayList<>();
    Set<UUID> userFacilities = user.getFulfillmentFacilities()
        .stream().map(FacilityDto::getId).collect(Collectors.toSet());

    for (Requisition requisition : requisitionList) {
      Requisition loadedRequisition = requisitionRepository.findOne(requisition.getId());

      if (RequisitionStatus.APPROVED == loadedRequisition.getStatus()) {
        loadedRequisition.setStatus(RequisitionStatus.RELEASED);
      } else {
        throw new InvalidRequisitionStatusException("Can not release requisition: "
            + loadedRequisition.getId() + " as order. Requisition must be approved.");
      }

      UUID facilityId = requisition.getSupplyingFacility();
      if (userFacilities.contains(facilityId)) {
        loadedRequisition.setSupplyingFacility(facilityId);
      } else {
        throw new InvalidRequisitionStateException("Can not release requisition: "
            + loadedRequisition.getId() + " as order. Requisition must have supplying facility.");
      }

      releasedRequisitions.add(loadedRequisition);
    }

    releasedRequisitions.forEach(r -> requisitionRepository.save(r));
    return releasedRequisitions;
  }

  /**
   * Retrieves available supplying depots for given requisition.
   *
   * @param requisition requisition to find facilities for
   * @return list of facilities
   */
  public List<FacilityDto> getAvailableSupplyingDepots(Requisition requisition) {
    Collection<FacilityDto> facilityDtos = facilityReferenceDataService
        .searchSupplyingDepots(requisition.getProgram(), requisition.getSupervisoryNode());
    return new ArrayList<>(facilityDtos);
  }

  /**
   * Get approved requisitions matching all of provided parameters.
   *
   * @param filterValue Value to be used to filter.
   * @param filterBy    Field used to filter: "programName", "facilityCode", "facilityName" or
   *                    "all".
   * @param sortBy      Field used to sort: "programName", "facilityCode" or "facilityName".
   * @param descending  Descending direction for sort.
   * @param pageNumber  Page number to return.
   * @param pageSize    Quantity for one page.
   * @return List of requisitions.
   */
  public List<Requisition> searchApprovedRequisitionsWithSortAndFilterAndPaging(
      String filterValue, String filterBy, String sortBy, Boolean descending,
      Integer pageNumber, Integer pageSize) {

    return requisitionRepository.searchApprovedRequisitionsWithSortAndFilterAndPaging(
        filterValue, filterBy, sortBy, descending, pageNumber, pageSize);
  }

  /**
   * Return the oldest period which is not associated with any requisition.
   *
   * @param programId Program for Requisition
   * @param facilityId Facility for Requisition
   * @return ProcessingPeriodDto.
   */
  private ProcessingPeriodDto findTheOldestPeriod(UUID programId, UUID facilityId) {

    ProcessingPeriodDto result = null;
    Collection<ProcessingPeriodDto> periods =
          periodReferenceDataService.searchByProgramAndFacility(programId, facilityId);

    List<Requisition> requisitions;

    if (periods != null) {
      for (ProcessingPeriodDto dto : periods) {
        requisitions = requisitionRepository.searchByProcessingPeriod(dto.getId());
        if (requisitions == null || requisitions.isEmpty()) {
          result = dto;
          break;
        }
      }
    }
    return result;
  }
}
