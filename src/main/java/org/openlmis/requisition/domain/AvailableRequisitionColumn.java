package org.openlmis.requisition.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "available_requisition_columns", schema = "requisition")
@NoArgsConstructor
@Getter
@Setter
public class AvailableRequisitionColumn extends BaseEntity {
  private String name;

  @ElementCollection(fetch = FetchType.EAGER, targetClass = SourceType.class)
  @Enumerated(EnumType.STRING)
  @Column(name = "value")
  @CollectionTable(
      name = "available_requisition_column_sources",
      joinColumns = @JoinColumn(name = "columnId")
  )
  private Set<SourceType> sources;

  private String label;

  private String indicator;

  private Boolean mandatory;

  private Boolean isDisplayRequired;

  private Boolean canChangeOrder;

  private Boolean canBeChangedByUser;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ColumnType columnType;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof AvailableRequisitionColumn)) {
      return false;
    }

    AvailableRequisitionColumn column = (AvailableRequisitionColumn) obj;
    return Objects.equals(column.getName(), getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}
