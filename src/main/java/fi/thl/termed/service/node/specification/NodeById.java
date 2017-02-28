package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class NodeById
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private UUID id;

  public NodeById(UUID id) {
    this.id = id;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(node.getId(), id);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("id", id.toString()));
  }

  @Override
  public String sqlQueryTemplate() {
    return "id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{id};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeById that = (NodeById) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .toString();
  }

}