package fi.thl.termed.service.node.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * For filtering node service read operations. Useful to put in front of an index.
 */
public class ReadAuthorizedNodeService implements Service<NodeId, Node> {

  private Service<NodeId, Node> delegate;

  private PermissionEvaluator<NodeId> nodeEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  public ReadAuthorizedNodeService(
      Service<NodeId, Node> delegate,
      PermissionEvaluator<TypeId> typeEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator) {
    this.delegate = delegate;
    this.nodeEvaluator = (u, r, p) -> typeEvaluator.hasPermission(u, r.getType(), p);
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
  }

  @Override
  public void save(Stream<Node> values, SaveMode mode, WriteOptions opts, User user) {
    delegate.save(values, mode, opts, user);
  }

  @Override
  public NodeId save(Node value, SaveMode mode, WriteOptions opts, User user) {
    return delegate.save(value, mode, opts, user);
  }

  @Override
  public void delete(Stream<NodeId> ids, WriteOptions opts, User user) {
    delegate.delete(ids, opts, user);
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    delegate.delete(id, opts, user);
  }

  @Override
  public void saveAndDelete(Stream<Node> saves, Stream<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    delegate.saveAndDelete(saves, deletes, mode, opts, user);
  }

  @Override
  public Stream<Node> values(Query<NodeId, Node> query, User user) {
    return filterValues(delegate.values(query, user), user);
  }

  @Override
  public Stream<NodeId> keys(Query<NodeId, Node> query, User user) {
    return filterKeys(delegate.keys(query, user), user);
  }

  @Override
  public long count(Specification<NodeId, Node> spec, User user) {
    return delegate.count(spec, user);
  }

  @Override
  public boolean exists(NodeId id, User user) {
    return nodeEvaluator.hasPermission(user, id, Permission.READ) && delegate.exists(id, user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user, Select... selects) {
    if (!nodeEvaluator.hasPermission(user, id, Permission.READ)) {
      return Optional.empty();
    }

    return delegate.get(id, user, selects)
        .filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private Stream<NodeId> filterKeys(Stream<NodeId> keys, User user) {
    return keys.filter(id -> nodeEvaluator.hasPermission(user, id, Permission.READ));
  }

  private Stream<Node> filterValues(Stream<Node> values, User user) {
    return values.filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private class AttributePermissionFilter implements Function<Node, Node> {

    private User user;
    private Permission permission;

    AttributePermissionFilter(User user, Permission permission) {
      this.user = user;
      this.permission = permission;
    }

    @Override
    public Node apply(Node node) {
      TypeId typeId = node.getType();
      return Node.builderFromCopyOf(node)
          .properties(Multimaps.filterKeys(
              node.getProperties(), new AcceptPropertyPredicate(typeId)))
          .references(Multimaps.filterEntries(
              node.getReferences(), new AcceptReferenceEntryPredicate(typeId)))
          .referrers(Multimaps.filterEntries(
              node.getReferrers(), new AcceptReferrerEntryPredicate()))
          .build();
    }

    /**
     * Accept a node text attribute value if attribute is permitted
     */
    private class AcceptPropertyPredicate implements Predicate<String> {

      private TypeId typeId;

      AcceptPropertyPredicate(TypeId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(String attributeId) {
        TextAttributeId textAttrId = new TextAttributeId(typeId, attributeId);
        return textAttrEvaluator.hasPermission(user, textAttrId, permission);
      }
    }

    /**
     * Accepts a node reference entry in the reference multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferenceEntryPredicate implements Predicate<Map.Entry<String, NodeId>> {

      private TypeId typeId;

      AcceptReferenceEntryPredicate(TypeId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(Map.Entry<String, NodeId> entry) {
        String attributeId = entry.getKey();
        NodeId reference = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(typeId, attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
            nodeEvaluator.hasPermission(user, reference, permission);
      }
    }

    /**
     * Accepts a node referrer entry in the referrers multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferrerEntryPredicate implements Predicate<Map.Entry<String, NodeId>> {

      @Override
      public boolean apply(Map.Entry<String, NodeId> entry) {
        String attributeId = entry.getKey();
        NodeId referrer = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(referrer.getType(), attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
            nodeEvaluator.hasPermission(user, referrer, permission);
      }
    }

  }

}
