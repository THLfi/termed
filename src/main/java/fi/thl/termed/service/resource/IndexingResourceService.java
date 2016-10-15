package fi.thl.termed.service.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeResourcesByValueId;
import fi.thl.termed.spesification.sql.ResourceReferenceAttributeValuesByResourceId;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.SpecificationQuery;
import fi.thl.termed.util.specification.SpecificationQuery.Engine;

import static com.google.common.collect.Lists.transform;

/**
 * Manages querying and updating full text index of resources
 */
public class IndexingResourceService extends ForwardingService<ResourceId, Resource> {

  private Repository<ResourceId, Resource> resourceRepository;

  private Index<ResourceId, Resource> resourceIndex;

  private PermissionEvaluator<ClassId> classEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  private SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao;

  public IndexingResourceService(
      Service<ResourceId, Resource> delegate,
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      PermissionEvaluator<ClassId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator,
      SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao) {
    super(delegate);
    this.resourceRepository = resourceRepository;
    this.resourceIndex = resourceIndex;
    this.classEvaluator = classEvaluator;
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
    this.referenceAttributeValueDao = referenceAttributeValueDao;
  }

  @Override
  public List<Resource> get(SpecificationQuery<ResourceId, Resource> specification,
                            User currentUser) {
    return specification.getEngine() == Engine.LUCENE
           ? filterByPermissions(resourceIndex.query(specification), currentUser)
           : resourceRepository.get(specification, currentUser);
  }

  // resources retrieved from index contain all data regardless of the user searching, thus filter
  private List<Resource> filterByPermissions(List<Resource> resources, User user) {
    Predicate<Resource> resourcePermissionPredicate =
        new ResourcePermissionPredicate(classEvaluator, user, Permission.READ);
    Function<Resource, Resource> resourceAttributeFilter =
        new ResourceAttributePermissionFilter(
            classEvaluator, textAttrEvaluator, refAttrEvaluator, user, Permission.READ);

    return resources.stream()
        .filter(resourcePermissionPredicate)
        .map(resourceAttributeFilter)
        .collect(Collectors.toList());
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User currentUser) {
    Set<ResourceId> affectedIds = Sets.newHashSet();

    for (Resource resource : resources) {
      affectedIds.add(new ResourceId(resource));
      affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    List<ResourceId> ids = super.save(resources, currentUser);

    for (Resource resource : resources) {
      affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));
    }

    asyncReindex(affectedIds);

    return ids;
  }

  @Override
  public ResourceId save(Resource resource, User currentUser) {
    Set<ResourceId> affectedIds = Sets.newHashSet();
    affectedIds.add(new ResourceId(resource));
    affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));

    ResourceId id = super.save(resource, currentUser);

    affectedIds.addAll(resourceRelatedIds(new ResourceId(resource)));
    reindex(affectedIds);

    return id;
  }

  @Override
  public void delete(ResourceId resourceId, User currentUser) {
    Set<ResourceId> affectedIds = resourceRelatedIds(resourceId);

    super.delete(resourceId, currentUser);

    resourceIndex.deleteFromIndex(resourceId);

    asyncReindex(affectedIds);
  }

  private Set<ResourceId> resourceRelatedIds(ResourceId resourceId) {
    Set<ResourceId> relatedIds = Sets.newHashSet();
    relatedIds.addAll(referencedResourceIds(resourceId));
    relatedIds.addAll(referringResourceIds(resourceId));
    return relatedIds;
  }

  private List<ResourceId> referencedResourceIds(ResourceId resourceId) {
    return referenceAttributeValueDao.getValues(
        new ResourceReferenceAttributeValuesByResourceId(resourceId));
  }

  private List<ResourceId> referringResourceIds(ResourceId resourceId) {
    List<ResourceAttributeValueId> keys = referenceAttributeValueDao.getKeys(
        new ResourceReferenceAttributeResourcesByValueId(resourceId));
    return transform(keys, ResourceAttributeValueId::getResourceId);
  }

  private void reindex(Set<ResourceId> affectedIds) {
    for (ResourceId affectedId : affectedIds) {
      resourceIndex.reindex(
          affectedId,
          resourceRepository.get(affectedId, new User("indexer", "", AppRole.ADMIN)).get());
    }
  }

  private void asyncReindex(Set<ResourceId> ids) {
    if (!ids.isEmpty()) {
      resourceIndex.reindex(
          ImmutableList.copyOf(ids),
          id -> resourceRepository.get(id, new User("indexer", "", AppRole.ADMIN)).get());
    }
  }

}
