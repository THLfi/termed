package fi.thl.termed.dao.jdbc;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcTextAttributePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> {

  public JdbcTextAttributePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<TextAttributeId> id, GrantedPermission value) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    jdbcTemplate.update(
        "insert into text_attribute_permission (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, role, permission) values (?, ?, ?, ?, ?)",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<TextAttributeId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    jdbcTemplate.update(
        "delete from text_attribute_permission where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from text_attribute_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<TextAttributeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from text_attribute_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute_permission where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        Long.class,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<TextAttributeId> id, RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from text_attribute_permission where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        mapper,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()));
  }

  @Override
  protected RowMapper<ObjectRolePermission<TextAttributeId>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<TextAttributeId>>() {
      @Override
      public ObjectRolePermission<TextAttributeId> mapRow(ResultSet rs, int rowNum)
          throws SQLException {

        UUID schemeId = UUIDs.fromString(rs.getString("text_attribute_scheme_id"));
        ClassId domainId = new ClassId(schemeId, rs.getString("text_attribute_domain_id"));
        TextAttributeId textAttributeId = new TextAttributeId(
            domainId, rs.getString("text_attribute_id"));

        return new ObjectRolePermission<TextAttributeId>(
            textAttributeId, new SchemeRole(schemeId, rs.getString("role")),
            Permission.valueOf(rs.getString("permission")));
      }
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return new RowMapper<GrantedPermission>() {
      @Override
      public GrantedPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
        return GrantedPermission.INSTANCE;
      }
    };
  }

}
