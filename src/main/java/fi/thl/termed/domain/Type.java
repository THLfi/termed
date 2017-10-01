package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.ListUtils.nullToEmpty;
import static fi.thl.termed.util.collect.ListUtils.nullableImmutableCopyOf;
import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Type implements Identifiable<TypeId> {

  private final String id;
  private final GraphId graph;

  private final String uri;
  private final Integer index;

  private final ImmutableMultimap<String, Permission> permissions;
  private final ImmutableMultimap<String, LangValue> properties;

  private final ImmutableList<TextAttribute> textAttributes;
  private final ImmutableList<ReferenceAttribute> referenceAttributes;

  public Type(String id, GraphId graph, String uri, Integer index,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties,
      List<TextAttribute> textAttributes,
      List<ReferenceAttribute> referenceAttributes) {
    this.id = requireNonNull(id);
    this.graph = requireNonNull(graph);
    this.uri = uri;
    this.index = index;
    this.permissions = nullableImmutableCopyOf(permissions);
    this.properties = nullableImmutableCopyOf(properties);
    this.textAttributes = nullableImmutableCopyOf(textAttributes);
    this.referenceAttributes = nullableImmutableCopyOf(referenceAttributes);
  }

  public String getId() {
    return id;
  }

  public GraphId getGraph() {
    return graph;
  }

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  public Optional<String> getUri() {
    return ofNullable(uri);
  }

  public Optional<Integer> getIndex() {
    return ofNullable(index);
  }

  public ImmutableMultimap<String, Permission> getPermissions() {
    return nullToEmpty(permissions);
  }

  public ImmutableMultimap<String, LangValue> getProperties() {
    return nullToEmpty(properties);
  }

  public ImmutableList<TextAttribute> getTextAttributes() {
    return nullToEmpty(textAttributes);
  }

  public ImmutableList<ReferenceAttribute> getReferenceAttributes() {
    return nullToEmpty(referenceAttributes);
  }

  public static TypeIdBuilder builder() {
    return new TypeIdBuilder();
  }

  public static TypeBuilder builderFromCopyOf(Type type) {
    TypeBuilder builder = new TypeBuilder(type.getId(), type.getGraph());
    builder.uri(type.getUri().orElse(null));
    builder.index(type.getIndex().orElse(null));
    builder.permissions(type.getPermissions());
    builder.properties(type.getProperties());
    builder.textAttributes(type.getTextAttributes());
    builder.referenceAttributes(type.getReferenceAttributes());
    return builder;
  }

  @Override
  public TypeId identifier() {
    return new TypeId(id, graph);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("graph", graph)
        .add("uri", uri)
        .add("index", index)
        .add("permissions", permissions)
        .add("properties", properties)
        .add("textAttributes", textAttributes)
        .add("referenceAttributes", referenceAttributes)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Type cls = (Type) o;
    return Objects.equals(id, cls.id) &&
        Objects.equals(graph, cls.graph) &&
        Objects.equals(uri, cls.uri) &&
        Objects.equals(index, cls.index) &&
        Objects.equals(permissions, cls.permissions) &&
        Objects.equals(properties, cls.properties) &&
        Objects.equals(textAttributes, cls.textAttributes) &&
        Objects.equals(referenceAttributes, cls.referenceAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, graph, uri, index, permissions, properties, textAttributes,
        referenceAttributes);
  }

  public static final class TypeIdBuilder {

    public TypeBuilder id(String id, UUID graphId) {
      return new TypeBuilder(id, GraphId.of(graphId));
    }

    public TypeBuilder id(String id, GraphId graph) {
      return new TypeBuilder(id, graph);
    }

    public TypeBuilder id(TypeId typeId) {
      return new TypeBuilder(typeId.getId(), typeId.getGraph());
    }
  }

  public static final class TypeBuilder {

    private final String id;
    private final GraphId graph;

    private String uri;
    private Integer index;
    private Multimap<String, Permission> permissions;
    private Multimap<String, LangValue> properties;
    private List<TextAttribute> textAttributes;
    private List<ReferenceAttribute> referenceAttributes;

    TypeBuilder(String id, GraphId graph) {
      this.id = requireNonNull(id);
      this.graph = requireNonNull(graph);
    }

    public TypeBuilder copyOptionalsFrom(Type type) {
      this.uri = type.uri;
      this.index = type.index;
      this.permissions = type.permissions;
      this.properties = type.properties;
      this.textAttributes = type.textAttributes;
      this.referenceAttributes = type.referenceAttributes;
      return this;
    }

    public TypeBuilder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public TypeBuilder index(Integer index) {
      this.index = index;
      return this;
    }

    public TypeBuilder permissions(Multimap<String, Permission> permissions) {
      this.permissions = permissions;
      return this;
    }

    public TypeBuilder properties(Multimap<String, LangValue> properties) {
      this.properties = properties;
      return this;
    }

    public TypeBuilder properties(String k0, LangValue v0) {
      this.properties = ImmutableMultimap.of(k0, v0);
      return this;
    }

    public TypeBuilder properties(String k0, LangValue v0, String k1, LangValue v1) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1);
      return this;
    }

    public TypeBuilder properties(String k0, LangValue v0, String k1, LangValue v1, String k2,
        LangValue v2) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1, k2, v2);
      return this;
    }

    public TypeBuilder textAttributes(TextAttribute... textAttributes) {
      this.textAttributes = Arrays.asList(textAttributes);
      return this;
    }

    public TypeBuilder textAttributes(List<TextAttribute> textAttributes) {
      this.textAttributes = textAttributes;
      return this;
    }

    public TypeBuilder referenceAttributes(ReferenceAttribute... referenceAttributes) {
      this.referenceAttributes = Arrays.asList(referenceAttributes);
      return this;
    }

    public TypeBuilder referenceAttributes(List<ReferenceAttribute> referenceAttributes) {
      this.referenceAttributes = referenceAttributes;
      return this;
    }

    public Type build() {
      return new Type(id, graph, uri, index,
          permissions, properties, textAttributes, referenceAttributes);
    }
  }

}
