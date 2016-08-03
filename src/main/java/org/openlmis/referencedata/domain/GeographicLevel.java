package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "geographic_levels", schema = "referencedata")
@NoArgsConstructor
public class GeographicLevel extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer levelNumber;

  public static GeographicLevel getMockGeographicLevel() {
    GeographicLevel gl = new GeographicLevel();

    gl.setCode("GL1");
    gl.setLevelNumber(1);

    return gl;
  }
}
