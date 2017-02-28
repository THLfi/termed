package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;

public class NodesByProperty implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final String value;

  public NodesByProperty(String attributeId, String value) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    this.attributeId = attributeId;
    this.value = value;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId).stream()
        .anyMatch(v -> v.getValue().equals(value));
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("properties." + attributeId, value));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByProperty that = (NodesByProperty) o;
    return Objects.equals(attributeId, that.attributeId) &&
           Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("value", value)
        .toString();
  }

}