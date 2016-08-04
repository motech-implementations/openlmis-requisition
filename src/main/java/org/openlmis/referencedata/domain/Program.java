package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "programs", schema = "referencedata")
@NoArgsConstructor
public class Program extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private Boolean active;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean periodsSkippable;

  @Getter
  @Setter
  private Boolean showNonFullSupplyTab;

  @PrePersist
  private void prePersist() {
    if (this.periodsSkippable == null) {
      this.periodsSkippable = false;
    }
  }

  /**
   * Returns a created program with only the required parameters set to dummy values.
   *
   * @return a mock program
   */
  public static Program getTestProgram() {
    Program program = new Program();
    
    program.setCode("P1");
    program.setPeriodsSkippable(true);
    
    return program;
  }
}
