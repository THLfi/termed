package fi.thl.termed.service.node.select;

import static java.util.Objects.requireNonNull;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.LuceneSelectField;
import java.util.Objects;

public abstract class AbstractSelectTypeQualifiedField
    extends AbstractSelectTypeQualified implements LuceneSelectField {

  protected final String field;

  public AbstractSelectTypeQualifiedField(TypeId typeId, String field) {
    super(typeId);
    this.field = requireNonNull(field);
  }

  public String getField() {
    return field;
  }

  @Override
  public String toLuceneSelectField() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() + "." + field;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AbstractSelectTypeQualifiedField that = (AbstractSelectTypeQualifiedField) o;
    return Objects.equals(field, that.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), field);
  }

  @Override
  public String toString() {
    return super.toString() + "." + field;
  }

}
