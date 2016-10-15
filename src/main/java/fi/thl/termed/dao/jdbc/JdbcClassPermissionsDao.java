package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcClassPermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<ClassId>, GrantedPermission> {

  public JdbcClassPermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<ClassId> id, GrantedPermission value) {
    ClassId classId = id.getObjectId();
    jdbcTemplate.update(
        "insert into class_permission (class_scheme_id, class_id, role, permission) values (?, ?, ?, ?)",
        classId.getSchemeId(), classId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<ClassId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<ClassId> id) {
    ClassId classId = id.getObjectId();
    jdbcTemplate.update(
        "delete from class_permission where class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        classId.getSchemeId(), classId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from class_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<ClassId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from class_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<ClassId> id) {
    ClassId classId = id.getObjectId();
    return jdbcTemplate.queryForObject(
        "select count(*) from class_permission where class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        Long.class,
        classId.getSchemeId(),
        classId.getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<ClassId> id, RowMapper<E> mapper) {
    ClassId classId = id.getObjectId();
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from class_permission where class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        mapper,
        classId.getSchemeId(),
        classId.getId(),
        id.getRole(),
        id.getPermission().toString()));
  }

  @Override
  protected RowMapper<ObjectRolePermission<ClassId>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<ClassId>>() {
      @Override
      public ObjectRolePermission<ClassId> mapRow(ResultSet rs, int rowNum) throws SQLException {
        UUID schemeId = UUIDs.fromString(rs.getString("class_scheme_id"));
        ClassId classId = new ClassId(schemeId, rs.getString("class_id"));
        return new ObjectRolePermission<ClassId>(
            classId, new SchemeRole(schemeId, rs.getString("role")),
            Permission.valueOf(rs.getString("permission")));
      }
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return new RowMapper<GrantedPermission>() {
      public GrantedPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
        return GrantedPermission.INSTANCE;
      }
    };
  }

}
