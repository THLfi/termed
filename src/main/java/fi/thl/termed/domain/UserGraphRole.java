package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class UserGraphRole implements Serializable {

  private final String username;
  private final GraphId graph;
  private final String role;

  public UserGraphRole(String username, GraphId graph, String role) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.graph = checkNotNull(graph, "graph can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
  }

  public String getUsername() {
    return username;
  }

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  public GraphId getGraph() {
    return graph;
  }

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGraphRole that = (UserGraphRole) o;
    return Objects.equals(username, that.username) &&
        Objects.equals(graph, that.graph) &&
        Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, graph, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("graph", graph)
        .add("role", role)
        .toString();
  }

}
