package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "facilities", schema = "referencedata")
@NoArgsConstructor
public class Facility extends BaseEntity {

  public static final String TEXT = "text";
  
  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "geographiczoneid", nullable = false)
  @Getter
  @Setter
  private GeographicZone geographicZone;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "typeid", nullable = false)
  @Getter
  @Setter
  private FacilityType type;

  @ManyToOne
  @JoinColumn(name = "operatedbyid")
  @Getter
  @Setter
  private FacilityOperator operator;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean active;

  @Getter
  @Setter
  private Date goLiveDate;

  @Getter
  @Setter
  private Date goDownDate;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String comment;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean enabled;

  @Getter
  @Setter
  private Boolean openLmisAccessible;

  @OneToMany
  @JoinColumn(name = "programId")
  @Getter
  @Setter
  private List<Program> supportedPrograms;

  /**
   * Returns a created facility with only the required parameters set to dummy values.
   * 
   * @return a mock facility
   */
  public static Facility getMockFacility() {
    Facility facility = new Facility();

    facility.setCode("F1");
    facility.setGeographicZone(GeographicZone.getMockGeographicZone());
    facility.setType(FacilityType.getMockFacilityType());
    facility.setActive(true);
    facility.setEnabled(true);
    
    return facility;
  }
}
