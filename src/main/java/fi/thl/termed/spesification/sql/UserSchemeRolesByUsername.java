package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class UserSchemeRolesByUsername extends AbstractSpecification<UserSchemeRoleId, Empty>
    implements SqlSpecification<UserSchemeRoleId, Empty> {

  private String username;

  public UserSchemeRolesByUsername(String username) {
    this.username = username;
  }

  @Override
  public boolean accept(UserSchemeRoleId id, Empty value) {
    return Objects.equals(id.getUsername(), username);
  }

  @Override
  public String sqlQueryTemplate() {
    return "username = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{username};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserSchemeRolesByUsername that = (UserSchemeRolesByUsername) o;
    return Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(username);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .toString();
  }

}
